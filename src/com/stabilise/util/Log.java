package com.stabilise.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.stabilise.core.Resources;

/**
 * The Log class allows for the logging and more streamlined management of
 * console output messages.
 * 
 * <p>Instances of this class represent "logging agents", which may produce log
 * entries prepended with identifying tags.
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
	private static List<String> entries = new LinkedList<String>();
	
	/** A cache of the untagged logging agent to save on processor time. */
	private static final Log defaultAgent = new Log("");
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The tag with which to prefix log entries. (e.g. "<i>[SERVER] - </i>") */
	private String tag;
	/** Whether or not the logging agent is unmuted - that is, allowed to
	 * print all entries to the console. {@code true} by default. */
	private boolean unmuted = true;
	
	
	/**
	 * Creates a new log instance.
	 * 
	 * @param tag The tag with which to prefix log entries.
	 */
	private Log(String tag) {
		if(tag != null && tag != "")
			this.tag = "[" + tag.toUpperCase() + "] - ";
		else
			this.tag = "";
	}
	
	/**
	 * Posts an entry to the log.
	 * 
	 * @param message The message to post.
	 */
	public void logMessage(String message) {
		add(message, false);
	}
	
	/**
	 * Posts a critical entry to the log.
	 * 
	 * @param message The message to post.
	 */
	public void logCritical(String message) {
		add(message, true);
	}
	
	/**
	 * Posts a critical entry accompanied by a Throwable to the log.
	 * 
	 * @param message The message to post.
	 * @param t The Throwable to post.
	 * 
	 * @throws NullPointerException if {@code t} is {@code null}.
	 */
	public void logCritical(String message, Throwable t) {
		// Synchronise so this prints as a block.
		synchronized(entries) {
			logCritical(message);
			logThrowable(t);
		}
	}
	
	/**
	 * Posts a throwable to the log.
	 * 
	 * @param t The throwable to post.
	 * 
	 * @throws NullPointerException if {@code t} is {@code null}.
	 */
	public void logThrowable(Throwable t) {
		// Synchronise everything here since we want this to print as a block.
		// If this is not synchronised, a log entry from another thread could
		// be injected midway though a stack trace, which is never nice.
		synchronized(entries) {
			add(t.toString(), true);
			StackTraceElement[] exceptionStack = t.getStackTrace();
			for(StackTraceElement ste : exceptionStack) {
				add("    at " + ste.toString(), true);
			}
		}
	}
	
	/**
	 * Posts a series of booleans to the log, for use in debugging.
	 * 
	 * @param booleans The booleans to print.
	 */
	public void logBooleans(boolean... booleans) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < booleans.length; i++) {
			sb.append(Boolean.toString(booleans[i]));
			if(i < booleans.length - 1) sb.append(", "); 
		}
		add(sb.toString(), false);
	}
	
	/**
	 * Posts a series of integers to the log, for use in debugging.
	 * 
	 * @param ints The integers to print.
	 */
	public void logIntegers(int... ints) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < ints.length; i++) {
			sb.append(ints[i]);
			if(i < ints.length - 1) sb.append(", "); 
		}
		add(sb.toString(), false);
	}
	
	/**
	 * Posts a series of doubles to the log, for use in debugging.
	 * 
	 * @param doubles The doubles to print.
	 */
	public void logDoubles(double... doubles) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < doubles.length; i++) {
			sb.append(doubles[i]);
			if(i < doubles.length - 1) sb.append(", "); 
		}
		add(sb.toString(), false);
	}
	
	/**
	 * Adds a message to the log in a thread-safe manner.
	 * 
	 * @param message The message to add to the log.
	 * @param critical Whether or not the message is a critical one.
	 */
	private void add(String message, boolean critical) {
		message = new Date().toString() + " - " + tag + message;
		
		if(critical)
			System.err.println(message);
		else if(unmuted)
			System.out.println(message);
		
		synchronized(entries) {
			entries.add(critical ? "*" + message : message);
			
			if(entries.size() > LOG_CAPACITY)
				entries.remove(0);
		}
	}
	
	/**
	 * Mutes the logging agent - that is, prevents it from printing
	 * non-critical entries to the console.
	 * 
	 * @return The logging agent, for chaining operations.
	 */
	public Log mute() {
		unmuted = false;
		return this;
	}
	
	/**
	 * Unmutes the log - that is, allows it to print entries to the console.
	 * Note that a logging agent is unmuted by default.
	 */
	public void unmute() {
		unmuted = true;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets an untagged logging agent.
	 * 
	 * @return The default untagged logging agent.
	 */
	public static Log getAgent() {
		return defaultAgent;
	}
	
	/**
	 * Gets a logging agent.
	 * 
	 * @param tag The tag with which to prefix the agent's log entries.
	 * 
	 * @return A tagged logging agent.
	 */
	public static Log getAgent(String tag) {
		return new Log(tag);
	}
	
	/**
	 * Posts an entry to the default log.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logMessage(message)}</pre>
	 * 
	 * @param message The message to post.
	 */
	public static void message(String message) {
		defaultAgent.logMessage(message);
	}
	
	/**
	 * Posts a critical entry to the default log.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logCritical(message)}</pre>
	 * 
	 * @param message The message to post.
	 */
	public static void critical(String message) {
		defaultAgent.logCritical(message);
	}
	
	/**
	 * Posts a throwable accompanied by a critical entry to the default log.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logCritical(message, t)}</pre>
	 * 
	 * @param message The message to post.
	 * @param t The throwable to post.
	 * 
	 * @throws NullPointerException if {@code t} is {@code null}.
	 */
	public static void critical(String message, Throwable t) {
		defaultAgent.logCritical(message, t);
	}
	
	/**
	 * Posts a throwable to the default log.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logThrowable(t)}</pre>
	 * 
	 * @param t The throwable to post.
	 * 
	 * @throws NullPointerException if {@code t} is {@code null}.
	 */
	public static void throwable(Throwable t) {
		defaultAgent.logThrowable(t);
	}
	
	/**
	 * Posts a series of booleans to the default log, for use in debugging.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logBooleans(booleans)}</pre>
	 * 
	 * @param booleans The booleans to print.
	 */
	public static void booleans(boolean... booleans) {
		defaultAgent.logBooleans(booleans);
	}
	
	/**
	 * Posts a series of integers to the default log, for use in debugging.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logIntegers(ints)}</pre>
	 * 
	 * @param ints The integers to print.
	 */
	public static void integers(int... ints) {
		defaultAgent.logIntegers(ints);
	}
	
	/**
	 * Posts a series of doubles to the default log, for use in debugging.
	 * An invocation of this is equivalent to:
	 * <pre>{@code getAgent().logDoubles(doubles)}</pre>
	 * 
	 * @param doubles The doubles to print.
	 */
	public static void doubles(double... doubles) {
		defaultAgent.logDoubles(doubles);
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
		File logFile = new File(Resources.LOG_DIR, fileName + ".txt");
		saveLog(crashLog, prefixMessage, logFile);
	}
	
	/**
	 * Saves the most recent log entries to the file system.
	 * 
	 * @param crashLog Whether or not the log being saved is that of a crash.
	 * @param prefixMessage The message with which to prefix the log dump.
	 * @param file The file to which to save the log.
	 */
	public static void saveLog(boolean crashLog, String prefixMessage, File file) {
		synchronized(entries) {
			if(entries.size() == 0)
				return;
		}
		
		IOUtil.createParentDirQuietly(file);
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
			
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
				Iterator<String> i = entries.iterator();
				while(i.hasNext()) {
					writer.write(i.next());
					writer.newLine();
				}
			}
		} catch(IOException e) {
			// Well... this is a problem.
			critical("Could not save log!", e);
		} finally {
			try {
				if(writer != null)
					writer.close();
			} catch(IOException e) {
				// asdfghjkl
			}
		}
	}
	
}
