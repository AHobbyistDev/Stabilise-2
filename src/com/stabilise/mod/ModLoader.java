package com.stabilise.mod;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.util.Log;
import com.stabilise.util.collect.LightArrayList;


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
		FileHandle[] fileList = Resources.MODS_DIR.list();
		LightArrayList<FileHandle> jarFiles = new LightArrayList<>();
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
			for(JarEntry je : toItr(jarFile.entries())) {
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
	
	private static final <E> Iterable<E> toItr(Enumeration<E> e) {
		return new EnumIterable<E>(e);
	}
	
	private static final class EnumIterable<E> implements Iterable<E> {
		
		private final Enumeration<E> e;
		private EnumIterable(Enumeration<E> e) {
			this.e = Objects.requireNonNull(e);
		}
		
		public Iterator<E> iterator() {
			return new Iterator<E>() {
				@Override
				public boolean hasNext() {
					return e.hasMoreElements();
				}
				@Override
				public E next() {
					return e.nextElement();
				}
			};
		}
		
	}
	
}
