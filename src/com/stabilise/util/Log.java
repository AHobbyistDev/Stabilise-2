package com.stabilise.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.util.collect.LightweightLinkedList;

/**
 * The Log class allows for the logging and more streamlined management of
 * console output messages.
 * 
 * <p>Instances of this class represent "logging agents", which may produce log
 * entries prepended with identifying tags.
 * 
 * <p>The default {@link #setLogLevel(Level) output level} is {@link
 * Level#INFO}.
 * 
 * <p>This class and its instances are thread-safe, and hence may be used by
 * multiple threads.
 * 
 * <!-- TODO: Would it not be more effective to funnel System.out into a file?
 * -->
 */
public class Log {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The maximum number of messages the log will store before wiping older
	 * entries. */
	private static final int LOG_CAPACITY = 256;
	
	/** Stores the log entries. Access to this list should be synchronised. */
	private static final List<String> entries = new LightweightLinkedList<String>();
	
	/** A cache of the untagged logging agent to save on processor time. */
	private static final Log defaultAgent = new Log("");
	
	/** The date. TODO: Would it be better to create Date objects on the fly? */
	private static final Date DATE = new Date();
	
	/**
	 * A log level is used to indicate granularity to which log messages will
	 * be posted.
	 */
	public static enum Level {
		/** Not valid as a message type. Setting the log's output level to this
		 * means no messages will be printed.  */
		NONE(0, true, null),
		/** A severe message - often an error. Setting the log's output level
		 * to this means only severe messages will be printed. */
		SEVERE(1, false, "[SEVERE] - ") {
			@Override
			protected void print(String msg) {
				System.err.println(msg);
			}
		},
		/** A warning message - not necessarily an error. Setting the log's
		 * output level to this means severe and warning messages will be
		 * printed. */
		WARNING(2, false, "[WARNING] - ") {
			@Override
			protected void print(String msg) {
				System.err.println(msg);
			}
		},
		/** An ordinary message. Setting the log's output level to this means
		 * severe, warning and info messages will be printed. */
		INFO(3, false, "[INFO] - "),
		/** A debug message - often granular and specific. Setting the log's
		 * output level to this means severe, warning, info and debug messages
		 * will be printed. */
		DEBUG(4, false, "[DEBUG1] - "),
		/** A fine debug message - granular and specific. Setting the log's
		 * output level to this means severe, warning, info, debug and fine
		 * debug messages will be printed. */
		FINE_DEBUG(5, false, "[DEBUG2] - "),
		/** Not valid as a message type. Setting the log's output level to this
		 * means all messages will be printed. */
		ALL(6, true, null);
		
		/** This level's value; used for comparison. */
		private final int value;
		/** Whether this may only be used as the print level of a log, and not
		 * a message type. */
		private final boolean printLevelOnly;
		/** This level's message tag. */
		private final String tag;
		
		private Level(int value, boolean printLevelOnly, String tag) {
			this.value = value;
			this.printLevelOnly = printLevelOnly;
			this.tag = tag;
		}
		
		/**
		 * @throws IllegalArgumentException if this level may not be used as
		 * a message level.
		 */
		private void checkAllowable() {
			if(printLevelOnly)
				throw new IllegalArgumentException("Invalid message level");
		}
		
		/**
		 * Prints the message if able.
		 * 
		 * @param logLevel The log's output level.
		 * @param msg The message.
		 */
		private void printIfAble(Level logLevel, String msg) {
			if(value <= logLevel.value)
				print(msg);
		}
		
		/**
		 * Prints the specified message to this level's output stream.
		 */
		protected void print(String msg) {
			System.out.println(msg);
		}
	}
	
	/** The log level. */
	private static volatile Level logLevel = Level.INFO;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The tag with which to prefix log entries. (e.g. "<i>[SERVER] - </i>") */
	private String tag;
	
	
	/**
	 * Creates a new log instance.
	 * 
	 * @param tag The tag with which to prefix log entries.
	 */
	private Log(String tag) {
		if(tag != null && tag != "")
			this.tag = "[" + tag + "] - "; // formerly tag.toUpperCase()
		else
			this.tag = "";
	}
	
	/**
	 * Posts a message to the log.
	 * 
	 * @param level The level of the message.
	 * @param msg The message.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IllegalArgumentException if the given level may not be used as
	 * a message level.
	 */
	public void post(Level level, String msg) {
		level.checkAllowable();
		synchronized(entries) {
			DATE.setTime(System.currentTimeMillis());
			add(level, logLevel, DATE.toString() + " - " + level.tag + tag + msg);
			if(entries.size() > LOG_CAPACITY)
				entries.remove(0);
		}
	}
	
	/**
	 * Posts a message accompanied by a throwable to the log.
	 * 
	 * @param level The level of the message.
	 * @param msg The message.
	 * @param t The throwable.
	 * 
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if the given level may not be used as
	 * a message level.
	 */
	public void post(Level level, String msg, Throwable t) {
		level.checkAllowable();
		
		Level outputLevel = logLevel; // since it may be modified concurrently
		String prefix;
		StackTraceElement[] stackTrace = t.getStackTrace();
		
		synchronized(entries) {
			DATE.setTime(System.currentTimeMillis());
			prefix = DATE.toString() + " - " + level.tag + tag;
			add(level, outputLevel, prefix + msg);
			add(level, outputLevel, prefix + t.toString());
			prefix += "    at ";
			for(StackTraceElement e : stackTrace)
				add(level, outputLevel, prefix + e.toString());
			while(entries.size() > LOG_CAPACITY)
				entries.remove(0);
		}
	}
	
