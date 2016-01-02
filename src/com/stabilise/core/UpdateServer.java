package com.stabilise.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.app.Application;
import com.stabilise.network.Server;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.update.C000Checksums;
import com.stabilise.network.protocol.update.IServerUpdate;
import com.stabilise.network.protocol.update.S000LauncherJar;
import com.stabilise.network.protocol.update.S001GameJar;
import com.stabilise.network.protocol.update.S002GameFiles;
import com.stabilise.network.protocol.update.S003Confirmation;
import com.stabilise.util.io.IOUtil;

/**
 * Hosts a simple update server to distribute game files over LAN.
 */
public class UpdateServer extends Server implements IServerUpdate {
    
    public static void main(String[] args) {
        Server server = new UpdateServer();
        server.runConcurrently();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
    
    
    private byte[] launcherChecksum;
    private byte[] gameChecksum;
    private byte[] filesChecksum;
    
    
    public UpdateServer() {
        super(5, Protocol.UPDATE);
        init();
    }
    
    @Override
    protected ServerSocket createSocket() throws IOException {
        return new ServerSocket(Constants.SERVER_PORT, 8, InetAddress.getLocalHost());
    }
    
    private void init() {
        if(!Resources.US_GAMEFILES.exists()) {
            System.out.println("Packing gamefiles...");
            Collection<FileHandle> includedDirs = new HashSet<>();
            for(FileHandle f : Resources.GAMEFILES_DIRS)
                includedDirs.add(f);
            try {
                IOUtil.zip(Resources.DIR_APP, Resources.US_LAUNCHER_JAR, includedDirs, true);
            } catch(IOException e) {
                System.out.println("Couldn't zip gamefiles");
                Application.crashApplication(e);
                return;
            }
        }
        
        try {
            System.out.println("Calculating checksums...");
            launcherChecksum = IOUtil.checksum(Resources.US_LAUNCHER_JAR);
            gameChecksum = IOUtil.checksum(Resources.US_GAME_JAR);
            filesChecksum = IOUtil.checksum(Resources.US_GAMEFILES);
        } catch(IOException e) {
            System.out.println("Could not get checksums!");
            Application.crashApplication(e);
            return;
        }
    }
    
    @Override
    protected void onClientConnect(TCPConnection con) {
        System.out.println("Client connected: " + con);
    }
    
    @Override
    protected void onClientDisconnect(TCPConnection con) {
        System.out.println("Client disconnected: " + con);
    }
    
    @Override
    public void handleChecksums(TCPConnection con, C000Checksums p) {
        boolean sendLauncher = !Arrays.equals(launcherChecksum, p.launcherChecksum);
        boolean sendGame = !Arrays.equals(gameChecksum, p.gameChecksum);
        boolean sendFiles = !Arrays.equals(filesChecksum, p.gamefilesChecksum);
        
        S003Confirmation c = new S003Confirmation();
        c.launcherNeedsUpdate = sendLauncher;
        c.gameNeedsUpdate = sendGame;
        c.gameFilesNeedUpdate = sendFiles;
        con.sendPacket(c);
        
        if(sendLauncher)
            con.sendPacket(new S000LauncherJar());
        if(sendGame)
            con.sendPacket(new S001GameJar());
        if(sendFiles)
            con.sendPacket(new S002GameFiles());
    }
    
}
