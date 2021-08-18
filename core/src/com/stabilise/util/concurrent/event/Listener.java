package com.stabilise.util.concurrent.event;

import java.util.Objects;
import java.util.concurrent.Executor;


/**
 * This class encapsulates a registered event listener. Package-private.
 */
class Listener<E extends Event> {
    
    public final Executor executor;
    public final EventHandler<E> handler;
    /** Number of uses on this handler remaining; -1 == unlimited uses. */
    public long uses;
    
    
    /**
     * @throws NullPointerException if either executor or handler are null.
     */
    public Listener(Executor executor, EventHandler<E> handler, long uses) {
        this.executor = Objects.requireNonNull(executor);
        this.handler = Objects.requireNonNull(handler);
        this.uses = uses;
    }
    
    /**
     * @throws NullPointerException if either executor or handler are null.
     */
    public Listener(Executor executor, EventHandler<E> handler, boolean singleUse) {
        this(executor, handler, singleUse ? 1 : -1);
    }
    
    /**
     * Consumes a use from this listener. Returns true if this listener has no
     * more remaining uses and should be dropped.
     */
    public boolean consumeUse() {
        return uses != -1 && --uses == 0;
    }
    
    public final void execute(E e) {
        executor.execute(() -> handler.accept(e));
    }
    
    
}
