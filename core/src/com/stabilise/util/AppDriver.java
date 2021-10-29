package com.stabilise.util;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.util.concurrent.Tasks;

/**
 * At the core of any game is a main loop, where we update the game state a
 * certain number of times per second (e.g. 60) and render a frame as many times
 * per second as is possible. This class does the job of ensuring the update and
 * render calls are timed appropriately from within the main loop, and in this
 * sense it <i>drives</i> the game or application.
 *
 * <p>The easiest way to get a main loop going is to invoke {@link #run()}.
 * Alternatively, if you already have a main loop, you can invoke either
 * {@link #tick()} or {@link #tickAndSleep()} from within that loop and the
 * AppDriver will take care of the rest.
 */
@NotThreadSafe
public final class AppDriver implements Runnable {
    
    /** Update ticks per second. */
    public final int tps;
    private final long nsPerTick; // nanos per tick
    
    /** Threshold value, in nanoseconds, of {@link #unprocessed} before we
     * reset its value and skip ticks. */
    private long delaySkip = 5_000_000_000L; // 5 seconds
    /** Force a render if catching up on update ticks takes longer than this.
     * This helps avoid perceived unresponsiveness if the update ticks take
     * far too much time. */
    private long forceRender = 500_000_000L; // 0.5 seconds
    
    /** The time when last it was checked as per {@code System.nanoTime()}. */
    private long lastTickStart = 0L;
    /** The number of 'unprocessed' nanoseconds. An update tick is executed
     * when this is greater than or equal to nsPerTick. */
    private long unprocessed = 0L;
    /** The number of updates and renders which have been executed in the 
     * lifetime of this driver. */
    private long numUpdates = 0L, numRenders = 0L;
    
    /** Maximum number of frames per second. 0 indicates no max, -1 indicates
     * no frames should be rendered. */
    private int maxFps;
    /** Current ongoing fps count. Reset every second. */
    private int fps = 0;
    /** Most recent completed fps count. Updated every second from {@link #fps}. */
    private int lastFps = 0;
    /** The most recent value of System.currentTimeMillis() at which lastFps
     * was updated. */
    private long lastFPSRefresh = System.currentTimeMillis();
    /** Nanoseconds per frame. */
    private long nsPerFrame;
    /** System.nanoTime() value when we ended the most recent render. */
    private long lastFrameEnd = 0L;
    /** The value most recently returned by {@link #tick()} (but in nanos). */
    private long lastSleep = 0L;
    
    /** {@code true} if this driver is running. You can set this to {@code
     * false} to stop this from running if it is being run as per {@link
     * #run()}. */
    public boolean running = false;
    /** {@code true} if a tick is in the process of executing. */
    private boolean ticking = false;
    
    /** This driver's profiler. It is disabled by default, and is configured to
     * reset on flush.
     * <p>This profiler is flushed automatically once per second; to use it,
     * simply invoke {@code start}, {@code next} and {@code end} where desired. */
    public final Profiler profiler = new Profiler(false, "root", true);
    private int ticksPerFlush;
    /** The last update at which the profiler was flushed. */
    private long lastProfilerFlush = 0L;
    
    private Log log = Log.get();
    
    private final Runnable updater;
    private final Runnable renderer;
    
    
    /**
     * Creates a new AppDriver.
     * 
     * @param tps The number of update ticks per second (also used as the FPS).
     * @param updater The runnable to run for each update tick. May be null.
     * @param renderer The runnable to run for each frame. May be null.
     * 
     * @throws IllegalArgumentException if {@code tps < 1}.
     * @throws NullPointerException if both {@code updater} and {@code
     * renderer} are {@code null}. Both being null makes an AppDriver pretty
     * pointless!
     */
    public AppDriver(int tps, Runnable updater, Runnable renderer) {
        this.tps = Checks.testMin(tps, 1);
        nsPerTick = 1_000_000_000 / tps;
        setMaxFPS(tps);
        this.log = log == null ? Log.get() : log;
        ticksPerFlush = tps;
        
        if(updater == null && renderer == null)
            throw new NullPointerException("Both updater and renderer null!");
        
        this.updater = updater == null ? Tasks.emptyRunnable() : updater;
        this.renderer = renderer == null ? Tasks.emptyRunnable() : renderer;
    }
    
    /**
     * Creates a new AppDriver.
     * 
     * @param tps The number of update ticks per second (also used as the FPS).
     * @param drivable The Drivable to update and render.
     * 
     * @throws IllegalArgumentException if {@code tps < 1}.
     * @throws NullPointerException if {@code drivable} is {@code null}.
     */
    public AppDriver(int tps, Drivable drivable) {
        this(tps, drivable::update, drivable::render);
    }
    
