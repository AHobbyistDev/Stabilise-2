package com.stabilise.util.concurrent;

import java.util.concurrent.Future;

/**
 * Unifying intergace for {@code Future} and {@code Tracker}.
 */
public interface TrackableFuture<V> extends Future<V>, Tracker {
	
}
