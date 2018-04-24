package com.stabilise.core.game;

import com.stabilise.util.collect.RingBuffer;


/**
 * In-game message history. Every game has one of them, usually with chat and
 * a console integrated. For now, however, all this does is record messages so
 * that they may be displayed if needed.
 */
public class Messages {
    
    /** A reference to the game, for tracking ticks. */
    private final Game game;
    
    private final RingBuffer<String> msgs = new RingBuffer<>(16);
    private long lastMessageSentAt = Long.MIN_VALUE;
    
    public Messages(Game game) {
        this.game = game;
    }
    
    /**
     * Sends a new message.
     */
    public void send(String msg) {
        msgs.push(msg);
        lastMessageSentAt = game.ticks;
    }
    
    /**
     * Returns the most recent message, or an empty string if no messages have
     * been sent.
     */
    public String getLastMsg() {
        String s = msgs.peekTail();
        return s == null ? "" : s;
    }
    
    /**
     * Returns the game tick at which the last message was posted.
     * 
     * @see Game#ticks
     */
    public long getLastMsgTick() {
        return lastMessageSentAt;
    }
    
}
