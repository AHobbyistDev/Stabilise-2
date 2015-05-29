package com.stabilise.util;

import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.stabilise.util.collect.LightLinkedList;

/**
 * A Profiler object is used to gauge the amount of processing time which is
 * being dedicated to various portions of a program.
 * 
 * <p>A Profiler operates through usage of 'sections', which operate similarly
 * to a file system's folder structure. To give a general idea how certain
 * invocations control these sections:
 * 
 * <pre>
 * new Profiler(true) // flush() is invoked on construction
 * > start("Section1")
 *     > start("Subsection1.1")
 *     > next("Subsection1.2")
 *         > start("Subsection 1.2.1")
 *         > end()
 *     > end()                  // <-- these two are equivalent to
 *     > start("Subsection1.3") // <-- next("Subsection1.3")
 *     > end()
 * > next("Section2")
 * > next("Section3")
 *     > start("Subsection3.1")
 *     > end()   
 * > end()</pre>
 * 
 * <p>Note that profiling can cause some overhead and it is as such advised to
 * disable a profiler when profiling is not required.
 * 
 * <p>A Profiler is not thread-safe.
 */
public class Profiler {
	
	/** The root section. */
	private Section root;
	/** The last root section. Data returned (e.g., by {@link #getData()} is
	 * that of the last root, and not the current one. */
	private Section lastRoot;
	
	/** Caches lastRoot's data in case it is requested multiple times. */
	private SectionData lastData;
	
	/** The stack of sections being profiled. Sections near the tail of this
	 * stack are constituents of sections closer to the head, with the first
	 * section being {@link #root}. */
	private final Deque<Section> stack = new LinkedList<>();
	
	/** Whether or not profiling is enabled. */
	private boolean enabled;
	/** Whether or not profiling should be treated as enabled. */
	private boolean effectivelyEnabled;
	
	/** {@code true} if the root section should be reset on flush. */
	private boolean resetOnFlush;
	
	
	/**
	 * Creates a new Profiler.
	 * 
	 * @param enabled The default 'enabled' status. This may be later changed
	 * via {@link #enable()} and {@link #disable()}.
	 * @param rootName The name of the root section.
	 * @param resetOnFlush Whether or not all profiling sections should be
	 * reset on an invocation of {@link #flush()}.
	 * 
	 * @throws NullPointerException if {@code rootName} is {@code null}.
	 */
	public Profiler(boolean enabled, String rootName, boolean resetOnFlush) {
		this.enabled = enabled;
		effectivelyEnabled = enabled;
		this.resetOnFlush = resetOnFlush;
		
		root = new Section(Objects.requireNonNull(rootName));
		if(enabled) {
			root.start();
			flush();
		} else {
			lastRoot = root;
		}
	}
	
	/**
	 * Starts a section. This section will be treated as a subsection of the
	 * current section (i.e. the one for which this method (or {@link
	 * #next(String) next}) was most recently invoked without a corresponding
	 * invocation of {@link #end()}).
	 * 
	 * <p>Every invocation of this should have a corresponding call to
	 * {@link #end()} after it.
	 * 
	 * <p>If such a section already exists, it will not be overwritten -
	 * rather, it will be added to.
	 * 
	 * @param section The name of the section.
	 * 
	 * @throws NullPointerException if {@code section} is {@code null}.
	 */
	public void start(String section) {
		if(effectivelyEnabled) {
			if(section == null)
				throw new NullPointerException("section is null");
			
			List<Section> curConstituents = stack.getLast().constituents;
			
			// Somewhat inefficient, but check to see if such a section already
			// exists.
			for(Section s : curConstituents) {
				if(s.name.equals(section)) {
					stack.add(s);
					s.start();
					return;
				}
			}
			
			Section s = new Section(section);
			curConstituents.add(s);
			stack.add(s);
			s.start();
		}
	}
	
	/**
	 * Ends the current section. The next invocation of {@link #start(String)}
	 * will create a section which is a subsection of the same section as the
	 * one which is ended by this method.
	 * 
	 * <p>This method should only be invoked following - and should correspond
	 * to - an invocation of {@link #start(String)}.
	 * 
	 * @throws IllegalStateException if this method is invoked more times than
	 * {@link #start(String)} (i.e. if {@link #getStackLevel()} would return
	 * {@code 1}). If this happens, remove the rogue call!
	 */
	public void end() {
		if(effectivelyEnabled && stack.removeLast().end() == root)
			throw new IllegalStateException();
	}
	
