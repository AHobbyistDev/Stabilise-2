package com.stabilise.core;

import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.stabilise.core.state.State;
import com.stabilise.input.InputManager;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;

/**
 * An {@code Application} forms the basis of any program which seeks to utilise
 * {@link State} or {@link Screen} architecture. (Note that while
 * {@link InputManager} and {@link SoundManager} may be used independent of an
 * {@code Application}, this class automatically invokes all methods necessary
 * for their operation.) 
 * 
 * <p>This class oversees program execution by invoking the current state's
 * {@link State#update() update} method as many times per second equivalent to
 * {@code ticksPerSecond} (which is defined in the
 * {@link #Application(int) Application constructor}), and the current state's
 * {@link State#render() render} method as many times per second as the system
 * will allow, or as many times equivalent to the FPS cap, whichever is
 * smaller.
 * 
 * <p>In practice, a program's main class should extend {@code Application} as
 * such:
 * 
 * <pre>
 * public class MyProgram extends Application {
 *     public MyProgram() {
 *         super(60); // Arbitrary example value	
 *     }
 *     
 *     &#64;Override
 *     protected Screen getScreen() {
 *         return ScreenLWJGL.get("My Program");
 *     }
 *     
 *     &#64;Override
 *     protected State getInitialState() {
 *         return new MyState(); // Implementor of State; does whatever
 *     }
 * }</pre>
 * 
 * <p>To run the program, simply construct a new instance as per {@code new
 * MyProgram()}, and that instance will control your program henceforth. Note
 * that any code placed after such a construction will never be executed.
 * 
 * <p>A program's actual logic should be located in the initial {@code State}
 * and other states which it may set.
 * 
 * <p>An Application is not thread-safe and should only be interacted with on
 * the thread that created it.
 */
public abstract class Application {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The application. */
	private static Application instance;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The listener which delegates to this Application. */
	private final Listener listener;
	
	/** The application's state. */
	protected State state;
	
	/** The screen dimensions. */
	private int width, height;
	
	/** Whether or not the application is running. */
	private boolean running = false;
	/** Whether or not the application has been shut down. */
	private boolean stopped = false;
	/** Whether or not the application is crashing. */
	private boolean crashing = false;
	
	/** The application's input manager. */
	protected final InputManager input;
	
	/** The number of update ticks executed per second. */
	protected final int ticksPerSecond;
	/** Nanoseconds per update tick. */
	private final long nsPerTick;
	/** The time when last it was checked as per {@code System.nanoTime()}. */
	private long lastTime;
	/** The number of 'unprocessed' nanoseconds. An update tick is executed
	 * when this is greater than or equal to nsPerTick. */
	private long unprocessed = 0L;
	/** The number of updates which have been executed in the lifetime of the
	 * application. */
	private long numUpdates = 0L;
	
	/** The Application's profiler. It is disabled by default.
	 * <p>This profiler is flushed automatically; to use it, simply invoke
	 * {@code start}, {@code next} and {@code end} where desired. */
	public final Profiler profiler = new Profiler(false, "root");
	
	
	/**
	 * Creates the Application.
	 * 
	 * @param ticksPerSecond The number of update ticks per second.
	 * 
	 * @throws IllegalStateException if an Application is already running (i.e.
	 * an Application instance has already been created).
	 * @throws IllegalArgumentException if {@code ticksPerSecond < 1}.
	 */
	protected Application(int ticksPerSecond) {
		if(instance != null)
			throw new IllegalStateException("The application has already been created!");
		if(ticksPerSecond < 1)
			throw new IllegalArgumentException("ticksPerSecond < 1");
		
		instance = this;
		
		this.ticksPerSecond = ticksPerSecond;
		nsPerTick = 1000000000L / ticksPerSecond;
		
		listener = new Listener(this);
		
		input = InputManager.get();
		
		state = getInitialState();
		if(state == null)
			crash(new NullPointerException("Initial state is null"));
		
		// Try to have the application shut down nicely in all cases of non-
		// standard closure
		/*
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// If this hook thread starts once everything else has stopped,
				// there's no need to synchronise on anything
				instance.shutdown();
			}
		});
		*/
	}
	
