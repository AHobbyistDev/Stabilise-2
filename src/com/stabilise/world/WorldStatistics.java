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
        sb.append('\t');
        sb.append(o);
        sb.append('\n');
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
            return name + "{"
                    +       "req:" + requests  + ", "
                    +       "rej:" + rejected  + ", "
                    +   "started:" + started   + ", "
                    +   "aborted:" + aborted   + ", "
                    + "completed:" + completed + ", "
                    +    "failed:" + failed
                    + "}";
        }
        
    }
    
}
