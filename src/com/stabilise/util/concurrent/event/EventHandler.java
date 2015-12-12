package com.stabilise.util.concurrent.event;

import java.util.function.Consumer;


public interface EventHandler<E extends Event> extends Consumer<E> {
    
}
