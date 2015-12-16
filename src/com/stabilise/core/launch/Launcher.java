package com.stabilise.core.launch;

import com.stabilise.core.Resources;


public class Launcher {
    
    private Launcher() {}
    
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "\"" + System.getProperty("java.home").replace('\\', '/') + "/bin/javaw\" "
                //"\"" + System.getProperty("sun.boot.library.path").replace('\\', '/') + "/javaw\""
                + " -jar "
                + " \"" + Resources.DIR_APP.child("Game.jar").path() + "\"",
                "arg1", "arg2");
        pb.directory(Resources.DIR_APP.file());
        pb.inheritIO();
        
        Process p = pb.start();
        p.waitFor();
        
        System.out.println("Application terminated");
    }
    
}