	/**
	 * Gets the initial state of the Application.
	 * 
	 * <p>If the returned state is {@code null}, the application will crash.
	 */
	protected abstract State getInitialState();
	
	/**
	 * Creates the Application - delegated from {@link Listener#create()}.
	 */
	private void create() {
		if(!stopped) {
			init();
			state.start();
			lastTime = System.nanoTime();
			running = true;
		}
	}
	
	/**
	 * Initiates the application.
	 * 
	 * <p>This is called when the application is first created, and may be used
	 * to perform any preemptive tasks before the initial state is started.
	 * 
	 * <p>The initial state has {@link State#start() start()} invoked on it
	 * immediately after this method returns.
	 * 
	 * <p>This method is not abstract as not all applications may with to do
	 * anything here, but it may be overridden at will.
	 */
	protected void init() {
		// nothing in the default implementation
	}
	
	/**
	 * Resizes the application.
	 * 
	 * @param width The new width of the application, in pixels.
	 * @param height The new height of the application, in pixels.
	 */
	private void resize(int width, int height) {
		this.width = width;
		this.height = height;
		state.resize(width, height);
	}
	
	/**
	 * Executes the main loop.
	 */
	private void mainLoop() {
		// Don't let the main loop run until the application has been properly
		// created.
		if(!running)
			return;
		
		long now = System.nanoTime();
		unprocessed += (now - lastTime);
		
		// Make sure nothing has gone wrong with timing
		if(unprocessed > 5000000000L) { // 5 seconds
			Log.critical("Can't keep up! Application is running "
					+ TimeUnit.NANOSECONDS.toMillis(now - lastTime) + " milliseconds behind; skipping " 
					+ (unprocessed / nsPerTick) + " ticks!"
			);
			unprocessed = 0L;
		} else if(unprocessed < 0L) {
			Log.critical("Time ran backwards! Did the timer overflow?");
			unprocessed = 0L;
		}
		
		lastTime = now;
		
		try {
			profiler.start("update"); // start update
			boolean updated = false;
			
			// Perform any scheduled update ticks
			while(unprocessed >= nsPerTick) {
				numUpdates++;
				unprocessed -= nsPerTick;
				updated = true;
				
				tick();
			}
			
			profiler.end(); // end update
			
			// Flush the profiler, but only if at least one update tick
			// occurred.
			if(updated)
				profiler.flush();
			
			// Rendering
			profiler.start("render");
			state.render(Gdx.graphics.getDeltaTime());
			profiler.end();
		} catch(Throwable t) {
			crash(t);
		}
	}
	
	/**
	 * Executes an update tick. This method is invoked a number of times per
	 * second equivalent to {@link #ticksPerSecond} specified in the
	 * Application constructor.
	 * 
	 * <p>Note that this may be overridden to add any state-independent update
	 * logic.
	 */
	protected void tick() {
		input.update();		// Input detecting
		state.update();		// All application logic as implemented by the current state
	}
	
	/**
	 * Pauses the application.
	 * 
	 * <p>Also invoked before {@link #shutdown()} when the application is shut
	 * down, thanks to libGDX.
	 */
	private void pause() {
		input.unpressButtons();
		input.unpressKeys();
		if(state != null)
			state.pause();
	}
	
	/**
	 * Resumes the application.
	 */
	private void resume() {
		if(state != null)
			state.resume();
	}
	
	/**
	 * Disposes the application. This executes the actual shutdown code, as
	 * this is the method routed to by libGDX ({@link #shutdown()} delegates
	 * to the libGDX shutdown as to follow proper procedure).
	 */
	private void dispose() {
		stopped = true;
		
		// try..catch since client code cannot be trusted
		try {
			if(state != null)
				state.dispose();
		} catch(Throwable t) {
			crash(t);
		}
	}
	
	/**
	 * Schedules a shutdown of the application. The current state will have its
	 * {@link State#pause() pause()} and {@link State#dispose() dispose()}
	 * methods invoked once shutdown commences.
	 */
	public final void shutdown() {
		if(stopped)
			return;
		stopped = true;
		running = false;
		
		Gdx.app.exit();
	}
	