	/**
	 * Adds a message to the list of entries, then prints it to the console if
	 * able.
	 * 
	 * @param level The message level.
	 * @param outputLevel The log's output level.
	 * @param msg The message.
	 */
	private void add(Level level, Level outputLevel, String msg) {
		entries.add(msg);
		level.printIfAble(outputLevel, msg);
	}
	
	/**
	 * Posts a severe message to the log, as per {@link #post(Level, String)
	 * post(Level.SEVERE, msg)}.
	 * 
	 * @throws NullPointerException if {@code msg} is {@code null}.
	 */
	public void postSevere(String msg) {
		post(Level.SEVERE, msg);
	}
	
	/**
	 * Posts a severe message accompanied by a throwable to the log, as per
	 * {@link #post(Level, String, Throwable) post(Level.SEVERE, msg, t)}.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public void postSevere(String msg, Throwable t) {
		post(Level.SEVERE, msg, t);
	}
	
	/**
	 * Posts a warning message to the log, as per {@link #post(Level, String)
	 * post(Level.WARNING, msg)}.
	 * 
	 * @throws NullPointerException if {@code msg} is {@code null}.
	 */
	public void postWarning(String msg) {
		post(Level.WARNING, msg);
	}
	
	/**
	 * Posts a warning message accompanied by a throwable to the log, as per
	 * {@link #post(Level, String, Throwable) post(Level.WARNING, msg, t)}.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public void postWarning(String msg, Throwable t) {
		post(Level.WARNING, msg, t);
	}
	
	/**
	 * Posts an info message to the log, as per {@link #post(Level, String)
	 * post(Level.INFO, msg)}.
	 * 
	 * @throws NullPointerException if {@code msg} is {@code null}.
	 */
	public void postInfo(String msg) {
		post(Level.INFO, msg);
	}
	
	/**
	 * Posts an info message accompanied by a throwable to the log, as per
	 * {@link #post(Level, String, Throwable) post(Level.INFO, msg, t)}.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public void postInfo(String msg, Throwable t) {
		post(Level.INFO, msg, t);
	}
	
	/**
	 * Posts a debug message to the log, as per {@link #post(Level, String)
	 * post(Level.DEBUG, msg)}.
	 * 
	 * @throws NullPointerException if {@code msg} is {@code null}.
	 */
	public void postDebug(String msg) {
		post(Level.DEBUG, msg);
	}
	
	/**
	 * Posts a debug message accompanied by a throwable to the log, as per
	 * {@link #post(Level, String, Throwable) post(Level.DEBUG, msg, t)}.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public void postDebug(String msg, Throwable t) {
		post(Level.DEBUG, msg, t);
	}
	
	/**
	 * Posts a fine debug message to the log, as per {@link
	 * #post(Level, String) post(Level.FINE_DEBUG, msg)}.
	 * 
	 * @throws NullPointerException if {@code msg} is {@code null}.
	 */
	public void postFineDebug(String msg) {
		post(Level.FINE_DEBUG, msg);
	}
	
	/**
	 * Posts a fine debug message accompanied by a throwable to the log, as per
	 * {@link #post(Level, String, Throwable) post(Level.FINE_DEBUG, msg, t)}.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public void postFineDebug(String msg, Throwable t) {
		post(Level.FINE_DEBUG, msg, t);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * @return The untagged default logging agent.
	 */
	public static Log get() {
		return defaultAgent;
	}
	
	/**
	 * Gets a logging agent.
	 * 
	 * @param tag The tag with which to prefix the agent's log entries. May be
	 * {@code null}.
	 * 
	 * @return A tagged logging agent.
	 */
	public static Log getAgent(String tag) {
		return new Log(tag);
	}
	
	/**
	 * Sets the log's output level.
	 * 
	 * @throws NullPointerException if {@code level} is {@code null}.
	 */
	public static void setLogLevel(Level level) {
		if(level == null)
			throw new NullPointerException();
		logLevel = level;
	}
	
	/**
	 * Saves the most recent log entries to the file system.
	 * 
	 * @param crashLog Whether or not the log being saved is that of a crash.
	 * @param prefixMessage The message with which to prefix the log dump.
	 */
	public static void saveLog(boolean crashLog, String prefixMessage) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
		if(crashLog)
			fileName += " [CRASH]";
		FileHandle logFile = Resources.LOG_DIR.child(fileName + ".txt");
		saveLog(crashLog, prefixMessage, logFile);
	}
	
	/**
	 * Saves the most recent log entries to the file system.
	 * 
	 * @param crashLog Whether or not the log being saved is that of a crash.
	 * @param prefixMessage The message with which to prefix the log dump.
	 * @param file The file to which to save the log.
	 */
	public static void saveLog(boolean crashLog, String prefixMessage, FileHandle file) {
		synchronized(entries) {
			if(entries.isEmpty())
				return;
		}
		
		IOUtil.createParentDir(file);
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(file.writer(true));
			
			if(crashLog) {
				writer.write("==========Begin crash log==========");
				writer.newLine();
				writer.newLine();
			}
			
			if(prefixMessage != null && prefixMessage != "") {
				writer.write(prefixMessage);
				writer.newLine();
				writer.newLine();
			}
			
			synchronized(entries) {
				for(String entry : entries) {
					writer.write(entry);
					writer.newLine();
				}
			}
		} catch(IOException e) {
			// Well... this is a problem.
			get().postSevere("Could not save log!", e);
		} finally {
			try {
				if(writer != null)
					writer.close();
			} catch(IOException e) {
				get().postSevere("asdfghjkl", e);
			}
		}
	}
	
}