    /**
     * Initiates a loop which invokes {@link #tick()} up to as many times per
     * second as {@code fps} as specified in the constructor or by {@link
     * #setMaxFPS(int)}. This method will not return until either of the
     * following occurs:
     * 
     * <ul>
     * <li>{@link #running} is set to {@code false}; or, equivalently,
     * <li>{@link #stop()} is invoked; or
     * <li>An exception or error is thrown while executing {@code tick()}, in
     *     which case that exception/error will propagate through this method.
     * </ul>
     */
    @Override
    public void run() {
        running = true;
        lastTickStart = System.nanoTime();
        
        while(running) {
            tickAndSleep();
        }
    }
    
    /**
     * Attempts to execute a render pass and as many ticks as applicable, and
     * then causes the current thread to sleep for an appropriate amount of
     * time such that the framerate does not exceed {@link #getMaxFPS()
     * the max fps}.
     * 
     * <p>If the current thread was interrupted while sleeping, then the
     * interrupt flag will be set when this method returns.
     * 
     * @throws IllegalStateException if this is invoked while a tick is in
     * progress, or if client code has been improperly using the profiler.
     */
    public void tickAndSleep() {
        long sleepTime = tick();
        try {
            Thread.sleep(sleepTime);
        } catch(InterruptedException e) {
            log.postWarning("Interrupted while sleeping until next tick!");
            Thread.currentThread().interrupt(); // pass on the interrupt
        }
    }
    
    /**
     * Attempts to execute a render pass and as many ticks as applicable.
     * 
     * <p>If invoked at regular intervals (i.e. from within some kind of main
     * loop), this method ensures the updater is invoked as many times per
     * second equivalent to {@link #tps} as specified in the constructor.
     * Furthermore, the renderer is invoked every time this method is invoked.
     * invoked (unless the FPS is set to -1).
     * 
     * @return The number of milliseconds to wait until this should be invoked
     * again to ensure this is invoked as many times per second as specified by
     * {@link #getMaxFPS()}. This is used by {@link #run()} and {@link
     * #tickAndSleep()} and should otherwise probably be ignored.
     * @throws IllegalStateException if this is invoked while a tick is in
     * progress, or if client code has been improperly using the profiler.
     */
    public long tick() {
        if(ticking)
            throw new IllegalStateException("A tick is already in progress!");
        ticking = true;
        
        long now = System.nanoTime();
        if(lastTickStart == 0L) { // should be the case when this is first invoked
            lastTickStart = now;
            unprocessed = nsPerTick;
        }
        unprocessed += now - lastTickStart;
        lastTickStart = now;
        
        // Make sure nothing has gone wrong with timing.
        if(unprocessed >= delaySkip) {
            log.postWarning("Can't keep up! Running "
                    + (unprocessed / 1_000_000L) + " milliseconds behind; skipping "
                    + (unprocessed / nsPerTick) + " ticks."
            );
            unprocessed = nsPerTick; // let at least one tick happen
        } else if(unprocessed < 0L) {
            log.postWarning("Time ran backwards?!");
            unprocessed = 0L;
        }
        
        profiler.verify("root.wait");
        profiler.next("update"); // end wait, start update
        
        // Perform any scheduled update ticks
        while(unprocessed >= nsPerTick) {
            numUpdates++;
            unprocessed -= nsPerTick;
            updater.run();
            
            if(System.nanoTime() - now >= forceRender)
                break;
        }
        
        profiler.verify("root.update");
        profiler.end(); // end update
        
        // Flush the profiler every ticksPerFlush ticks
        if(numUpdates - lastProfilerFlush >= ticksPerFlush) {
            lastProfilerFlush = numUpdates;
            profiler.flush();
        } else if(numUpdates < lastProfilerFlush) {
            // numUpdates must have overflowed
            lastProfilerFlush = numUpdates;
        }
        
        // Rendering
        profiler.start("render");
        if(maxFps != -1) {
            renderer.run();
            numRenders++;
        }
        
        fps++;
        if(System.currentTimeMillis() - lastFPSRefresh >= 1000) {
            lastFps = fps;
            fps = 0;
            lastFPSRefresh = System.currentTimeMillis();
        }
        
        profiler.verify("root.render");
        profiler.next("wait");
        
        ticking = false;
        
        
        // We can't just sleep nsPerFrame - (System.nanoTime()-lastTickStart),
        // since we should account for time spent by whatever enclosing code
        // it is that invokes this function. So try to account for it by
        // subtracting off the difference between actual sleep time
        // (lastTickStart - lastFrameEnd) and the expected sleep time
        // (lastSleep). (This assumes the overhead of the enclosing code is the
        // same every tick). Then we get
        // 
        //   nsPerFrame - (System.nanoTime()-lastTickStart) - ( (lastTickStart-
        //      lastFrameEnd) - lastSleep )
        // = nsPerFrame - System.nanoTime() + lastFrameEnd + lastSleep.
        long newLastFrameEnd = System.nanoTime();
        lastSleep = nsPerFrame - newLastFrameEnd + lastFrameEnd + lastSleep;
        lastFrameEnd = newLastFrameEnd;
        if(lastSleep < 0)
            lastSleep = 0;
        return lastSleep / 1_000_000L; // convert to millis
        
        
        //long usedNanos = System.nanoTime() - lastTickStart;
        //lastTickStart = now;
        //if(usedNanos < nsPerFrame)
        //    return (nsPerFrame - usedNanos) / 1_000_000L; // convert to millis
        //else
        //    return 0L;
    }
    
