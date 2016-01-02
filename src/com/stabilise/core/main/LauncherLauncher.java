package com.stabilise.core.main;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.stabilise.util.collect.IteratorUtils;

/**
 * Bare-bones dependency-less launcher class which loads and runs the real
 * launcher.
 */
public class LauncherLauncher {
    
    /** Note: must be in the same directory as our .jar */
    public static final String LAUNCHER_FILES = "Launcherfiles.jar";
    public static final String LAUNCHER_CLASS = "com.stabilise.desktop.DesktopLauncher";
    
    
    public static void main(String[] args) throws Exception {
        File file = new File(LAUNCHER_FILES);
        Map<String, Class<?>> classes = new HashMap<>();
        
        try(JarFile jarFile = new JarFile(new File(LAUNCHER_FILES))) {
            URL[] url = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(url, LauncherLauncher.class.getClassLoader());
            for(JarEntry je : IteratorUtils.toIterable(jarFile.entries())) {
                if(je.isDirectory() || !je.getName().endsWith(".class"))
                    continue;
                
                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                Class<?> c = cl.loadClass(className);
                classes.put(className, c);
            }
        }
        
        Class<?> mainClass = classes.get(LAUNCHER_CLASS);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object[]) args);
    }
    
}
