package com.stabilise.core;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.stabilise.util.IOUtil;
import com.stabilise.util.Log;

/**
 * Manages application resources.
 */
public class Resources {
	
	/** The directory in which all application data will be saved. */
	public static final File APP_DIR = getApplicationPath("stabilise");
	/** The directory in which os-specific natives required for the application
	 * will be located. */
	public static final File NATIVES_DIR = getLWJGLNativesPath();
	
	/** The directory in which config files should be saved. */
	public static final File CONFIG_DIR = IOUtil.createDirQuietly(new File(APP_DIR, "config/"));
	
	/** The directory in which game save data will be saved. */
	public static final File SAVES_DIR = IOUtil.createDirQuietly(new File(APP_DIR, "saves/"));
	/** The directory in which character save data will be saved. */
	public static final File CHARACTERS_DIR = IOUtil.createDirQuietly(new File(SAVES_DIR, "chars/"));
	/** The directory in which world save data will be saved. */
	public static final File WORLDS_DIR = IOUtil.createDirQuietly(new File(SAVES_DIR, "worlds/"));
	
	/** The directory in which application's resources should be located. */
	public static final File RESOURCE_DIR = IOUtil.createDirQuietly(new File(APP_DIR, "res/"));
	/** The directory in which the application's image resources should be
	 * located. */
	public static final File IMAGE_DIR = IOUtil.createDirQuietly(new File(RESOURCE_DIR, "img/"));
	/** The directory in which the application's font resources should be
	 * located. */
	public static final File FONT_DIR = IOUtil.createDirQuietly(new File(RESOURCE_DIR, "fonts/"));
	/** The directory in which the application's spritesheet resources should
	 * be located. */
	public static final File SPRITESHEET_DIR = IOUtil.createDirQuietly(new File(IMAGE_DIR, "sheets/"));
	/** The directory in which the application's audio resources should be
	 * located. */
	public static final File SOUND_DIR = IOUtil.createDirQuietly(new File(RESOURCE_DIR, "sound/"));
	/** The directory in which the application's schematic resources should be
	 * located. */
	public static final File SCHEMATIC_DIR = IOUtil.createDirQuietly(new File(RESOURCE_DIR, "schematics/"));
	/** The directory in which the application's shader resources should be
	 * located. */
	public static final File SHADER_DIR = IOUtil.createDirQuietly(new File(RESOURCE_DIR, "shaders/"));
	
	/** The directory in which console output logs should be saved. */
	public static final File LOG_DIR = IOUtil.createDirQuietly(new File(APP_DIR, "log/"));
	
	
	/**
	 * Finds and returns the main directory for the application.
	 * 
	 * @param appName The name of the application.
	 * 
	 * @return The {@code File} representing the main application directory.
	 * @throws NullPointerException if {@code appName} is {@code null}.
	 */
	private static File getApplicationPath(String appName) {
		appName = "." + appName + "/";
		//return new File("E:/Personal Stuff/IT Stuff/Java/Projects/Stabilise 2/" + appName);
		///*
		String dir = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		int osID = os.contains("windows") ? 0 : (os.contains("mac") ? 1 : (os.contains("linux") ? 2 : -1));
		
		File appDir = null;
		switch(osID) {
			case 0:		// Windows
				String appDataDir = System.getenv("APPDATA");
				if(appDataDir != null)
					appDir = new File(appDataDir, appName);
				else
					appDir = new File(dir, appName);
				break;
			case 1:		// Mac
				appDir = new File(dir, "Library/Application Support/" + appName);
				break;
			case 2:		// Linux
				appDir = new File(dir, appName);
				break;
			default:	// Other
				Log.get().postSevere("OS not supported");
				System.exit(0);
				break;
		}
		
		return IOUtil.createDirQuietly(appDir);
		//*/
	}
	
	/**
	 * Finds and returns the folder containing the LWJGL natives.
	 * 
	 * @return The {@code File} representing the directory containing the LWJGL
	 * natives.
	 */
	private static File getLWJGLNativesPath() {
		String os = System.getProperty("os.name").toLowerCase();
		os = os.contains("windows") ? "windows" : (os.contains("mac") ? "macosx" : (os.contains("linux") ? "linux" : "unknown"));
		
		return IOUtil.createDirQuietly(new File(APP_DIR, "natives/" + os));
	}
	
	/**
	 * Gets the input stream for a file contained within the application's
	 * .jar. It is the responsibility of the invoker to ensure that the stream
	 * is closed via invocation of {@link InputStream#close() close()} once it
	 * has fallen out of use.
	 * 
	 * @param path The path of the file.
	 * 
	 * @return The file's input stream, or {@code null} if the given file does
	 * not exist.
	 * @throws NullPointerException if {@code path} is {@code null}.
	 */
	public static InputStream getClasspathInputStream(String path) {
		return Resources.class.getResourceAsStream(path);
	}
	