    /**
     * Stops this driver from executing, if it is being run as per {@link
     * #run()} (this does nothing otherwise). This is equivalent to setting
     * {@link #running} to {@code false}.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * @return The number of update ticks which have been executed during the
     * lifetime of the application.
     */
    public long getUpdateCount() {
        return numUpdates;
    }
    
    /**
     * @return The number of renders which have been executed during the
     * lifetime of the application.
     */
    public long getRenderCount() {
        return numRenders;
    }
    
    /**
     * Gets the maximum FPS.
     * 
     * @return The max FPS. A value of {@code 0} indicates no maximum; a value
     * of {@code -1} means no frames will be rendered.
     */
    public int getMaxFPS() {
        return maxFps;
    }
    
    /**
     * Sets the maximum FPS. The default value is equal to the TPS as specified
     * in the constructor.
     * 
     * <p>The FPS value <b>does not matter</b> if {@link #run()} is not used.
     * 
     * @param fps The max FPS if this is run via {@link #run()}. A value of
     * {@code 0} indicates no maximum; a value of {@code -1} indicates not to
     * render (note this case will apply even if {@code run()} isn't used).
     * 
     * @throws IllegalArgumentException if {@code fps < -1}.
     * @return This AppDriver.
     */
    public AppDriver setMaxFPS(int fps) {
        this.maxFps = Checks.testMin(fps, -1);
        nsPerFrame = fps <= 0 ? 0 : 1_000_000_000L / fps;
        return this;
    }
    
    /**
     * Returns the current frames-per-second value. If this has not been set
     * manually it will be equal to {@link #tps}.
     */
    public int getFPS() {
        return lastFps;
    }
    
    /**
     * Sets the threshold delay time before updates are discarded - that is, if
     * cumulative delays from updates or renders exceeds the specified time,
     * this driver will not attempt to catch up on scheduled updates.
     * 
     * <p>The default value is 5 seconds.
     * 
     * @param time The delay threshold.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws IllegalArgumentException if, when converted to nanoseconds,
     * {@code time < 0}, or the given value is lower than the number of
     * nanoseconds per tick.
     * @return This AppDriver.
     */
    public AppDriver setDelaySkip(long time, TimeUnit unit) {
        time = unit.toNanos(time);
        if(time < 0)
            throw new IllegalArgumentException("Illegal time! In nanoseconds, "
                    + "its value is: " + time);
        if(time <= nsPerTick)
            throw new IllegalArgumentException("Illegal time; it is lower than "
                    + "nsPerTick.");
        this.delaySkip = time;
        return this;
    }
    
    /**
     * Sets the threshold delay time before a render is forced. If catching up
     * on update ticks takes too long then the application may appear to be
     * completely unresponsive since nothing new is being rendered in the
     * meantime. Thus, we will force a render if the time to execute a string
     * of updates takes longer than this time.
     * 
     * <p>The default value is 0.5 of a second.
     * 
     * @param time The force render threshold.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws IllegalArgumentException if, when converted to nanoseconds,
     * {@code time < 0}.
     * @return This AppDriver.
     */
    public AppDriver setForceRender(long time, TimeUnit unit) {
        time = unit.toNanos(time);
        if(time < 0)
            throw new IllegalArgumentException("Illegal time! In nanoseconds, "
                    + "its value is: " + time);
        this.delaySkip = time;
        return this;
    }
    
    /**
     * Sets the number of update ticks between which to flush the profiler.
     * 
     * @throws IllegalArgumentException if {@code ticksPerFlush < 1}.
     * @return This AppDriver.
     */
    public AppDriver setTicksPerProfilerFlush(int ticksPerFlush) {
        this.ticksPerFlush = Checks.testMin(ticksPerFlush, 1);
        return this;
    }
    
    /**
     * Sets this AppDriver's log. If null, the default log is used.
     * 
     * @return This AppDriver.
     */
    public AppDriver setLog(Log log) {
        this.log = log == null ? Log.get() : log;
        return this;
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Defines the methods {@code update} and {@code render}, which are invoked
     * by an AppDriver created by {@link #AppDriver(int, Drivable)}.
     */
    public interface Drivable {
        
        /**
         * Performs an update tick.
         * 
         * @see AppDriver#tick()
         */
        void update();
        
        /**
         * Performs any rendering.
         * 
         * @see AppDriver#tick()
         */
        void render();
        
    }
    
}
