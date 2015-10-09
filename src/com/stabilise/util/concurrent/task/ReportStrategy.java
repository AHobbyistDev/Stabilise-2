package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.function.LongUnaryOperator;

/**
 * A task can generally be broken down into a number of small incremental
 * pieces, or "parts", which together constitute its entire operation. As a
 * task chugs along, it may increment this value to give an indication as to
 * how close it is to completion, and this general principle forms the basis of
 * progress bars, completion percentages, and the like.
 * 
 * <p>In general, progress trackers are nicest when they are granular and
 * consistent (i.e. they have minimal stalling). To maximise granularity, a
 * task should have a large number of constituent parts, and to maximise
 * consistency a task should strive to increment the parts counter at a flat
 * rate.
 * 
 * <p>However, a problem emerges when we compose smaller tasks into a single
 * one - what if the absolute level of granularity is not consistent amongst
 * subtasks? For example, suppose we were to load a number of images from the
 * filesystem, patch them together and upload them to GL. Suppose the loader
 * made its parts counter track the total number of bytes loaded, while the
 * patcher tracks how many images it's patched together. If we naively summed
 * the parts of each subtask, the loader would completely overshadow the
 * patcher (with, say, 250000 parts to 100). This may not be so much of a
 * problem if each image were patched right after it was loaded, but if the
 * program suspends patching until all images are loaded, then we'll see the
 * progress bar sit at 99% for a long while as we wait for the patcher to do
 * its job.
 * 
 * <p>To remedy this problem, we introduce report strategies - that is,
 * strategies for determining how much of a subtask's parts count contributes
 * to the overall parts count (or is <i>reported</i>). 
 */
public final class ReportStrategy {
    
    private static final ReportStrategy NONE = new ReportStrategy(l -> 0, true);
    private static final ReportStrategy ALL  = new ReportStrategy(l -> l, false);
    
    private final LongUnaryOperator func;
    private final boolean constant;
    
    private ReportStrategy(LongUnaryOperator func, boolean constant) {
        this.func = func;
        this.constant = constant;
    }
    
    long get(long parts) {
        return func.applyAsLong(parts); 
    }
    
    boolean isNone()     { return this == NONE; }
    boolean isConstant() { return constant;     }
    
    /**
     * The "no-report" strategy - completed parts of a task will not be
     * reported to its parent under this strategy. This strategy perfectly
     * balances all tasks and prevents a single task with an inordinate number
     * of parts from enveloping the rest, but sacrifices granularity.
     */
    public static ReportStrategy none() { return NONE; }
    
    /**
     * The "report everything" strategy - every completed part of a task will
     * be reported to its parent under this strategy. This strategy offers
     * maximum completion granularity, but risks allowing a single task with a
     * large number of parts to overshadow all of its peers.
     */
    public static ReportStrategy all()  { return ALL; }
    
    /**
     * 
     * - scalingFunction should be a nondecreasing positive semidefinite function
     */
    public static ReportStrategy scale(LongUnaryOperator scalingFunction) {
        return new ReportStrategy(Objects.requireNonNull(scalingFunction), false);
    }
    
    public static ReportStrategy constant(long value) {
        return new ReportStrategy(l -> value, true);
    }
    
}
