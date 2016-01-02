package com.stabilise.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.app.Application;
import com.stabilise.network.Client;
import com.stabilise.network.TCPConnection;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.network.protocol.update.C000Checksums;
import com.stabilise.network.protocol.update.IClientUpdate;
import com.stabilise.network.protocol.update.S000LauncherJar;
import com.stabilise.network.protocol.update.S001GameJar;
import com.stabilise.network.protocol.update.S002GameFiles;
import com.stabilise.network.protocol.update.S003Confirmation;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

/**
 * A simple update client which connects to an {@link UpdateServer} over LAN to
 * update the game files.
 */
public class UpdateClient extends Client implements IClientUpdate {
    
    private static final InetAddress LAN;
    
    static {
        try {
            LAN = InetAddress.getLocalHost();
        } catch(UnknownHostException e) {
            throw new Error(e);
        }
    }
    
    public static final Event evtGotLauncher = new Event("gotLauncher");
    public static final Event evtGotGame = new Event("gotGame");
    public static final Event evtGotGamefiles = new Event("gotGamefiles");
    public static final Event evtGameReady = new Event("gameReady");
    
    
    private DataCompound versionData;
    private boolean needLauncher = true;
    private boolean needGame = true;
    private boolean needFiles = true;
    
    private EventDispatcher events = EventDispatcher.concurrentRetained();
    
    
    public UpdateClient() {
        super(LAN, Constants.SERVER_PORT, Protocol.UPDATE);
    }
    
    @Override
    protected void handleProtocolSwitch(TCPConnection con, Protocol protocol) {
        if(protocol == Protocol.UPDATE) {
            loadVersionData();
            System.out.println("Sending checksums");
            C000Checksums p = new C000Checksums();
            p.launcherChecksum = versionData.getByteArr("launcherChecksum");
            p.gameChecksum = versionData.getByteArr("gameChecksum");
            p.gamefilesChecksum = versionData.getByteArr("gamefilesChecksum");
            con.sendPacket(p);
        }
    }
    
    private void loadVersionData() {
        try {
            versionData = IOUtil.read(Format.NBT, Compression.UNCOMPRESSED,
                    Resources.DIR_GAMEDATA.child("versiondata.json"));
        } catch(IOException e) {
            System.out.println("Could not load version data! Initialising as empty...");
            versionData = DataCompound.create();
        }
    }
    
    /**
     * Registers an event listener.
     * 
     * @see #evtGotLauncher
     * @see #evtGotGame
     * @see #evtGotGamefiles
     * @see #evtGameReady
     */
    public void addListener(Event event, Runnable listener) {
        events.addListener(Tasks.currentThreadExecutor(), event, e -> listener.run());
    }
    
    @Override
    public void handleConfirmation(TCPConnection con, S003Confirmation p) {
        needLauncher = p.launcherNeedsUpdate;
        needGame = p.gameNeedsUpdate;
        needFiles = p.gameFilesNeedUpdate;
    }
    
    @Override
    public void handleLauncherJar(TCPConnection con, S000LauncherJar p) {
        needLauncher = false;
    }
    
    @Override
    public void handleGameJar(TCPConnection con, S001GameJar p) {
        needGame = false;
    }
    
    @Override
    public void handleGameFiles(TCPConnection con, S002GameFiles p) {
        needFiles = false;
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
