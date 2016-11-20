package com.stabilise.core.state;

import com.stabilise.core.app.Application;

/**
 * States allow for the circumstantial execution of the application's logic.
 * 
 * <p>The application's current state, in essence, determines and controls what
 * the application does.
 * 
 * @see Application
 */
public interface State {
    
    /**
     * Called when the state is started.
     * 
     * <p>If there is an old state, this method is invoked after {@link
     * #predispose()} is invoked on the old state, and before {@link
     * #dispose()} is invoked on it.
     */
    void start();
    
    /**
     * Called when the state is stopped and disposed, and before {@link
     * #start()} is invoked on the new state, if one has been set.
     * 
     * <p>Between this and {@link #dispose()}, used resources should be
     * disposed of an any necessary cleanups should be performed here, as to
     * prevent any memory leaks. This operation should ideally be a swift
     * one.
     * 
     * <p>The need for a distinction between {@code predispose()} and {@code
     * dispose()} emerges from the fact that some cached resources may be
     * shared across states - when a new state is set, such resources may be
     * marked for caching by the new state before they are unmarked by the old
     * state in {@code dispose()}. Conversely, a new state may only be able to
     * obtain a resource once the old state has first released it in {@code
     * predispose()}.
     */
    void predispose();
    
    /**
     * Called when the state is stopped and disposed, and after {@link
     * #start()} is invoked on the new state, if one has been set.
     * 
     * <p>Between this and {@link #predispose()}, used resources should be
     * disposed of an any necessary cleanups should be performed here, as to
     * prevent any memory leaks. This operation should ideally be a swift
     * one.
     * 
     * <p>The need for a distinction between {@code predispose()} and {@code
     * dispose()} emerges from the fact that some cached resources may be
     * shared across states - when a new state is set, such resources may be
     * marked for caching by the new state before they are unmarked by the old
     * state in {@code dispose()}. Conversely, a new state may only be able to
     * obtain a resource once the old state has first released it in {@code
     * predispose()}.
     */
    void dispose();
    
    /**
     * Called when the application is resized. This is also called when a state
     * is set as the application's current state, immediately after {@link
     * #start()}.
     * 
     * @param width The new application width, in pixels.
     * @param height The new application height, in pixels.
     */
    void resize(int width, int height);
    
    /**
     * Called when the application is paused.
     */
    void pause();
    
    /**
     * Called when the application is resumed.
     */
    void resume();
    
    /**
     * Called when the state should update.
     */
    void update();
    
    /**
     * Called when the state should render.
     * 
     * @param delta The time since the last render, in seconds.
     */
    void render(float delta);
    
}