	/**
	 * Starts the next section, which is a subsection of the same section as
	 * the current section. Invoking this is equivalent to the following:
	 * 
	 * <pre>
	 * end();
	 * start(section);</pre>
	 * 
	 * @param section The name of the section.
	 * 
	 * @throws IllegalStateException if this method has been invoked without
	 * a prior call to {@link #start(String)} which hasn't yet been terminated
	 * by {@link #end()} (i.e. if {@link #getStackLevel()} would return
	 * {@code 1}).
	 * @throws NullPointerException if {@code section} is {@code null}.
	 * 
	 * @see #end()
	 * @see #start(String)
	 */
	public void next(String section) {
		end();
		start(section);
	}
	
	/**
	 * Flushes the profiler. This should be invoked every time the profiler is
	 * to be reset for a fresh round of profiling.
	 * 
	 * <p>This should <i>never</i> be called between invocations of {@link
	 * #start(String)} and {@link #end()}, as invoking this method will
	 * reset all declared sections, thus invalidating {@code start()} and
	 * causing {@code end()} to throw an exception.
	 * 
	 * <p>Note that this method will not throw an exception even if the {@link
	 * #getStackLevel() stack level} is greater than 1 (even though this should
	 * be the case for any proper profiling implementation).
	 */
	public void flush() {
		if(enabled) {
			effectivelyEnabled = true;
			root.end();
			lastRoot = root;
			stack.clear();
			if(resetOnFlush) {
				root = new Section(lastRoot.name);
				lastData = null;
			}
			stack.add(root);
			root.start();
		}
	}
	
	/**
	 * Wipes all stored profiling data. This method is typically only useful if
	 * {@link #setResetOnFlush(boolean) this profiler does not reset when
	 * flushed}, for which older profiling data may be minimising the
	 * visibility of newer data.
	 */
	public void reset() {
		// We're using disable() then enable() for convenience since doing so
		// wipes the sections, which is basically what this method is designed
		// to do. However, retain lastData since the profiler isn't actually
		// out of commission.
		SectionData last = lastData;
		disable();
		enable();
		lastData = last;
	}
	
	/**
	 * Enables profiling. Note that the profiler will not act as if it is
	 * enabled until {@link #flush()} is next invoked as to prevent problems
	 * with unbalanced invocations of {@link #start(String)} and {@link
	 * #end()}.
	 * 
	 * <p>For example, the following code will not cause an ISE to be thrown:
	 * 
	 * <pre>
	 * profiler.disable();
	 * profiler.start("blah"); // ignored as the profiler is disabled
	 * profiler.enable();
	 * profiler.end(); // ignored as flush() hasn't yet been invoked</pre>
	 * 
	 * <p>The following, however, will:
	 * 
	 * <pre>
	 * profiler.disable();
	 * profiler.start("blah"); // ignored as the profiler is disabled
	 * profiler.enable();
	 * profiler.flush();
	 * profiler.end(); // this will throw an ISE!</pre>
	 * </pre>
	 */
	public void enable() {
		enabled = true;
	}
	
	/**
	 * Disables profiling.
	 */
	public void disable() {
		if(!enabled)
			return;
		
		enabled = false;
		effectivelyEnabled = false;
		
		// let the gc do its work
		stack.clear();
		root = new Section(lastRoot.name);
		stack.add(root);
		lastRoot = root;
		lastData = null;
	}
	
	/**
	 * Note that this will only return {@code true} when profiling is
	 * effectively enabled - that is, only once {@link #flush()} is invoked
	 * following an invocation of {@link #enable()}.
	 * 
	 * @return {@code true} if profiling is enabled; {@code false} otherwise.
	 */
	public boolean isEnabled() {
		return effectivelyEnabled;
	}
	
	/**
	 * Sets the flushing behaviour of this profiler.
	 * 
	 * <ul>
	 * <li>If {@code resetOnFlush} is {@code true}, profiling will start afresh
	 *     after an invocation of {@link #flush()}; profiling performed between
	 *     successive invocations of {@code flush()} thus occur independently.
	 * <li>If {@code resetOnFlush} is {@code false}, profiling between
	 *     successive invocations of {@link #flush()} is summed, which may
	 *     produce smoother and more averaged-out profiling information.
	 * </ul>
	 */
	public void setResetOnFlush(boolean resetOnFlush) {
		this.resetOnFlush = resetOnFlush;
	}
	
