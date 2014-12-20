package com.stabilise.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A Profiler object is used to gauge the amount of processing time which is
 * being dedicated to various portions of a program.
 * 
 * <p>A Profiler operates through usage of 'sections', which operate similarly
 * to a file system's folder structure. To give a general idea how certain
 * invocations control these sections:
 * 
 * <pre>
 * new Profiler() // flush() is invoked on construction
 * > flush()
 *     > start("Section1")
 *         > start("Subsection1.1")
 *         > next("Subsection1.2")
 *             > start("Subsection 1.2.1")
 *             > end()
 *         > end()                  // <-- these two are equivalent to
 *         > start("Subsection1.3") // <-- next("Subsection1.3")
 *         > end()
 *     > next("Section2")
 *     > next("Section3")
 *         > start("Subsection3.1")
 *         > end()   
 *     > end()
 * 
 * <p>A Profiler is not thread-safe.
 */
public class Profiler {
	
	/** The root section. */
	private Section root;
	/** The last root section. */
	private Section lastRoot;
	
	/** The stack of sections being profiled. Sections near the tail of this
	 * stack are constituents of sections closer to the head, with the first
	 * section being {@link #root}. */
	private final Deque<Section> stack = new LinkedList<Section>();
	
	/** Whether or not profiling is enabled. */
	private boolean enabled;
	/** Whether or not profiling should be treated as enabled. */
	private boolean effectivelyEnabled;
	
	
	/**
	 * Creates a new Profiler which is enabled by default, and for which the 
	 * root section is named "root".
	 */
	public Profiler() {
		this(true, "root");
	}
	
	/**
	 * Creates a new Profiler with the root section named "root".
	 * 
	 * @param enabled The default 'enabled' status. This may be later changed
	 * via {@link #enable()} and {@link #disable()}.
	 */
	public Profiler(boolean enabled) {
		this(enabled, "root");
	}
	
	/**
	 * Creates a new Profiler, which is enabled by default.
	 * 
	 * @param rootName The name of the root section.
	 */
	public Profiler(String rootName) {
		this(true, rootName);
	}
	
