package com.stabilise.mod;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.UnorderedArrayList;

@Incomplete
public class ModLoader {
    
    public static void main(String[] args) {
        for(FileHandle f : findModCandidates()) {
            Log.get().postInfo("Searching for classes in " + f.toString());
            try {
                for(Class<?> c : loadClassesFromJar(f))
                    Log.get().postInfo("Found class: " + c.toString());
            } catch(Exception e) {
                Log.get().postSevere("Error reading jar", e);
            }
        }
    }
    
    public static FileHandle[] findModCandidates() {
        FileHandle[] fileList = Resources.DIR_MODS.list();
        UnorderedArrayList<FileHandle> jarFiles = new UnorderedArrayList<>();
        for(FileHandle f : fileList) {
            if(f.isDirectory())
                continue;
            if(f.extension().toLowerCase().equals("jar"))
                jarFiles.add(f);
        }
        return jarFiles.toArray(new FileHandle[0]);
    }
    
    public static Class<?>[] loadClassesFromJar(FileHandle file) throws Exception {
        JarFile jarFile = new JarFile(file.file());
        try {
            URL[] url = { new URL("jar:file:" + file.file().getAbsolutePath() + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(url);
            List<Class<?>> classes = new ArrayList<>();
            for(JarEntry je : IteratorUtils.toIterable(jarFile.entries())) {
                if(je.isDirectory() || !je.getName().endsWith(".class"))
                    continue;
                
                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                Class<?> c = cl.loadClass(className);
                classes.add(c);
            }
            return classes.toArray(new Class<?>[0]);
        } finally {
            jarFile.close();
        }
    }
    
}