	/**
	 * Crashes the application.
	 * 
	 * <p>The application will attempt to shutdown as per an invocation of
	 * {@link #shutdown()}, however it should be noted that depending on the
	 * state of the application, doing so may not be possible.
	 * 
	 * @param t The Throwable to treat as the cause of the crash.
	 */
	public final void crash(Throwable t) {
		if(crashing)		// TODO: Possibly append a log entry detailing the
			return;			// double-crash and re-save the log?
		crashing = true;
		Log.critical("The application has crashed!", t);
		produceCrashLog();
		shutdown();
	}
	
	/**
	 * Saves the crash log.
	 */
	protected void produceCrashLog() {
		Log.saveLog(true, "");
	}
	
	/**
	 * Gets a reference to the Application's state.
	 * 
	 * @return The Application's current state object.
	 */
	public final State getState() {
		return state;
	}
	
	/**
	 * Sets the application's state. The following operations will occur in
	 * order:
	 * 
	 * <ul>
	 * <li>{@link State#start() start} is invoked on the new state.
	 * <li>{@link State#resize(int, int) resize} is invoked on the new state.
	 * <li>{@link State#dispose() dispose} is invoked on the old state.
	 * <li>The old state will be discarded and should be garbage collected.
	 * </ul>
	 * 
	 * @param state The application's new state.
	 * 
	 * @throws NullPointerException if {@code state} is {@code null}.
	 */
	public final void setState(State state) {
		if(state == null)
			throw new NullPointerException("state is null!");
		state.start();
		state.resize(width, height);
		this.state.dispose();
		this.state = state;
	}
	
	/**
	 * @return The application window's width.
	 */
	public final int getWidth() {
		return width;
	}
	
	/**
	 * @return The application window's height.
	 */
	public final int getHeight() {
		return height;
	}
	
	/**
	 * @return The number of updates which have been executed during the
	 * lifetime of the application.
	 */
	public final long getUpdateCount() {
		return numUpdates;
	}
	
	/**
	 * This method is a wrapper for {@code Gdx.graphics.getFrameId()}.
	 * 
	 * @return The number of frames rendered during the lifetime of the
	 * application.
	 */
	public final long getRenderCount() {
		return Gdx.graphics.getFrameId();
	}
	
	/**
	 * Gets the ApplicationListener linked to this application for libGDX to
	 * link to.
	 * 
	 * @return This application's backing ApplicationListener.
	 */
	public final ApplicationListener getListener() {
		return listener;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets a reference to the Application.
	 * 
	 * @return The application instance, or {@code null} if an Application is
	 * not running.
	 */
	public static Application get() {
		return instance;
	}
	
	/**
	 * Crashes the Application. An invocation of this is equivalent to:
	 * <pre>Application.get().crash(t);</pre>
	 * 
	 * @param t The throwable to treat as the cause of the crash.
	 * 
	 * @throws NullPointerException if an Application is not running (i.e., if
	 * {@link #get()} would return {@code null}).
	 * @see Application#crash(Exception)
	 */
	public static void crashApplication(Throwable t) {
		instance.crash(t);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * An implementation of ApplicationListener which delegates to private
	 * methods in {@link Application}.
	 */
	private static class Listener implements ApplicationListener {
		
		/** The target application. */
		private final Application app;
		
		
		/**
		 * Creates a wrapping listener for the specified application.
		 */
		public Listener(Application app) {
			if(app == null)
				throw new NullPointerException("How did you manage to make app null?");
			this.app = app;
		}
		
		@Override
		public void create() {
			app.create();
		}
		
		@Override
		public void resize(int width, int height) {
			app.resize(width, height);
		}
		
		@Override
		public void render() {
			app.mainLoop();
		}
		
		@Override
		public void pause() {
			app.pause();
		}
		
		@Override
		public void resume() {
			app.resume();
		}
		
		@Override
		public void dispose() {
			app.dispose();
		}
		
	}
	
}
