package com.stabilise.core.main;

/**
 * The log copier - copies any log files to a local directory.
 */
public class LogCopier {
    
    private LogCopier() {
        // non-instantiable
    }
    
    /**
     * Runs the log copier program.
     */
    /*
    public static void main(String[] args) {
        if(!Resources.APP_DIR.exists())
            return;
        try {
            FileUtils.copyDirectory(Resources.LOG_DIR, new File("logs/"));
        } catch(IOException e) {
            // meh
        }
        System.exit(0);
    }
    */
    
}
