package com.stabilise.core;

import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.ApplicationListener;
import com.stabilise.core.state.State;
import com.stabilise.input.InputManager;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.annotation.UsedByApplication;
import com.stabilise.util.annotation.UsesApplication;

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
 * that any code placed after such a construction will never be executed. For
 * example:
 * 
 * <pre>
 * public static void main(String[] args) {
 *     new MyProgram();
 *     doSomethingElse(); // This will never be invoked!
 * }</pre>
 * 
 * <p>A program's actual logic should be located in the initial {@code State}
 * and other states which it may set.
 * 
 * <p>An Application is not thread-safe and should only be interacted with on
 * the thread that created it.
 */
@UsedByApplication
@UsesApplication
public abstract class Application {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The application. */
	private static Application instance;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The number of update ticks executed per second. */
	protected final int ticksPerSecond;
	
	/** The listener which delegates to this Application. */
	private final Listener listener;
	
	/** The application's state. */
	protected State state;
	/** The new state, which is to be set at the end of the current tick, if
	 * it is non-null. */
	private State newState = null;
	
	/** Whether or not the application is currently running. Setting this to
	 * {@link false} aborts the main loop and shuts down the Application as per
	 * an invocation of {@link #shutdown()}. */
	protected boolean running = true;
	/** Whether or not the application has been shut down. */
	private boolean stopped = false;
	/** Whether or not the application is crashing. */
	private boolean crashing = false;
	
	/** The application's input manager. */
	protected final InputManager input;
	
	/** The time when last it was checked as per {@code System.nanoTime()}. */
	private long lastTime = System.nanoTime();
	/** The number of 'unprocessed' nanoseconds. An update tick is executed
	 * when this is greater than or equal to nsPerTick. */
	private long unprocessed = 0L;
	/** Nanoseconds per update tick. */
	private final long nsPerTick;
	
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
	 * Gets the initial state of the Application. The returned {@code State}
	 * will be set as the Application's state as the Application is
	 * constructed.
	 * 
	 * @return The State.
	 */
	protected abstract State getInitialState();
	
	/**
	 * Creates the Application - delegated from {@link Listener#create()}.
	 */
	private void create() {
		init();
		state.start();
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
		state.resize(width, height);
	}
	
	/**
	 * Executes the main loop.
	 * The main game loop that is in control of the execution of all of the
	 * application's logic.
	 * 
	 * <p>As long as {@code running} is {@code true}, {@link #tick()} will be
	 * invoked a number of times per second equivalent to
	 * {@link #ticksPerSecond}, and
	 * {@link Screen#preRender() screen.preRender()},
	 * {@link State#render state.render()} &
	 * {@link Screen#postRender() screen.postRender()} will
	 * be invoked as many times per second as the system will allow, or as
	 * many times equivalent to the FPS cap, whichever is smaller.
	 * 
	 * <p>If a Throwable is thrown during the execution of any of these
	 * methods, {@link #crash(Throwable)} will be invoked and the Application
	 * will abort.
	 */
	private void mainLoop() {
		long now = System.nanoTime();
		unprocessed += (now - lastTime);
		
		// Make sure nothing has gone wrong with timing
		if(unprocessed > 5000000000L) { // 5 seconds
			Log.critical("Can't keep up; running "
					+ TimeUnit.NANOSECONDS.toMillis(now - lastTime) + " milliseconds (" 
					+ (unprocessed / nsPerTick) + " ticks) behind! "
					+ "Is the application overloaded?");
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
				// Swap out states if necessary
				if(newState != null) {
					newState.start();
					state.dispose();
					state = newState;
					newState = null;
				}
				
				tick();
				unprocessed -= nsPerTick;
				updated = true;
			}
			
			profiler.end(); // end update
			
			// Flush the profiler, but only if at least one update tick
			// occurred.
			if(updated)
				profiler.flush();
			
			// Rendering
			profiler.start("render"); // begin render
			
			//profiler.start("preRender");
			//screen.preRender();
			//profiler.start("render");
			state.render();
			//profiler.next("postRender");
			//screen.postRender();
			//profiler.end();
			
			profiler.end(); // end render
		} catch(Throwable t) {
			crash(t);
			return;
		}
		
		/*
		// If the loop has been aborted but the application has yet to stop,
		// stop it
		if(!stopped)
			shutdown();
		*/
	}
	
	/**
	 * Executes an update tick. This method is invoked a number of times per
	 * second equivalent to {@link #ticksPerSecond} by the main application
	 * loop.
	 * 
	 * <p>Note that this may be overridden to add any state-independent update
	 * logic.
	 */
	protected void tick() {
		input.update();		// Input detecting
		state.update();		// All application logic as implemented by the current state
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
	 * Sets the application's state.
	 * 
	 * <p>The current state, if it is non-null, will have its
	 * {@link State#dispose() stop()} method invoked and the new state will have its
	 * {@link State#start() start()} method invoked at the start of the next
	 * tick, after which the current state will be discarded and allowed to be
	 * garbage collected.
	 * 
	 * @param state The application's new state.
	 * 
	 * @throws NullPointerException if {@code state} is {@code null}.
	 */
	public final void setState(State state) {
		if(state == null)
			throw new NullPointerException("state is null!");
		newState = state;
	}
	
	/**
	 * Pauses the application. This is invoked automatically by the active
	 * {@code Screen} when the application window loses focus, so there is
	 * typically no need to invoke this manually.
	 */
	private void pause() {
		input.unpressButtons();
		input.unpressKeys();
		if(state != null)
			state.pause();
	}
	
	/**
	 * Resumes the application. This is invoked automatically by the active
	 * {@code Screen} when the application window regains focus, so there is
	 * typically no need to invoke this manually.
	 */
	private void resume() {
		if(state != null)
			state.resume();
	}
	
	/**
	 * Crashes the application.
	 * 
	 * <p>The application will attempt to shutdown as per an invocation of
	 * {@link #shutdown()}, however it should be noted that depending on the nature
	 * of the exception, doing so may not be possible.
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
	 * Shuts down the application. The current state will have its {@link
	 * State#dispose() stop()} method invoked, if it is non-null, and the sound
	 * manager will be cleaned up via {@link SoundManager#clean() clean()}.
	 * Finally, {@link System#exit(int)} is invoked, which shuts down the JVM
	 * and thereby the Application.
	 */
	public final void shutdown() {
		if(stopped)
			return;
		
		Log.message("Shutting down application...");
		
		stopped = true;
		running = false;
		
		// try..catch since the Application shouldn't trust client code
		try {
			if(state != null)
				state.dispose();
		} catch(Throwable t) {
			// Orchestrated in such a way that any chain of crash()-stop()
			// redirects all work out nicely
			crash(t);		
		}
		
		Log.message("Application shut down.");
		
		System.exit(0);			// Farewell, world!
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
	 * An implementation of ApplicationListener which delegates to methods in
	 * Application such that said methods in Application can avoid being
	 * public.
	 */
	private static class Listener implements ApplicationListener {
		
		/** The target application. */
		private final Application app;
		
		
		/**
		 * Creates a new listener for the specified application - never {@code
		 * null}.
		 */
		public Listener(Application app) {
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
			app.shutdown();
		}
		
	}
	
}