	/**
	 * Gets the input stream for a file in the file system. It is the
	 * responsibility of the invoker to ensure that the stream is closed via
	 * invocation of {@link FileInputStream#close() close()} once it has fallen
	 * out of use.
	 * 
	 * @param file The file.
	 * 
	 * @return The file's input stream, or {@code null} if the given file does
	 * not exist, or for some reason could not be accessed.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 */
	public static FileInputStream getFilepathInputStream(File file) {
		if(!file.exists())
			return null;
		
		try {
			return new FileInputStream(file);
		} catch(FileNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Gets the output stream for a file in the file system. It is the
	 * responsibility of the invoker to ensure that the stream is closed via
	 * invocation of {@link FileOutputStream#close() close()} once it has
	 * fallen out of use.
	 * 
	 * @param file The file.
	 * 
	 * @return The file's output stream, or {@code null} if {@code file}
	 * represents a directory, could not be created, or could not be accessed
	 * for any other reason.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 */
	public static FileOutputStream getFilepathOutputStream(File file) {
		try {
			return new FileOutputStream(file);
		} catch(FileNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Loads an image from the application's .jar. Note that this only loads
	 * images saved in the .png format.
	 * 
	 * @param imagePath The image path.
	 * 
	 * @return The image.
	 * @throws NullPointerException if {@code imagePath} is {@code null}.
	 * @throws IOException if the image does not exist, or an I/O exception is
	 * encountered while loading the image.
	 */
	public static BufferedImage loadImageFromClasspath(String imagePath) throws IOException {
		if(!imagePath.toLowerCase().endsWith(".png"));
			imagePath += ".png";
		
		InputStream is = Resources.class.getResourceAsStream(imagePath);
		return ImageIO.read(is);
	}
	
	/**
	 * Loads an image relative to the {@link #IMAGE_DIR image directory}. Note
	 * that this only loads images saved in the .png format.
	 * 
	 * @param imagePath The image path.
	 * 
	 * @return The image.
	 * @throws NullPointerException if {@code imagePath} is {@code null}.
	 * @throws IOException if the image does not exist, or an I/O exception is
	 * encountered while loading the image.
	 */
	public static BufferedImage loadImageFromFileSystem(String imagePath) throws IOException {
		if(!imagePath.toLowerCase().endsWith(".png"))
			imagePath += ".png";
		
		return doLoadImageFromFileSystem(new File(IMAGE_DIR, imagePath));
	}
	
	/**
	 * Loads an image from the file system. Note that this only loads images
	 * saved in the .png format.
	 * 
	 * @param imageFile The image file.
	 * 
	 * @return The image, or {@code null} if a null file is given.
	 * @throws NullPointerException if {@code imageFile} is {@code null}.
	 * @throws IOException if the image does not exist, or an I/O exception is
	 * encountered while loading the image.
	 */
	public static BufferedImage loadImageFromFileSystem(File imageFile) throws IOException {
		if(imageFile.getAbsolutePath().toLowerCase().endsWith(".png"))
			return doLoadImageFromFileSystem(imageFile);
		else
			return doLoadImageFromFileSystem(new File(imageFile.getAbsolutePath() + ".png"));
	}
	
	/**
	 * Handles loading an image from the file system.
	 * 
	 * @param imageFile The image file.
	 * 
	 * @return The image.
	 * @throws IOException if the image does not exist, or an I/O exception is
	 * encountered while loading the image.
	 */
	private static BufferedImage doLoadImageFromFileSystem(File imageFile) throws IOException {
		return ImageIO.read(new FileInputStream(imageFile));
	}
	
	/**
	 * Reads a text file from the application's .jar. Each element in the
	 * returned array represents one line of the file.
	 * 
	 * @param filePath The file's path.
	 * 
	 * @return The file's contents.
	 * @throws NullPointerException if {@code filePath} is {@code null}.
	 * @throws IOException if the file does not exist, or an I/O exception is
	 * encountered while reading the file.
	 */
	public static String[] loadTextFileFromClasspath(String filePath) throws IOException {
		InputStream is = Resources.class.getResourceAsStream(filePath);
		if(is == null)
			throw new IOException("Text resource does not exist!");
		
		// Theoretically should never ever throw the the UnsupportedEncodingException
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		
		ArrayList<String> strings = new ArrayList<String>();
		String s;
		
		try {
			while((s = br.readLine()) != null) {
				strings.add(s);
			}
		} finally {
			br.close();
			isr.close();
		}
		
		return strings.toArray(new String[0]);
	}
	
	/**
	 * Reads a text file from the file system. Each element in the returned
	 * array represents one line of the file.
	 * 
	 * @param file The file.
	 * 
	 * @return The file's contents.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws IOException if the file does not exist, or an I/O exception is
	 * encountered while reading the file.
	 */
	public static String[] loadTextFileFromFileSystem(File file) throws IOException {
		if(!file.exists())
			throw new IOException("Text resource does not exist!");
		
		FileReader fr = new FileReader(file);		// May throw a FileNotFoundException
		BufferedReader br = new BufferedReader(fr);
		
		ArrayList<String> strings = new ArrayList<String>();
		String s;
		
		try {
			while((s = br.readLine()) != null) {
				strings.add(s);
			}
		} finally {
			br.close();
			fr.close();
		}
		
		return strings.toArray(new String[0]);
	}
	
	
	// non-instantiable
	private Resources() {}
	
}
