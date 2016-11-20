package com.stabilise.network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.network.protocol.PacketHandler;
import com.stabilise.network.protocol.Protocol;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.ThreadSafeMethod;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventHandler;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * A TCPConnection instance maintains a connection between a server and a
 * client, and is the gateway for interaction between the two.
 * 
 * <p>A TCPConnection object has two threads associated with it, for managing
 * the input and output streams of the associated socket. The activity of these
 * threads is proportional to the amount of data traffic.
 * 
 * <p>A TCPConnection may not reconnect if it is closed; a new one must be
 * created.
 */
@NotThreadSafe
public class TCPConnection {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** Posted when a TCPConnection synchronises protocols with its peer. */
    public static final ProtocolSyncEvent EVENT_PROTOCOL_SYNC
            = new ProtocolSyncEvent(null, null);
    /** Posted when a TCPConnection is opened. */
    public static final Event EVENT_OPENED = new TCPEvent("connectionOpened");
    /** Posted when a TCPConnection is closed. */
    public static final Event EVENT_CLOSED = new TCPEvent("connectionClosed");
    
    
    /** State values.
     * 
     * <p>{@code STARTING} is an intermediate state used only while a
     * TCPConnection object is being constructed.
     * <p>{@code ACTIVE} indicates that a connection is active.
     * <p>{@code CLOSE_REQUESTED} indicates that either the read or write
     * thread encountered an exception and shut down.
     * <p>{@code SHUTDOWN} indicates that a connection is shutting down.
     * <p>{@code TERMINATED} indicates that a connection has been terminated. */
    private static enum State {
            STARTING,
            ACTIVE,
            CLOSE_REQUESTED,
            SHUTDOWN,
            TERMINATED;
    }
    
    private static final AtomicInteger CONNECTIONS_SERVER = new AtomicInteger(0);
    private static final AtomicInteger CONNECTIONS_CLIENT = new AtomicInteger(0);
    
    /** Number of ms between consecutive ping requests. */
    private static final long PING_INTERVAL = 1000;
    /** Number of ms before a connection disconnects automatically due to
     * large ping. */
    private static final long TIMEOUT_PING = 30_000L; // 30 seconds
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** {@code true} if this is a server-side connection; {@code false} if
     * client-side. */
    public final boolean server;
    
    /** The current connection protocol. When this is changed via {@link
     * #setProtocol(Protocol)} we send out a packet to tell our peer, to
     * ensure it doesn't try to handle packets we send across the new protocol
     * while it is still using the old one. */
    private Protocol protocol;
    /** Protocol being used by the read thread. This updates when our peer
     * sends us a {@link P254ProtocolSwitch} packet. This is only ever modified
     * by the read thread, and NOT the main thread. */
    private Protocol readThreadProtocol;
    /** Protocol being used by the write thread. This updates when we send a
     * {@link P254ProtocolSwitch} packet. This is only ever modified by the
     * write thread, and NOT the main thread. */
    private Protocol writeThreadProtocol;
    /** The protocol being used by our peer. This is basically the same as
     * {@link #readThreadProtocol}, but this is updated by the main thread when
     * the protocol switch packet is handled, rather than read. This is null
     * until initially synced. */
    private Protocol peerProtocol = null;
    /** Whether or not packets have been initially synced. While this is false,
     * we allow packets to be queued via {@link #sendPacket(Packet)}, even
     * though we haven't synced with our peer to avoid unpredictable packet
     * dropping, and then flush all queued packets once we've synced. */
    private boolean hasInitiallySynced = false;
    
    private final AtomicReference<State> state = new AtomicReference<>(State.STARTING);
    
    /** Stateful events are retained (e.g. connection close) for convenience,
     * and in the case of this class may be posted concurrently. */
    private final EventDispatcher eventsStateful = EventDispatcher.concurrentRetained();
    private final EventDispatcher eventsNormal = EventDispatcher.concurrentNormal();
    
    protected final Socket socket;
    
    private final DataInStream in;
    private final DataOutStream out;
    
    private final BlockingDeque<Packet> packetQueueIn  = new LinkedBlockingDeque<>();
    private final BlockingDeque<Packet> packetQueueOut = new LinkedBlockingDeque<>();
    private final List<Packet> syncQueue = new ArrayList<>();
    
    private final TCPReadThread readThread;
    private final TCPWriteThread writeThread;
    
    private volatile int packetsSent = 0;
    private volatile int packetsReceived = 0;
    
