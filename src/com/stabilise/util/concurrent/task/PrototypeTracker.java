package com.stabilise.util.concurrent.task;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import com.stabilise.util.Checks;

public class PrototypeTracker {
    
    static final long MIN_PARTS = 0;
    static final long MAX_PARTS = Long.MAX_VALUE - 1;
    
    /** Initial status. Never null. */
    final String status;
    /** Parts count. Used if the parts count is bounded by Long.MAX_VALUE.
     * Includes the completion part, so the real value is bounded by
     * MAX_VALUE-1 */
    long parts;
    /** Parts count used if the count exceeds Long.MAX_VALUE. */
    private BigInteger bigParts = null;
    /** If we have our own parts we can't have children. */
    private final boolean hasOwnParts;
    
    /** Strategy to apply to children. */
    private final ReportStrategy strategy;
    /** List of children. null if no children. */
    private Collection<PrototypeTracker> children = null;
    /** Parent. null if no parent. */
    private PrototypeTracker parent = null;
    /** Number of parts to report to parent. Includes the completion part. 0 if
     * no parent. */
    long partsToReport = 0;
    
    /** The tracker we produce. null until built. */
    private TaskTracker2 tracker = null;
    
    
    public PrototypeTracker(long parts, String status, ReportStrategy strategy) {
        this.parts = Checks.test(parts, MIN_PARTS, MAX_PARTS) + 1;
        this.status = status;
        this.strategy = strategy;
        this.hasOwnParts = parts != 1; // 1 = "completion" part
    }
    
    public PrototypeTracker child(long childParts, String status) {
        return child(childParts, status, strategy);
    }
    
    public PrototypeTracker child(long childParts, String status, ReportStrategy strategy) {
        if(hasOwnParts) {
            // This should never happen in practice as everything is controlled
            // by TaskBuilder.
            throw new AssertionError("Cannot add a child to a task with"
                    + " its own parts.");
        }
        
        PrototypeTracker child = new PrototypeTracker(childParts, status, strategy);
        child.parent = this;
        if(children == null)
            children = new ArrayList<>();
        children.add(child);
        return child;
    }
    
    public void buildHeirarchy() {
        if(parent != null)
            throw new UnsupportedOperationException("Cannot build from a tracker"
                    + " that is not the root tracker.");
        if(tracker != null)
            throw new IllegalStateException("Already built hierarchy!");
        build(ReportStrategy.all());
    }
    
    private void build(ReportStrategy strat) {
        if(children != null) {
            // We begin by recursively building our children and collecting
            // their partsToReport.
            for(PrototypeTracker t : children) {
                t.build(this.strategy);
                
                if(bigParts == null) {
                    long p = parts + t.partsToReport;
                    
                    if(p < parts) // overflow!
                        // Upgrade to BigInteger
                        bigParts = BigInteger.valueOf(parts);
                    else
                        parts = p;
                }
                
                if(bigParts != null) {
                    bigParts = bigParts.add(BigInteger.valueOf(t.partsToReport));
                }
            }
        }
        
        if(bigParts != null) {
            assert(children != null);
            
            // If our number of parts has been pushed into the BigInteger
            // range, we cap off parts at Long.MAX_VALUE and adjust our
            // childrens' partsToReport values such that their sum does not
            // exceed this value. We also need to resum parts to account for
            // rounding errors.
            parts = 1; // completion part
            double scale = BigDecimal.valueOf(Long.MAX_VALUE)
                    .divide(new BigDecimal(bigParts))
                    .doubleValue();
            for(PrototypeTracker t : children) {
                parts += t.scale(scale);
            }
        }
        
        // Though it would make more sense applying the ReportStrategy function
        // before normalising parts/bigParts to the valid range of longs, it's
        // simpler this way and doesn't require people to write functions
        // taking a BigInteger as the input parameter.
        partsToReport = strat.get(parts - 1) + 1; // -1/+1 for completion part
        if(partsToReport < 0) // either an overflow, or the strat did something stupid
            throw new BadReportStrategyException();
        
        tracker = new TaskTracker2(this);
        if(children != null)
            children.forEach(t -> t.tracker.parent = tracker);
    }
    
    private long scale(double scale) {
        partsToReport = (long)(scale * partsToReport);
        if(tracker != null)
            tracker.partsToReport = partsToReport;
        return partsToReport;
    }
    
    public TaskTracker2 get() {
        return tracker;
    }
    
}
