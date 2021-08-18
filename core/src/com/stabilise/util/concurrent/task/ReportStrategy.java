package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.function.LongUnaryOperator;

import com.stabilise.util.Checks;

/**
 * A task can generally be broken down into a number of small incremental
 * pieces, or "parts", which together constitute its entire operation. As a
 * task chugs along it may increment this value to give an indication as to
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
 * to the overall parts count (or how much is <i>reported</i>). For each
 * subtask, we apply a report strategy to determine how many parts it will
 * contribute to the overall count. We offer 5 types of report strategies:
 * 
 * <ul>
 * <li><b>{@link #none() none}</b> bubbles up no parts from a subtask.
 * <li><b>{@link #all() all}</b> bubbles up all parts from a subtask.
 * <li><b>{@link #constant(long) constant}</b> strategies force all subtasks to
 *     contribute the same number of parts.
 * <li><b>{@link #scale(double) scale}</b> strategies simply multiply a
 *     subtask's parts by a specified amount before sending them to the parent
 *     task.
 * <li><b>{@link #manual(LongUnaryOperator) manual}</b> strategies allow the
 *     user to manually specify their own function which produces the number of
 *     parts to report given a subtask's number of reports.
 * </ul>
 * 
 * <p>In all cases a subtask's parts are scaled appropriately when reported to
 * the parent task (e.g. if a subtask has completed 5/10 of its parts but is
 * under a constant strategy of 500, it will report 250 parts to its
 * parent<sup><tt>1</tt></sup>).
 * 
 * <p><tt>1.</tt> This isn't technically correct. A task which thinks it has 10
 * parts altogether actually has 11, as every task is additionally allocated a
 * "completion part" which is only set when the task is completed. This
 * completion part is always bubbled up regardless of the report strategy used
 * (though, again, this may not always be the case - the completion part
 * may be rounded away if a task unit's reported parts must be rescaled to
 * prevent its parent's parts count from overflowing). In this example, the
 * task unit would've actually completed 5/11 of its parts, which corresponds
 * to 227 parts to report to its parent.
 */
public final class ReportStrategy {
    
    private static final ReportStrategy NONE = new ReportStrategy(l -> 0);
    private static final ReportStrategy ALL  = new ReportStrategy(l -> l);
    
    private final LongUnaryOperator func;
    
    private ReportStrategy(LongUnaryOperator func) {
        this.func = func;
    }
    
    long get(long parts) {
        return func.applyAsLong(parts); 
    }
    
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
     * The "report this many" strategy - every subtask will report the
     * specified number of parts to its parent under this strategy. This
     * strategy, like {@link #none()} perfectly balances all tasks, and can be
     * made sufficiently granular with a sufficiently large parts value.
     * 
     * @throws IllegalArgumentException if parts < 0 || parts == Long.MAX_VALUE
     */
    public static ReportStrategy constant(long parts) {
        Checks.test(parts, TaskTracker.MIN_PARTS, TaskTracker.MAX_PARTS);
        return new ReportStrategy(l -> parts);
    }
    
    /**
     * The "scale reported parts by this much" strategy. Under this strategy
     * the number of parts reported is equal to a task's total number of parts
     * multiplied by scale.
     * 
     * <p>We only allow scaling factors in the range 0-1 as to disallow
     * negative results and preclude overflows.
     * 
     * @throws IllegalArgumentException if scale < 0 || scale > 1.
     */
    public static ReportStrategy scale(double scale) {
        Checks.test(scale, 0, 1);
        return new ReportStrategy(l -> (long)(scale * l));
    }
    
    /**
     * Manually specifies a "report this many" function. The function takes a
     * task's total parts as its input and returns the number of parts to report
     * to its parent.
     * 
     * <p>As the correctness of a function cannot be analysed at compile time,
     * a {@link BadReportStrategyException} may be thrown at runtime if the
     * function produces illegal values. Any negative returned value is
     * considered illegal, whether it originated from an integer overflow or
     * otherwise (so watch out for overflows!).
     * 
     * @throws NullPointerException if func is null
     */
    public static ReportStrategy manual(LongUnaryOperator func) {
        return new ReportStrategy(Objects.requireNonNull(func));
    }
    
}