    /** Number of pings sent to the connection partner. */
    private int pingCount = 0;
    /** Number of pings sent by the partner to us. */
    private int partnerPingCount = 0;
    /** System.currentTimeMillis() at which we sent our most recent ping. */
    private long pingSent = 0L;
    /** true if we've received the rebound packet for our ping request. If
     * false we don't send pings to avoid overlapping requests. */
    private boolean pingReceived = true;
    /** Ping, in ms. Updated every second, or after the last ping request
     * returns, whichever is later. */
    private int ping = -1;
    
    private volatile String disconnectReason = "";
    
    final Log log;
    
    
    /**
     * Creates a new TCPConnection.
     * 
     * @param socket The socket upon which to base the connection.
     * @param server Whether or not this is a server-side connection.
     * @param initialProtocol The starting protocol to use.
     * 
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IOException if the connection could not be established.
     */
    public TCPConnection(Socket socket, boolean server, Protocol initialProtocol) throws IOException {
        this.socket = Objects.requireNonNull(socket);
        this.server = server;
        this.protocol = Objects.requireNonNull(initialProtocol);
        this.readThreadProtocol = protocol;
        this.writeThreadProtocol = protocol;
        
        int id = server
                ? CONNECTIONS_SERVER.getAndIncrement()
                : CONNECTIONS_CLIENT.getAndIncrement();
        
        log = Log.getAgent((server ? "SERVER" : "CLIENT") + id);
        
        in = new DataInStream(new BufferedInputStream(//new InflaterInputStream(
                socket.getInputStream()));
        out = new DataOutStream(new BufferedOutputStream(//new DeflaterOutputStream(
                socket.getOutputStream()));
        
        readThread  = new TCPReadThread ((server ? "ServerReader" : "ClientReader") + id);
        writeThread = new TCPWriteThread((server ? "ServerWriter" : "ClientWriter") + id);
    }
    
    /**
     * Opens this connection.
     * 
     * @throws IllegalStateException if this connection has already been
     * opened.
     */
    void open() {
        if(!state.compareAndSet(State.STARTING, State.ACTIVE))
            throw new IllegalStateException("Already open!");
        
        readThread.start();
        writeThread.start();
        
        // Initial protocol sync
        sendPacket(new P254ProtocolSwitch(protocol));
    }
    
    /**
     * Performs an update tick on this TCPConnection. This method {@link
     * Packet#handle(PacketHandler, TCPConnection) handles} all packets
     * received since the last update, and ensures we ping our peer on a
     * per-second basis.
     * 
     * @param handler The PacketHandler with which to handle received packets.
     */
    void update(PacketHandler handler) {
        handleIncomingPackets(handler);
        
        if(pingSent == 0L) // initialise pingSent
            pingSent = System.currentTimeMillis() - PING_INTERVAL;
        long pingTime = System.currentTimeMillis() - pingSent;
        if(pingTime > TIMEOUT_PING) {
            requestClose("Timed out.");
            return;
        }
        // If we're not currently waiting for a ping response and it's been at
        // least 1 second since we sent our last ping request, send a new
        // request.
        if(pingTime > PING_INTERVAL) {
            if(pingReceived) {
                sendPacket(new P255Ping(++pingCount, true));
                pingSent = System.currentTimeMillis();
                pingReceived = false;
            } else {
                ping = (int)pingTime;
            }
        }
    }
    
    /**
     * Handles as many incoming packets as possible (i.e. until {@link
     * #getPacket()} returns {@code null}).
     * 
     * @param handler The handler with which to handle the packets.
     */
    private void handleIncomingPackets(PacketHandler handler) {
        for(Packet p; (p = getPacket()) != null;)
            // Only handle protocol-dependent packets while our protocols are
            // synced.
            if(areProtocolsSynced() || p.isUniversal())
                p.handle(handler, this);
    }
    
    /**
     * Handles a ping packet.
     */
    void handlePing(P255Ping packet) {
        if(packet.request) {
            if(++partnerPingCount != packet.pingID) {
                log.postWarning("Out of sync with peer's ping requests? We "
                        + "think peer has sent " + partnerPingCount + " ping"
                        + " requests; peer says it has sent " + packet.pingID
                        + ".");
                partnerPingCount = packet.pingID;
            }
            sendPacket(new P255Ping(partnerPingCount, false));
        } else {
            if(pingCount != packet.pingID) {
                log.postWarning("Peer out of sync with our ping requests? We "
                        + "have sent " + pingCount + " ping requests; peer "
                        + "thinks we have sent " + packet.pingID + ".");
            }
            ping = (int)(System.currentTimeMillis() - pingSent);
            pingReceived = true;
        }
    }
    
