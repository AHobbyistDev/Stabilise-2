package com.stabilise.util.concurrent.event;

import java.util.function.Consumer;


/**
 * An {@code EventHandler} is an {@link Event}-accepting {@code Consumer} which
 * is used by {@link EventDispatcher}.
 */
public interface EventHandler<E extends Event> extends Consumer<E> {
    
}
