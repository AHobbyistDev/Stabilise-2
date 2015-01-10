package com.stabilise.core;

import java.util.Random;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.stabilise.core.state.State;
import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;

/**
 * An {@code Application} is designed to form the basis of any program which
 * seeks to utilise a more advanced variant of the libGDX application
 * architecture.
 * 
 * <p>In practice, a program's main class should extend {@code Application} as
 * such:
 * 
 * <pre>
 * public class MyProgram extends Application {
 *     public MyProgram() {
 *         super(60); // Arbitrary TPS value	
 *     }
 *     
 *     &#64;Override
 *     protected State getInitialState() {
 *         return new MyState(); // Implementor of State; does whatever
 *     }
 * }</pre>
 * 
 * <p>To use an application, simply pass its {@code ApplicationListener} to the
 * libGDX application class in a manner similar to:
 * 
 * <pre>new MyProgram().getListener()</pre>
 * 
 * <p>e.g. For a desktop application, the code may be similar to:
 * 
 * <pre>
 * LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
 * new LwjglApplication(new MyProgram().getListener(), config);
 * </pre>
 * 
 * <p>An Application is not thread-safe and should only be interacted with on
 * the main thread (i.e. the one through which the Application itself invokes
 * the current state's methods).
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
	
	private final InternalAppDriver driver;
	
	/** The listener which delegates to this Application. */
	private final InternalAppListener listener;
	/** The lifecycle listener which we'll use to catch the pause()-dispose()
	 * combo when the application is shut down before the main listener catches
	 * it. */
	private final InternalLCListener lcListener;
	
	/** The application's state. */
	protected State state;
	
	/** {@code true} if the application has been shut down. */
	private boolean stopped = false;
	/** {@code true} if the application is crashing. */
	private boolean crashing = false;
	
	/** The application profiler. It is disabled by default.
	 * <p>This profiler is flushed automatically once per second; to use it,
	 * simply invoke {@code start}, {@code next} and {@code end} where desired. */
	public final Profiler profiler;
	
	private final Log log;
	
	
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
		
		// It shouldn't matter that we're publishing this instance before it
		// has been constructed as this should be the starting point (or
		// effective starting point) for an application anyway.
		instance = this;
		
		log = Log.getAgent("Application");
		driver = new InternalAppDriver(ticksPerSecond, ticksPerSecond, log);
		profiler = driver.profiler;
		
		listener = new InternalAppListener();
		lcListener = new InternalLCListener();
		
		state = getInitialState();
		if(state == null)
			crash(new NullPointerException("Initial state is null"));
		
		// Not needed for libgdx I think
		/*
		// Try to have the application shut down nicely in all cases of non-
		// standard closure
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
	 * Creates the Application - delegated from {@link
	 * InternalAppListener#create()}.
	 */
	private void create() {
		if(!stopped) {
			Gdx.app.addLifecycleListener(lcListener);
			driver.running = true;
			init();
			state.start();
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
	 * <p>This method does nothing in the default implementation and may be
	 * optionally overridden.
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
	 */
	private void mainLoop() {
		// Don't let the main loop run until the application has been properly
		// created.
		if(!driver.running)
			return;
		
		try {
			driver.tick();
		} catch(Throwable t) {
			crash(t);
		}
	}
	
	/**
	 * Executes an update tick. This method is invoked a number of times per
	 * second equivalent to {@link #ticksPerSecond} specified in the
	 * Application constructor. The current state has {@link State#update()
	 * update()} invoked immediately after this method is invoked.
	 * 
	 * <p>This method does nothing in the default implementation and may be
	 * optionally overridden.
	 */
	protected void tick() {
		// nothing in the default implementation
	}
	
	/**
	 * Pauses the application.
	 */
	private void pause() {
		if(!stopped && state != null)
			state.pause();
	}
	
	/**
	 * Resumes the application.
	 */
	private void resume() {
		if(!stopped && state != null)
			state.resume();
	}
	
	/**
	 * Disposes the application. This executes the actual shutdown code, as
	 * this is the method routed to by libGDX ({@link #shutdown()} delegates
	 * to the libGDX shutdown as to follow proper procedure).
	 */
	private void dispose() {
		stopped = true;
		driver.stop();
		
		// try..catch since client code cannot be trusted
		try {
			if(state != null) {
				state.predispose();
				state.dispose();
			}
		} catch(Throwable t) {
			crash(t);
		}
	}
	
	/**
	 * Schedules a shutdown of the application. The current state will have its
	 * {@link State#predispose() predispose()} and {@link State#dispose()
	 * dispose()} methods invoked once shutdown commences.
	 */
	public final void shutdown() {
		if(stopped)
			return;
		
		stopped = true;
		driver.stop();
		
		Gdx.app.exit(); // will result in pause() then dispose() being invoked
	}
	
	/**
	 * Crashes the application.
	 * 
	 * <p>The application will attempt to shutdown as per an invocation of
	 * {@link #shutdown()}, however it should be noted that depending on the
	 * state of the application, doing so may not be possible.
	 * 
	 * @param t The Throwable to treat as the cause of the crash. If {@code
	 * null}, a dummy exception is constructed to provide a stack trace to the
	 * log.
	 */
	public final void crash(Throwable t) {
		if(crashing)		// TODO: Possibly append a log entry detailing the
			return;			// double-crash and re-save the log?
		crashing = true;
		if(t == null)
			log.postSevere("The application has crashed!", new Exception(dummyExceptionMessage()));
		else
			log.postSevere("The application has crashed!", t);
		produceCrashLog();
		shutdown();
	}
	
	private String dummyExceptionMessage() {
		String[] msgs = new String[] {
				"I am here to provide a stack trace",
				"Stack trace buddy!",
				"All your stack trace are belong to us"
		};
		return msgs[new Random().nextInt(msgs.length)];
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
	 * Sets the application's state. This method performs the following
	 * operations in order:
	 * 
	 * <ul>
	 * <li>{@link State#predispose() predispose()} is invoked on the old state.
	 * <li>{@link State#start() start()} is invoked on the new state.
	 * <li>{@link State#resize(int, int) resize()} is invoked on the new state.
	 * <li>{@link State#dispose() dispose()} is invoked on the old state.
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
		this.state.predispose();
		state.start();
		state.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.state.dispose();
		this.state = state;
	}
	
	/**
	 * @return The number of updates which have been executed during the
	 * lifetime of the application.
	 */
	public final long getUpdateCount() {
		return driver.getUpdateCount();
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
	 * The implementation of AppDriver which powers an application.
	 */
	private class InternalAppDriver extends AppDriver {
		
		public InternalAppDriver(int tps, int fps, Log log) {
			super(tps, fps, log);
		}
		
		@Override
		protected void update() {
			Application.this.tick();
			Application.this.state.update();
		}
		
		@Override
		protected void render() {
			Application.this.state.render(Gdx.graphics.getDeltaTime());
		}
		
	}
	
	/**
	 * An implementation of ApplicationListener which delegates to private
	 * methods in {@link Application}.
	 */
	private class InternalAppListener implements ApplicationListener {
		
		@Override public void create() {
			Application.this.create();
		}
		
		@Override public void resize(int width, int height) {
			Application.this.resize(width, height);
		}
		
		@Override public void render() {
			Application.this.mainLoop();
		}
		
		@Override public void pause() {
			Application.this.pause();
		}
		
		@Override public void resume() {
			Application.this.resume();
		}
		
		@Override public void dispose() {
			Application.this.dispose();
		}
		
	}
	
	/**
	 * When the libgdx application shuts down, for some reason it invokes
	 * pause() then dispose() on the main ApplicationListener, which is kind
	 * of inconvenient if the user doesn't want to execute all the pause logic
	 * on a shutdown. As such, by attaching this lifecycle listener to Gdx.app,
	 * we can catch the pause()-dispose() combo before the main listener does,
	 * and tell it to ignore the pause().
	 */
	private class InternalLCListener implements LifecycleListener {
		@Override public void pause() {}
		@Override public void resume() {}
		
		@Override
		public void dispose() {
			Application.this.stopped = true;
		}
		
	}
	
}