    /**
     * Returns this connection's ping to its peer, which is the approximate
     * time in ms required for a round trip of data. {@code -1} is returned if
     * we haven't yet pinged our peer.
     */
    public int getPing() {
        return ping;
    }
    
    /**
     * Returns the protocol currently being used by this connection.
     */
    public Protocol getProtocol() {
        return protocol;
    }
    
    /**
     * Sets this connection's protocol.
     * 
     * @throws NullPointerException if {@code protocol} is {@code null}.
     */
    public void setProtocol(Protocol protocol) {
        if(this.protocol != protocol) {
            sendPacket(new P254ProtocolSwitch(protocol));
            this.protocol = protocol;
            tryProtocolSync();
            
            // Force this to true to avoid the "queue anyway" concession, since
            // now it is known for sure that the protocols aren't synced
            hasInitiallySynced = true;
        }
    }
    
    /**
     * Handles a {@link P254ProtocolSwitch} packet, which tells us that our
     * peer has switched to a different protocol.
     */
    void handlePeerProtocolSwitch(Protocol peerProtocol) {
        if(this.peerProtocol == peerProtocol)
            return;
        this.peerProtocol = peerProtocol;
        tryProtocolSync();
    }
    
    /**
     * @return {@code true} if our protocol is synchronised with our peer's.
     */
    private boolean areProtocolsSynced() {
        return protocol == peerProtocol;
    }
    
    private void tryProtocolSync() {
        if(areProtocolsSynced()) {
            // On the initial sync, add all packets in the sync queue to the
            // real queue.
            if(!hasInitiallySynced) {
                hasInitiallySynced = true;
                packetQueueOut.addAll(syncQueue);
                syncQueue.clear();
            }
            
            eventsNormal.post(new ProtocolSyncEvent(this, protocol));
        } else {
            if(!hasInitiallySynced)
                log.postWarning("Our peer's initial protocol (" + peerProtocol
                        + ") is not the same as ours (" + protocol + ")! This "
                        + "will likely cause the connection to stall "
                        + "indefinitely as no packets can be sent!");
        }
    }
    
    /**
     * Queues a packet for sending.
     * 
     * @throws NullPointerException if {@code packet} is {@code null}.
     */
    @UserThread("MainThread")
    public void sendPacket(Packet packet) {
        boolean useSyncQueue = false;
        // Discard protocol-dependent packets until we synchronise protocols
        // with our peer. However, we allow packets queued before initial
        // syncing to be queued to avoid unpredictable packet cutoffs.
        if(!areProtocolsSynced() && !packet.isUniversal()) {
            if(!hasInitiallySynced)
                useSyncQueue = true;
            else
                return;
        }
        
        if(server) {
            if(!protocol.isServerPacket(packet)) {
                log.postWarning("Attempting to send a non-server packet ("
                        + packet + ") across protocol " + protocol);
                return;
            }
        } else {
            if(!protocol.isClientPacket(packet)) {
                log.postWarning("Attempting to send a non-client packet ("
                        + packet + ") across protocol " + protocol);
                return;
            }
        }
        
        if(useSyncQueue) {
            syncQueue.add(packet);
        } else {
            if(packet.isImportant())
                packetQueueOut.addFirst(packet);
            else
                packetQueueOut.addLast(packet);
        }
    }
    
    /**
     * Polls the input packet queue for the next packet.
     * 
     * @return The oldest queued packet, or {@code null} if the queue is empty.
     */
    @UserThread("MainThread")
    private Packet getPacket() {
        return packetQueueIn.poll();
    }
    
    /**
     * Reads a packet from the socket's input stream. A return value of {@code
     * false} indicates either an I/O exception or the end of the stream having
     * been reached.
     * 
     * @throws IOException if an I/O error occurs, or the stream has ended.
     * @throws FaultyPacketRegistrationException if the packet was registered
     * incorrectly (in this case the error lies in the registration code).
     */
    @UserThread("ReadThread")
    private void readPacket() throws IOException {
        Packet packet = readThreadProtocol.readPacket(server, in, log);
        if(packet == null)
            requestClose("End of stream.");
        else if(packet != Packet.DUMMY_PACKET) {
            // If the read thread encounters a protocol switch packet, it means
            // our peer will be sending through that protocol henceforth, so
            // we'll switch to using that protocol.
            if(packet instanceof P254ProtocolSwitch)
                readThreadProtocol = ((P254ProtocolSwitch)packet).protocol;
            
            if(packet.isImportant())
                packetQueueIn.addFirst(packet);
            else
                packetQueueIn.addLast(packet);
            packetsReceived++;
        }
    }
    