	/**
	 * This method allows a user to verify that the profiler is in the desired
	 * state.
	 * 
	 * @throws IllegalStateException if profiling is enabled, the {@link
	 * #getStackLevel() stack level} does not equal {@code level}, and the
	 * {@link #getStackName() stack name} does not equal {@code stack}.
	 */
	public void verify(int level, String stack) {
		if(isEnabled() && getStackLevel() != level && !getStackName().equals(stack))
			throw new IllegalStateException("Profiler stack is \"" + getStackName()
					+ "\" (it should be \"" + stack + "\")");
	}
	
	/**
	 * Gets the size of the current profiler stack. If this returns 1, the
	 * current profiling section is the root section, and an invocation of
	 * either {@link #end()} or {@link #next(String)} would result in an ISE
	 * being thrown.
	 * 
	 * <p>Note that when profiling is disabled, this will always return {@code
	 * 1} and as such the above warning doesn't apply.
	 */
	public int getStackLevel() {
		return stack.size();
	}
	
	/**
	 * @return The name of the current section stack. This takes the form of
	 * period-delimited names of the section stack. If profiling is disabled
	 * this returns the name of the root section.
	 */
	public String getStackName() {
		Iterator<Section> i = stack.iterator();
		StringBuilder sb = new StringBuilder(stack.size() * 8);
		while(i.hasNext()) {
			sb.append(i.next().name);
			if(i.hasNext())
				sb.append('.');
		}
		return sb.toString();
	}
	
