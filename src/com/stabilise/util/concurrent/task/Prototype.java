package com.stabilise.util.concurrent.task;

import static com.stabilise.util.concurrent.task.TaskTracker.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Executor;

import com.stabilise.util.Checks;
import com.stabilise.util.concurrent.event.EventDispatcher;

/**
 * A prototype for a TaskUnit and its TaskTracker, containing all data
 * necessary to build them.
 */
class Prototype {
    
    private static final BigDecimal BIG_LONG_MAX_VALUE = BigDecimal.valueOf(Long.MAX_VALUE);
    // We conservatively use RoundingMode.DOWN to avoid overflows. See build()
    private static final MathContext DIV_RULES = new MathContext(16, RoundingMode.DOWN);
    
    
    /** The actual task to run. null iff this prototype represents the root. */
    private final TaskRunnable task;
    /** The unit produced by this Prototype. null until built. Will be null if
     * {@link #task} is null. */
    TaskUnit unit = null;
    /** EventDispatcher of the unit to produce. */
    final EventDispatcher events = EventDispatcher.concurrentRetained();
    
    /** The prototype to come after this one. null if no next task */
    Prototype next = null;
    
    /** Initial status. Never null. */
    final String status;
    /** Parts count. Used if the parts count is bounded by Long.MAX_VALUE.
     * Includes the completion part, so the real value is bounded by
     * MAX_VALUE-1 */
    long parts;
    /** Parts count used if the count exceeds Long.MAX_VALUE. */
    private BigInteger bigParts = null;
    
    /** Strategy to apply to children. */
    private final ReportStrategy strategy;
    /** List of children. null if no children. */
    private final Collection<Prototype> children = new ArrayList<>();
    /** Number of parts to report to parent. Includes the completion part. 0 if
     * no parent. */
    long partsToReport = 0;
    
    /** The tracker we produce. null until built. */
    private TaskTracker tracker = null;
    
    
    /**
     * Creates a new Prototype for a task.
     * 
     * @param task May be null iff this is the root prototype.
     * @param parts The number of parts in the task. This should be 0 if the
     * user did not specify a parts count for the task, or it is a group and
     * hence does not have its own intrinsic count.
     * @param status The initial status of the task. If this is null we use
     * {@link TaskTracker#DEFAULT_STATUS}.
     * @param strategy The report strategy to apply to children of this task.
     * 
     * @throws IllegalArgumentException if parts < 0 || parts == Long.MAX_VALUE
     * @throws NullPointerException if {@code strategy == null}.
     */
    Prototype(TaskRunnable task, long parts, String status, ReportStrategy strategy) {
        this.task = task;
        this.parts = Checks.test(parts, MIN_PARTS, MAX_PARTS) + 1; // completion part
        this.status = status == null ? DEFAULT_STATUS : status;
        this.strategy = Objects.requireNonNull(strategy);
    }
    
    /**
     * Creates a child prototype which inherits our ReportStrategy for its
     * children.
     * 
     * @param parts The number of parts in the task. This should be 0 if the
     * user did not specify a parts count for the task, or it is a group and
     * hence does not have its own intrinsic count.
     * @param status The initial status of the task. If this is null we use
     * {@link TaskTracker#DEFAULT_STATUS}.
     * 
     * @return The child.
     * @throws IllegalArgumentException if parts < 0 || parts == Long.MAX_VALUE
     */
    Prototype child(TaskRunnable task, long parts, String status) {
        return child(task, parts, status, strategy);
    }
    
    /**
     * Creates a child prototype.
     * 
     * @param childParts The number of parts in the task. This should be 0 if
     * the user did not specify a parts count for the task, or it is a group
     * and hence does not have its own intrinsic count.
     * @param status The initial status of the task. If this is null we use
     * {@link TaskTracker#DEFAULT_STATUS}.
     * @param strategy The report strategy to apply to children of this task.
     * 
     * @return The child.
     * @throws IllegalArgumentException if parts < 0 || parts == Long.MAX_VALUE
     * @throws NullPointerException if {@code strategy == null}.
     */
    Prototype child(TaskRunnable task, long childParts, String status, ReportStrategy strategy) {
        Prototype child = new Prototype(task, childParts, status, strategy);
        children.add(child);
        return child;
    }
    
    /**
     * Builds the hierarchy of trackers. This should only ever be invoked once
     * on the root task. <!--Though in theory it wouldn't matter if this is
     * invoked multiple times-->
     */
    void buildHeirarchy(Executor exec) {
        build(exec, ReportStrategy.all());
    }
    
    /**
     * Builds the actual tracker using the specified report strategy to
     * determine the number of parts the tracker should report.
     * 
     * @param strat The strategy to apply to this prototype.
     */
    private void build(Executor exec, ReportStrategy strat) {
        // We begin by recursively building our children and collecting
        // their partsToReport.
        for(Prototype t : children) {
            t.build(exec, this.strategy);
            
            if(bigParts == null) {
                long p = parts + t.partsToReport;
                
                if(p < parts) // overflow - upgrade to BigInteger
                    bigParts = BigInteger.valueOf(parts);
                else
                    parts = p;
            }
            
            if(bigParts != null)
                bigParts = bigParts.add(BigInteger.valueOf(t.partsToReport));
        }
        
        if(bigParts != null) {
            // If our number of parts has been pushed into the BigInteger
            // range, we cap off parts at Long.MAX_VALUE and adjust our
            // children's partsToReport values such that their sum does not
            // exceed this value. We also need to resum parts to account for
            // rounding errors.
            parts = 1; // completion part
            double scale = BIG_LONG_MAX_VALUE
                    .divide(new BigDecimal(bigParts), DIV_RULES)
                    .doubleValue();
            for(Prototype t : children) {
                parts += t.scale(scale);
            }
        }
        
        // Though it would make more sense applying the ReportStrategy function
        // before normalising parts/bigParts to the valid range of longs, it's
        // simpler this way and doesn't require people to write functions
        // taking a BigInteger as the input parameter.
        // The -1/+1 is the completion part, which is temporarily pulled out as
        // to avoid being scaled.
        partsToReport = strat.get(parts - 1) + 1;
        if(partsToReport <= 0) // overflow, or the strat did something stupid
            throw new BadReportStrategyException();
        
        tracker = new TaskTracker(this);
        if(task != null)
            unit = new TaskUnit(exec, task, tracker, events);
        
        children.forEach(t -> {
            t.tracker.setParent(tracker);
            // While we're at it, we also link the units now that they've been
            // built.
            if(t.next != null)
                t.unit.setNext(t.next.unit);
        });
    }
    
    /**
     * Scales and returns partsToReport.
     */
    private long scale(double scale) {
        partsToReport = (long)(scale * partsToReport);
        if(tracker != null)
            tracker.setPartsToReport(partsToReport);
        return partsToReport;
    }
    
    TaskTracker get() {
        return tracker;
    }
    
}