	/**
	 * Creates a new Profiler.
	 * 
	 * @param enabled The default 'enabled' status. This may be later changed
	 * via {@link #enable()} and {@link #disable()}.
	 * @param rootName The name of the root section.
	 */
	public Profiler(boolean enabled, String rootName) {
		this.enabled = enabled;
		effectivelyEnabled = enabled;
		
		root = new Section(rootName);
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
	 * @throws NoSuchElementException if {@link #end()} has been invoked more
	 * times than this method. If this happens, remove the rogue call!
	 */
	public void start(String section) {
		if(effectivelyEnabled) {
			if(section == null)
				throw new NullPointerException("section is null");
			
			List<Section> curConstituents = stack.getLast().constituents;
			
			// Somewhat inefficient, but check to see if such a section already
			// exists.
			for(Section s : curConstituents) {
				if(s.name == section) {
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
	 * {@link #start(String)}. If this happens, remove the rogue call!
	 */
	public void end() {
		if(effectivelyEnabled) {
			if(stack.removeLast() == root)
				throw new IllegalStateException();
		}
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
	 * @throws NullPointerException if {@code section} is {@code null}.
	 * @throws NoSuchElementException if {@link #end()} has been invoked more
	 * times than {@link #start(String)}. If this happens, remove the rogue
	 * call!
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
	 */
	public void flush() {
		if(enabled) {
			effectivelyEnabled = true;
			root.end();
			lastRoot = root;
			stack.clear();
			root = new Section(lastRoot.name);
			stack.add(root);
			root.start();
		}
	}
	
	/**
	 * Enables profiling.
	 */
	public void enable() {
		enabled = true;
		// Do not set effectivelyEnabled = true until flush() is invoked as
		// this may have been invoked between invocations of start() and end(),
		// which can and will lead to exceptions being thrown.
	}
	
	/**
	 * Disables profiling.
	 */
	public void disable() {
		enabled = false;
		effectivelyEnabled = false;
		
		// let the gc do its work
		stack.clear();
		root = new Section(lastRoot.name);
		stack.add(root);
		lastRoot = root;
	}
	
	/**
	 * @return {@code true} if profiling is enabled; {@code false} otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Gets the profiler's data at the time it was most recently flushed, as
	 * per {@link #flush()}.
	 * 
	 * @return The profiler's data.
	 */
	public SectionData getData() {
		return lastRoot.getData();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A profiling section.
	 */
	private static class Section {
		
		/** The section's name. */
		private final String name;
		/** Holds the time the section was started. */
		private long startTime = 0L;
		/** The duration of the section, in nanoseconds. */
		private long duration = 0L;
		/** The sections's constituent sections. This may be indefinitely
		 * recursive. */
		private final List<Section> constituents = new LinkedList<Section>();
		
		
		/**
		 * Creates a new Section.
		 * 
		 * @param name The name of the section.
		 */
		private Section(String name) {
			this.name = name;
		}
		
		/**
		 * Starts timing the section.
		 */
		private void start() {
			startTime = System.nanoTime();
		}
		
		/**
		 * Stops timing the section.
		 */
		private void end() {
			duration += System.nanoTime() - startTime;
		}
		
		/**
		 * Gets this section's data, and treats it as a root section.
		 * 
		 * @return The data.
		 */
		private SectionData getData() {
			return getData("", 100f, 100f);
		}
		
		/**
		 * Gets this section's data.
		 * 
		 * @param parentName The section's parent's absolute name.
		 * @param totalPercent The total percentage which this section
		 * occupies.
		 * @param localPercent The percentage of its parent section which this
		 * section occupies.
		 * 
		 * @return The data.
		 */
		private SectionData getData(String parentName, float localPercent, float totalPercent) {
			if(parentName != "")
				parentName += "." + name;
			else
				parentName = name;
			
			// parentName functions as absoluteName for our purposes here
			SectionData data = new SectionDataNormal(name, parentName, duration, localPercent, totalPercent);
			
			List<SectionData> children = new ArrayList<SectionData>();
			
			long unspecified = duration; // time not specified by any constituents
			for(Section s : constituents) {
				unspecified -= s.duration;
				
				float locPercent = duration == 0L ? 0f :
					((float)s.duration / duration);
				float totPercent = locPercent * totalPercent;
				
				children.add(s.getData(parentName, 100*locPercent, totPercent));
			}
			
			//if(unspecified > 0L) {
			float locPercent = duration == 0L ? 0f :
				((float)unspecified / duration);
			float totPercent = locPercent * totalPercent;
			children.add(new SectionDataUnspecified(parentName, unspecified, 100*locPercent, totPercent));
			//}
			
			Collections.sort(children);
			data.setConstituents(children.toArray(new SectionData[0]));
			
			return data;
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
	public static abstract class SectionData implements Comparable<SectionData> {
		
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
		 * Sets this SectionData's array of constituents.
		 * 
		 * @param constituents The constituents.
		 */
		protected abstract void setConstituents(SectionData[] constituents);
		
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
			sb.append(StringUtil.floatToNPlaces(totalPercent, 2));
			sb.append("% ");
			sb.append(StringUtil.floatToNPlaces(localPercent, 2));
			sb.append("% ");
			sb.append(name);
			sb.append(" (");
			sb.append(duration);
			sb.append(")");
			
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
			if(localPercent > s.localPercent) return -1;
			if(localPercent < s.localPercent) return 1;
			return 0;
		}
		
	}
	
	/**
	 * The section data of a 'normal' section. Most sections are normal
	 * sections.
	 */
	private static class SectionDataNormal extends SectionData {
		
		/** The constituents. */
		private SectionData[] constituents;
		
		
		private SectionDataNormal(String name, String absoluteName,
				long duration, float localPercent, float totalPercent) {
			super(name, absoluteName, duration, localPercent, totalPercent);
		}
		
		@Override
		public boolean hasConstituents() {
			return true;
		}
		
		@Override
		protected void setConstituents(SectionData[] constituents) {
			this.constituents = constituents;
		}
		
		@Override
		public SectionData[] getConstituents() {
			return constituents;
		}
		
	}
	
	/**
	 * The section data of an 'unspecified' section.
	 */
	private static class SectionDataUnspecified extends SectionData {
		
		private SectionDataUnspecified(String parentName,
				long duration, float localPercent, float totalPercent) {
			super("unspecified", parentName + ".unspecified", duration, localPercent, totalPercent);
		}
		
		@Override
		public boolean hasConstituents() {
			return false;
		}
		
		@Override
		protected void setConstituents(SectionData[] constituents) {
			// ignored
		}
		
		@Override
		public SectionData[] getConstituents() {
			return new SectionData[0];
		}
		
	}
	
}