    /**
     * Writes a packet to the socket's output stream. A return value of {@code
     * false} indicates that the packet queue was empty when polled.
     * 
     * @return {@code true} if the packet was sent; {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    @UserThread("WriteThread")
    private boolean writePacket() throws IOException {
        Packet packet = packetQueueOut.poll();
        if(packet == null)
            return false;
        doWritePacket(packet);
        return true;
    }
    
    /**
     * Writes a packet to the socket's output stream, blocking if necessary
     * until a packet is available to send.
     * 
     * @throws InterruptedException if the current thread is interrupted while
     * waiting for a packet.
     * @throws IOException if an I/O error occurs.
     */
    @UserThread("WriteThread")
    private void writePacketWithBlock() throws InterruptedException, IOException {
        
        doWritePacket(packetQueueOut.take());
    }
    
    @UserThread("WriteThread")
    private void doWritePacket(Packet packet) throws IOException {
        // Once the write thread encounters a protocol switch packet, we
        // update this thread's view of the protocol.
        if(packet instanceof P254ProtocolSwitch)
            writeThreadProtocol = ((P254ProtocolSwitch)packet).protocol;
        writeThreadProtocol.writePacket(out, packet);
        packetsSent++;
    }
    
    /**
     * Requests for this connection to close, if an error occurs.
     * 
     * @throws NullPointerException if {@code reason} is {@code null}.
     */
    @UserThread({"ReadThread", "WriteThread"})
    private void requestClose(String reason) {
        disconnectReason = Objects.requireNonNull(reason);
        state.compareAndSet(State.ACTIVE, State.CLOSE_REQUESTED);
    }
    
    /**
     * Closes this connection and releases any resources held by it.
     * 
     * <p>This method does nothing if this connection has already been closed.
     */
    @ThreadSafeMethod
    public void closeConnection() {
        if(!state.compareAndSet(State.ACTIVE, State.SHUTDOWN) &&
                !state.compareAndSet(State.CLOSE_REQUESTED, State.SHUTDOWN))
            return;
        
        if(disconnectReason.equals(""))
            disconnectReason = "Disconnected.";
        
        log.postInfo("Closing connection... reason: " + disconnectReason);
        
        readThread.interrupt();
        writeThread.interrupt();
        
        close(in, "input stream");
        close(out, "output stream");
        close(socket, "socket");
        
        // On second thought, don't bother joining these threads since it
        // causes unnecessary delays.
        //readThread.doJoin();
        //writeThread.doJoin();
        
        state.set(State.TERMINATED);
        
        log.postInfo("Connection closed; "
                + packetsSent + (packetsSent == 1 ? " packet" : " packets")
                + " sent (" + out.size() + " bytes), "
                + packetsReceived + (packetsReceived == 1 ? " packet" : " packets")
                + " received.");
        
        eventsStateful.post(EVENT_CLOSED);
    }
    
    /**
     * Closes a Closeable and ignores thrown IOExceptions.
     */
    private void close(Closeable closeable, String identifier) {
        try {
            closeable.close();
        } catch(IOException e) {
            //log.postWarning("IOException while closing " + identifier + " ("
            //        + e.getMessage() + ")");
        }
    }
    
    @Override
    protected void finalize() {
        // Abuse finalisers to ensure a connection is closed properly.
        closeConnection();
    }
    
    /**
     * Returns {@code true} if this connection is active; {@code false}
     * otherwise. Even if this returns {@code false}, it is important that you
     * invoke {@link #closeConnection()} to properly terminate this connection
     * (since it is possible for a connection to exist in a state where it is
     * inactive but still not terminated).
     */
    public boolean isActive() {
        return state.get() == State.ACTIVE;
    }
    
    /**
     * @return {@code true} if this connection is terminated; {@code false}
     * otherwise. If this connection is neither active nor terminated, it is
     * either currently in the process of terminating, or needs to be
     * terminated via {@link #closeConnection()}.
     */
    public boolean isTerminated() {
        return state.get() == State.TERMINATED;
    }
    
    /**
     * Returns a user-friendly reason for why this connection has disconnected.
     */
    public String getDisconnectReason() {
        return disconnectReason;
    }
    
