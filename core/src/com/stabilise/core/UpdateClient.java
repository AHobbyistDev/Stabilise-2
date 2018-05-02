package com.stabilise.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Arrays;

import javaslang.control.Option;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.network.Client;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.update.*;
import com.stabilise.util.AppDriver;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

/**
 * A simple update client which connects to an {@link UpdateServer} over LAN to
 * update the game files.
 */
public class UpdateClient extends Client implements IClientUpdate {
    
    public static void main(String[] args) {
        InetAddress adr = null;
        
        try {
            adr = findServer();
        } catch(IOException e) {
            System.out.println("IO error while searching for server");
            e.printStackTrace();
        }
        
        if(adr == null) {
            System.out.println("Update server not found; aborting update");
            return;
        }
        
        UpdateClient client = new UpdateClient(adr);
        AppDriver driver = new AppDriver(24, client::update, null);
        Runnable doClose = () -> {
            driver.stop();
            client.disconnect();
            System.exit(0);
        };
        client.addListener(evtGameReady, doClose);
        client.addListener(EVENT_DISCONNECTED, doClose);
        client.tryConnect();
        driver.run();
    }
    
    private static InetAddress findServer() throws IOException {
        DatagramSocket s = new DatagramSocket();
        s.setBroadcast(true);
        DatagramPacket p;
        byte[] sendData = Constants.BROADCAST_MSG;
        InetAddress adr = Constants.BROADCAST_ADDRESS;
        int port = Constants.PORT_BROADCAST;
        
        try {
            p = new DatagramPacket(sendData, sendData.length, adr, port);
            s.send(p);
            System.out.println("Sent server IP request broadcast to " + adr.getHostAddress());
        } catch(IOException e) {}
        
        // Try all local network interfaces
        for(NetworkInterface ni : IteratorUtils.toIterable(NetworkInterface.getNetworkInterfaces())) {
            if(ni.isLoopback() || !ni.isUp())
                break; // don't broadcast to loopback
            
            for(InterfaceAddress ia : ni.getInterfaceAddresses()) {
                adr = ia.getBroadcast();
                if(adr == null)
                    continue;
                
                try {
                    p = new DatagramPacket(sendData, sendData.length, adr, port);
                    s.send(p);
                    System.out.println("Sent server IP request broadcast to " + adr.getHostAddress());
                } catch(IOException e) {}
            }
        }
        
        System.out.println("Waiting for reply from the server");
        
        byte[] expected = Constants.BROADCAST_RESPONSE;
        byte[] buf = new byte[expected.length];
        p = new DatagramPacket(buf, buf.length);
        s.setSoTimeout(1000);
        s.receive(p);
        s.close();
        
        if(Arrays.equals(buf, expected))
            return p.getAddress();
        
        return null;
    }
    
    
    private static final FileHandle VERSIONDATA = Resources.DIR_GAMEDATA.child("versiondata");
    
    public static final Event evtGameReady = new Event("gameReady");
    
    
    private DataCompound versionData;
    
    
    public UpdateClient(InetAddress address) {
        super(address, Constants.PORT_SERVER, Protocol.UPDATE);
    }
    
    /**
     * Attempts to connect to the update server.
     */
    public void tryConnect() {
        connect();
        if(!isConnected()) {
            System.out.println("Could not connect to update server... aborting");
            events.post(evtGameReady);
        }
    }
    
    public void addListener(Event event, Runnable listener) {
        addListener(Tasks.currentThreadExecutor(), event, e -> listener.run());
    }
    
    @Override
    public void handleFileList(TCPConnection con, S000FileList p) {
        System.out.println("Received file list");
        System.out.println("Preparing checksums...");
        
        // We send the server our checksums for every file on the list, using
        // the cached values from the versiondata file if possible.
        try {
            versionData = IOUtil.read(VERSIONDATA, Format.NBT, Compression.UNCOMPRESSED);
        } catch(IOException e) {
            System.out.println("Could not load versiondata file. Resetting...");
            versionData = DataCompound.create();
        }
        
        C000Checksums c = new C000Checksums();
        
        for(String path : p.files) {
            // Try versiondata first, otherwise calculate as we go
            Option<byte[]> ch = versionData.optI8Arr(path);
            if(ch.isDefined()) {
                c.add(path, ch.get());
            } else {
                FileHandle file = Resources.DIR_APP.child(path);
                byte[] checksum = null;
                if(file.exists()) {
                    try {
                        checksum = IOUtil.checksum(file);
                        versionData.put(path, checksum); // update versiondata
                    } catch(IOException e) {
                        System.out.println("Could not calculate checksum for " + file);
                    }
                }
                c.add(path, checksum == null ? new byte[0] : checksum);
            }
        }
        
        try {
            IOUtil.write(VERSIONDATA, versionData, Compression.UNCOMPRESSED);
        } catch(IOException e) {
            System.err.println("Could not save versiondata!");
            e.printStackTrace();
        }
        
        System.out.println("Checksums generated. Sending...");
        
        con.sendPacket(c); // send the checksums away
    }
    
    @Override
    public void handleFileTransfer(TCPConnection con, S001FileTransfer p) {
        System.out.println("Received files from server! Unzipping...");
        
        for(int i = 0; i < p.files.size(); i += 2) {
            String s1 = p.files.get(i);
            String s2 = p.files.get(i+1);
            FileHandle zipFile = Resources.DIR_APP.child(s1);
            FileHandle destination = Resources.DIR_APP.child(s2);
            
            System.out.println("Unzipping " + zipFile + " into " + destination);
            
            try {
                IOUtil.unzip(zipFile, destination);
            } catch(IOException e) {
                System.out.println("Could not unzip " + zipFile);
                e.printStackTrace();
            }
        }
        
        System.out.println("Update complete");
        
        events.post(evtGameReady);
    }
    
    @SuppressWarnings("unused")
    private void handleGameFiles(TCPConnection con) {
        System.out.println("Deleting old game files...");
        for(FileHandle f : Resources.GAMEFILES_DIRS)
            f.deleteDirectory();
        System.out.println("Unzipping game files...");
        try {
            IOUtil.unzip(Resources.GAMEFILES_DEST, Resources.DIR_APP);
        } catch(IOException e) {
            System.out.println("Error while unzipping!");
            Application.crashApplication(e);
            return;
        }
    }
    
}
