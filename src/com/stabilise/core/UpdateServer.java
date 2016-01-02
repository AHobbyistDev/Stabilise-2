package com.stabilise.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.app.Application;
import com.stabilise.network.Server;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.update.C000Checksums;
import com.stabilise.network.protocol.update.IServerUpdate;
import com.stabilise.network.protocol.update.S000FileList;
import com.stabilise.network.protocol.update.S001FileTransfer;
import com.stabilise.util.StringUtil;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.io.IOUtil;

/**
 * Hosts a simple update server to distribute game files over LAN.
 */
public class UpdateServer extends Server implements IServerUpdate {
    
    public static void main(String[] args) throws IOException {
        ResourcesRaw.loadAllDependencies();
        
        Server server = new UpdateServer();
        server.runConcurrently();
        
        new Thread(UpdateServer::listenForBroadcasts).start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> socket.get().close()));
        
        // Read for console quits
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while(true) {
                String s = br.readLine();
                if(s.contains("quit") || s.contains("exit"))
                    break; // fall through
            }
        } catch(IOException e) {
            // Fall through
        }
        System.exit(0);
    }
    
    private static void listenForBroadcasts() {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket(Constants.PORT_BROADCAST, InetAddress.getByName("0.0.0.0"));
            socket.set(s);
            s.setBroadcast(true);
            
            byte[] expected = Constants.BROADCAST_MSG;
            byte[] buf = new byte[expected.length];
            
            while(true) {
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                s.receive(p);
                
                if(Arrays.equals(expected, buf)) {
                    System.out.println("Detected broadcast. Sending IP...");
                    byte[] send = Constants.BROADCAST_RESPONSE;
                    DatagramPacket p2 = new DatagramPacket(send, send.length, p.getAddress(), p.getPort());
                    s.send(p2);
                }
            }
        } catch(IOException e) {
            System.out.println("Broadcast socket failed");
            e.printStackTrace();
        } finally {
            s.close();
        }
    }
    
    private static final Box<DatagramSocket> socket = Boxes.emptyMut();
    
    private final S000FileList fileListPacket = new S000FileList();
    private final List<String> fileList = fileListPacket.files;
    private final Map<String, byte[]> checksums = new HashMap<>();
    
    
    public UpdateServer() {
        super(5, Protocol.UPDATE);
        init();
    }
    
    @Override
    protected ServerSocket createSocket() throws IOException {
        return new ServerSocket(Constants.PORT_SERVER, 8, InetAddress.getLocalHost());
    }
    
    private void init() {
        if(!Resources.US_GAMEFILES.exists()) {
            System.out.println("Packing gamefiles...");
            Collection<FileHandle> includedDirs = new HashSet<>();
            for(FileHandle f : Resources.GAMEFILES_DIRS)
                includedDirs.add(f);
            try {
                IOUtil.zip(Resources.DIR_APP, Resources.US_GAMEFILES, includedDirs, true);
            } catch(IOException e) {
                System.out.println("Couldn't zip gamefiles");
                Application.crashApplication(e);
                return;
            }
        }
        
        System.out.println("Cataloguing files...");
        
        markFile(Resources.DIR_UPDATE_SERVER);
        
        for(Map.Entry<String, byte[]> e : checksums.entrySet()) {
            System.out.println(e.getKey() + " - " + StringUtil.toHexString(e.getValue()));
        }
        
        System.out.println("Cataloguing complete");
    }
    
    private void markFile(FileHandle file) {
        if(!file.exists())
            return;
        if(file.isDirectory()) {
            for(FileHandle f : file.list()) {
                markFile(f);
            }
        } else {
            String localPath = Resources.relativiseUpdateServer(file);
            byte[] checksum = null;
            try {
                checksum = IOUtil.checksum(file);
            } catch(IOException e) {
                System.out.println("Couldn't calculate checksum for " + file);
                return;
            }
            fileList.add(localPath); // <-- fileListPacket's list
            checksums.put(localPath, checksum);
        }
    }
    
    @Override
    protected void onClientConnect(TCPConnection con) {
        System.out.println("Client connected: " + con);
        System.out.println("Sending file list");
        con.sendPacket(fileListPacket);
    }
    
    @Override
    protected void onClientDisconnect(TCPConnection con) {
        System.out.println("Client disconnected: " + con);
    }
    
    @Override
    public void handleChecksums(TCPConnection con, C000Checksums p) {
        System.out.println("Received client checksums");
        
        S001FileTransfer p2 = new S001FileTransfer();
        
        for(Map.Entry<String, byte[]> e : p.checksums.entrySet()) {
            String path = e.getKey();
            byte[] ourChecksum = checksums.get(path);
            if(ourChecksum == null)
                continue; // silly client sending invalid paths
            // If their checksum doesn't match ours, send them the file
            if(!Arrays.equals(ourChecksum, e.getValue())) {
                p2.files.add(path);
                // Gotta also give the unzip destination
                if(path.endsWith(".zip")) {
                    String unzipLoc = Resources.UNZIP_MAP.get(path);
                    if(unzipLoc == null)
                        unzipLoc = Resources.ROOT_DIR;
                    p2.files.add(unzipLoc);
                }
            }
        }
        
        System.out.println("Sending files... " + p2.files.size() + " files to send.");
        
        con.sendPacket(p2);
    }
    
}