    /**
     * Adds an event listener to this connection.
     * 
     * @param exec The executor with which to execute the handler.
     * @param event The event to listen for.
     * @param handler The handler to invoke when the specified event is posted.
     * 
     * @throws NullPointerException if any argument is null.
     * @see #EVENT_OPENED
     * @see #EVENT_CLOSED
     * @see #EVENT_PROTOCOL_SYNC
     */
    public <E extends Event> void addListener(Executor exec, E event,
            EventHandler<? super E> handler) {
        if(event == EVENT_CLOSED || event == EVENT_OPENED)
            eventsStateful.addListener(exec, event, handler);
        else
            eventsNormal.addListener(exec, event, handler);
    }
    
    @Override
    public String toString() {
        return "TCPConnection[Connected to:" + socket.getInetAddress() +
                ", state:" + state.get() +
                (isTerminated() ? ", d/c reason:" + disconnectReason : "") + "]";
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Base implementation for TCP read/write threads. Provides an uncaught
     * exception handler which invokes {@link TCPConnection#requestClose()},
     * and the {@link #doJoin()} method.
     */
    private abstract class TCPThread extends Thread {
        
        public TCPThread(String threadName) {
            super(threadName);
            
            setUncaughtExceptionHandler((t, e) -> {
                log.postSevere("Uncaught exception " + e.getClass().getSimpleName()
                        + " in " + t.getName(), e);
                requestClose("Uncaught " + e.getClass().getSimpleName()
                        + ": " + e.getMessage());
            });
        }
        
        /**
         * Invokes join() on this thread and silently ignores an
         * InterruptedException if it is thrown.
         */
        @SuppressWarnings("unused")
        public void doJoin() {
            try {
                join();
            } catch(InterruptedException e) {
                log.postWarning("Interrupted while waiting for " + getName()
                        + " to die!");
            }
        }
    }
    
    /**
     * The TCPWriteThread class handles the reading operations from a
     * TCPConnection's input stream.
     */
    private class TCPReadThread extends TCPThread {
        
        public TCPReadThread(String threadName) {
            super(threadName);
        }
        
        @Override
        public void run() {
            try {
                while(isActive())
                    readPacket();
            } catch(IOException e) {
                // An IOException being thrown is a standard part of the
                // shutdown procedure (as shutting down the socket will cause
                // any in-progress IO operation to fail). However, if an
                // IOException is thrown outside of shutdown procedures,
                // something has gone wrong.
                if(isActive()) {
                    if(!socket.isClosed())
                        log.postSevere("IOException thrown in read thread before"
                                + " connection shutdown!", e);
                    requestClose(e.getClass() + ": " + e.getMessage());
                }
            }
        }
        
    }
    
    /**
     * The TCPWriteThread class handles the writing operations to a TCPConnection's
     * output stream.
     */
    private class TCPWriteThread extends TCPThread {
        
        public TCPWriteThread(String threadName) {
            super(threadName);
        }
        
        @Override
        public void run() {
            /*
             * The writing strategy is simple:
             * 1. Block until we receive a packet, and write it.
             * 2. Keep writing packets until the queue is emptied.
             * 3. Flush the output stream.
             * 4. Repeat.
             * 
             * This strategy has some nice advantages:
             * a) We only wait when we've caught up to the packet producer
             *    thread, and thus keep up with demand.
             * b) Packets are (hopefully) batched nicely enough that we don't
             *    flush too often.
             */
            try {
                while(isActive()) {
                    writePacketWithBlock(); // wait for a packet
                    while(writePacket()) {} // empty the queue
                    out.flush();            // flush the batch of packets
                }
            } catch(InterruptedException | IOException e) {
                // An IOException being thrown is a standard part of the
                // shutdown procedure (as shutting down the socket will cause
                // any in-progress IO operation to fail). However, if an
                // IOException is thrown outside of shutdown procedures,
                // something has gone wrong. Ditto with InterruptedException.
                if(isActive()) {
                    if(!socket.isClosed())
                        log.postSevere(e.getClass().getSimpleName() + " thrown in write "
                                + "thread before connection shutdown!");
                    requestClose(e.getClass() + ": " + e.getMessage());
                }
            }
        }
        
    }
    
    /**
     * Custom class for events to prevent cheap duplication.
     */
    private static class TCPEvent extends Event {
        public TCPEvent(String name) {
            super(name);
        }
    }
    
    /**
     * A ProtocolSyncEvent is an event type which is posted when a {@code
     * TCPConnection} synchronises protocols with its peer.
     */
    public static class ProtocolSyncEvent extends TCPEvent {
        
        public final TCPConnection con;
        public final Protocol protocol;
        
        private ProtocolSyncEvent(TCPConnection con, Protocol protocol) {
            super("protocolSync");
            this.con = con;
            this.protocol = protocol;
        }
        
    }
    
}
