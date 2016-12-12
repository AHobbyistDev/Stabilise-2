package com.stabilise.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Provides some utility methods to help manage dependencies.
 * 
 * <p>Naturally, this class does not have any dependencies of its own.
 */
public class Dependencies {
    
    private Dependencies() {} // non-instantiable
    
    
    public static boolean verbose = false;
    
    
    /**
     * Loads all classes from all .jar files located in the given directory and
     * any subdirectories. Subdirectories are loaded in alphabetical order.
     * 
     * @return A collection of .jar files which could not be loaded, due to an
     * I/O exception occurring.
     */
    public static Collection<File> loadAllDependencies(File lib) {
        System.out.println("Loading dependencies...");
        
        Collection<File> badFiles = new HashSet<>();
        
        if(!lib.exists() || !lib.isDirectory())
            return badFiles;
        
        doLoadFile(lib, badFiles);
        
        return badFiles;
    }
    
    private static void doLoadFile(File f, Collection<File> badFiles) {
        if(verbose)
            System.out.println("Trying " + f.getAbsolutePath());
        if(f.isDirectory()) {
            File[] files = f.listFiles();
            Arrays.sort(files, (f1, f2) -> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));
            for(File child : files)
                doLoadFile(child, badFiles);
        } else {
            if(f.getAbsolutePath().endsWith(".jar")) {
                try {
                    loadClassesFromJar(f);
                } catch(IOException e) {
                    System.out.println("Couldn't load dependency " + f.getAbsolutePath());
                    e.printStackTrace();
                    badFiles.add(f);
                }
            }
        }
    }
    
    /**
     * Loads all classes from the given .jar file.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public static void loadClassesFromJar(File file) throws IOException {
        System.out.println("Loading classes from .jar: \"" + file.getAbsolutePath() + "\"");
        
        try(JarFile jarFile = new JarFile(file)) {
            URL[] url = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(url, Dependencies.class.getClassLoader());
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class"))
                    continue;
                
                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                try {
                    cl.loadClass(className);
                    if(verbose)
                        System.out.println("Loaded class " + className);
                } catch(ClassNotFoundException e) {
                    System.out.println("Could not load class \"" + className + "\"");
                }
            }
        }
    }
    
}
