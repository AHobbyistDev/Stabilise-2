package com.stabilise.world;

import java.util.concurrent.atomic.LongAdder;

/**
 * Maintains statistics about a world.
 */
public class WorldStatistics {
    
    // World generator
    
    public final ProcessStats gen = new ProcessStats("GenStats");
    
    // World loader
    
    public final ProcessStats load = new ProcessStats("LoadStats");
    public final ProcessStats save = new ProcessStats("SaveStats");
    
    // Server
    
    
    
    // Client
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistics for world: {\n");
        append(sb, gen);
        append(sb, load);
        append(sb, save);
        sb.append('}');
        return sb.toString();
    }
    
    private void append(StringBuilder sb, Object o) {
        sb.append("    ").append(o).append('\n');
    }
    
    public static class ProcessStats {
        
        private final String name;
        private ProcessStats(String name) { this.name = name; }
        
        public final LongAdder requests = new LongAdder(),
                rejected = new LongAdder(),
                started = new LongAdder(),
                aborted = new LongAdder(),
                completed = new LongAdder(),
                failed = new LongAdder();
        
        @Override
        public String toString() {
            return String.format("%10s", name) + "{"
                    +       "req:" + String.format("%4d", requests.sum())  + ", "
                    +       "rej:" + String.format("%4d", rejected.sum())  + ", "
                    +   "started:" + String.format("%4d", started.sum())   + ", "
                    +   "aborted:" + String.format("%4d", aborted.sum())   + ", "
                    + "completed:" + String.format("%4d", completed.sum()) + ", "
                    +    "failed:" + String.format("%4d", failed.sum())
                    + "}";
        }
        
    }
    
}