	/**
	 * Gets the profiler's data at the time it was most recently flushed, as
	 * per {@link #flush()}.
	 * 
	 * @return The profiler's data.
	 */
	public SectionData getData() {
		return lastData == null || !resetOnFlush
				? lastData = lastRoot.getData()
				: lastData;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A profiling section.
	 */
	private class Section {
		
		/** The section's name. */
		private final String name;
		/** Holds the time the section was started. */
		private long startTime = 0L;
		/** The duration of the section, in nanoseconds. */
		private long duration = 0L;
		/** true if we're currently timing. */
		private boolean active = false;
		/** The sections's constituent sections. */
		private final List<Section> constituents = new LightLinkedList<>();
		
		
		/**
		 * Creates a new Section.
		 * 
		 * @param name The name of the section.
		 */
		public Section(String name) {
			this.name = name;
		}
		
		/**
		 * Starts timing the section.
		 */
		public void start() {
			startTime = System.nanoTime();
			active = true;
		}
		
		/**
		 * Stops timing the section.
		 * 
		 * @return This section.
		 */
		public Section end() {
			if(active) {
				duration += System.nanoTime() - startTime;
				active = false;
			}
			return this;
		}
		
		private void updateDuration(long currTime) {
			if(active) {
				duration += currTime - startTime;
				startTime = currTime;
			}
		}
		
		/**
		 * Gets this section's data, and treats it as a root section.
		 * 
		 * @return The data.
		 */
		public SectionData getData() {
			long t = System.nanoTime();
			updateDuration(t);
			return getData("", 100f, 100f, t);
		}
		
		/**
		 * Gets this section's data.
		 * 
		 * @param parentName The section's parent's absolute name.
		 * @param totalPercent The total percentage which this section
		 * occupies.
		 * @param localPercent The percentage of its parent section which this
		 * section occupies.
		 * @param currentTime The current time, as per System.nanoTime().
		 * 
		 * @return The data.
		 */
		private SectionData getData(String parentName, float localPercent, float totalPercent,
				final long currentTime) {
			// Make parentName function as absoluteName
			parentName = !parentName.equals("") ? parentName + "." + name : name;
			
			// Imitating ArrayList functionality here
			SectionData[] children = new SectionData[constituents.size() + 1];
			int childCount = 0;
			
			long unspecified = duration; // time not specified by any constituents
			for(Section s : constituents) {
				s.updateDuration(currentTime);
				
				unspecified -= s.duration;
				
				float locPercent = duration == 0L ? 0f :
					((float)s.duration / duration);
				float totPercent = locPercent * totalPercent;
				
				children[childCount++] = s.getData(parentName, 100*locPercent, totPercent,
						currentTime);
			}
			
			//if(unspecified > 0L) {
			float locPercent = duration == 0L ? 0f :
				((float)unspecified / duration);
			float totPercent = locPercent * totalPercent;
			children[childCount] = new SectionDataUnspecified(parentName, unspecified,
					100*locPercent, totPercent);
			//}
			
			Arrays.sort(children);
			
			return new SectionDataNormal(name, parentName, duration, localPercent, totalPercent, children);
		}
		
	}
	
	/**
	 * Data about a profiling section.
	 * 
	 * <p>There are two types of sections which may be represented by an
	 * instance of this class - an ordinary section and an unspecified section.
	 * Ordinary sections always contain an unspecified section, plus any
	 * child sections added during program lifetime.
	 */
	public abstract class SectionData implements Comparable<SectionData> {
		
		/** The name of the section represented by this data. */
		public final String name;
		/** The section's absolute name - i.e. its own name prepended by
		 * period-delimited supersection names. This may alternatively be
		 * recursively defined as {@code [parentSectionName].[name]}.*/
		public final String absoluteName;
		/** The duration of the section, in nanoseconds. */
		public final long duration;
		/** The percentage of its parent section which is constituted by this
		 * section. */
		public final float localPercent;
		/** The overall percentage which is constituted by this section. */
		public final float totalPercent;
		
		
		/**
		 * Creates a new SectionData. Neither string parameter should be null.
		 */
		private SectionData(String name, String absoluteName,
				long duration, float localPercent, float totalPercent) {
			this.name = name;
			this.absoluteName = absoluteName;
			this.duration = duration;
			this.localPercent = localPercent;
			this.totalPercent = totalPercent;
		}
		
		/**
		 * Checks for whether or not this section has any constituent sections.
		 * This only returns {@code false} if this is an unspecified section.
		 * 
		 * @return {@code true} if this section has constituent sections;
		 * {@code false} otherwise.
		 */
		public abstract boolean hasConstituents();
		
		/**
		 * Gets this section's constituent sections. The returned array will be
		 * sorted, with the section with the largest {@link #localPercent} (or,
		 * equivalently, {@link #totalPercent}) at the head, and the one with
		 * the smallest at the tail.
		 * 
		 * @return This section's constituent sections, or an empty array if
		 * this section has no constituents.
		 */
		public abstract SectionData[] getConstituents();
		
		@Override
		public String toString() {
			return toString("");
		}
		
		/**
		 * Gets the string representation of this SectionData.
		 * 
		 * @param prefix That with which to prefix each line. Should not be
		 * null.
		 * 
		 * @return A string representation of this SectionData.
		 */
		private String toString(String prefix) {
			StringBuilder sb = new StringBuilder(prefix);
			sb.append(StringUtil.cullFP(totalPercent, 2));
			sb.append("% ");
			sb.append(StringUtil.cullFP(localPercent, 2));
			sb.append("% ");
			sb.append(name);
			sb.append(" (");
			sb.append(duration / 1000000);
			sb.append(" millis)");
			
			// since all constituent arrays contain the unspecified entry,
			// check length
			if(hasConstituents() && getConstituents().length > 1) {
				prefix = "    " + prefix;
				for(SectionData s : getConstituents()) {
					sb.append('\n');
					sb.append(s.toString(prefix));
				}
			}
			
			return sb.toString();
		}
		
		@Override
		public int compareTo(SectionData s) {
			return Float.compare(s.localPercent, localPercent);
			//if(localPercent > s.localPercent) return -1;
			//if(localPercent < s.localPercent) return 1;
			//return 0;
		}
		
	}
	
	/**
	 * The section data of a 'normal' section. Most sections are normal
	 * sections.
	 */
	private class SectionDataNormal extends SectionData {
		
		/** The constituents. */
		private final SectionData[] constituents;
		
		
		private SectionDataNormal(String name, String absoluteName,
				long duration, float localPercent, float totalPercent,
				SectionData[] constituents) {
			super(name, absoluteName, duration, localPercent, totalPercent);
			this.constituents = constituents;
		}
		
		@Override
		public boolean hasConstituents() {
			return true;
		}
		
		@Override
		public SectionData[] getConstituents() {
			return constituents;
		}
		
	}
	
	/**
	 * The section data of an 'unspecified' section.
	 */
	private class SectionDataUnspecified extends SectionData {
		
		private SectionDataUnspecified(String parentName,
				long duration, float localPercent, float totalPercent) {
			super("unspecified", parentName + ".unspecified", duration, localPercent, totalPercent);
		}
		
		@Override
		public boolean hasConstituents() {
			return false;
		}
		
		@Override
		public SectionData[] getConstituents() {
			return new SectionData[0];
		}
		
	}
	
}
