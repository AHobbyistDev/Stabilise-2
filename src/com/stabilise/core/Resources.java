package com.stabilise.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.stabilise.util.Log;
import com.stabilise.util.io.IOUtil;

/**
 * Manages application resources.
 */
public class Resources {
	
	/** The application's root working directory. */
	public static final FileHandle APP_DIR = getApplicationPath("stabilise");
	
	/** The config file directory. */
	public static final FileHandle CONFIG_DIR = IOUtil.createDir(APP_DIR.child("config/"));
	
	/** Root directory for save data. */
	public static final FileHandle SAVES_DIR = IOUtil.createDir(APP_DIR.child("saves/"));
	public static final FileHandle CHARACTERS_DIR = IOUtil.createDir(SAVES_DIR.child("chars/"));
	public static final FileHandle WORLDS_DIR = IOUtil.createDir(SAVES_DIR.child("worlds/"));
	
	/** Root directory for application resources e.g. images, sounds. */
	public static final FileHandle RESOURCE_DIR = IOUtil.createDir(APP_DIR.child("res/"));
	public static final FileHandle IMAGE_DIR = IOUtil.createDir(RESOURCE_DIR.child("img/"));
	public static final FileHandle FONT_DIR = IOUtil.createDir(RESOURCE_DIR.child("fonts/"));
	public static final FileHandle SOUND_DIR = IOUtil.createDir(RESOURCE_DIR.child("sound/"));
	public static final FileHandle SCHEMATIC_DIR = IOUtil.createDir(RESOURCE_DIR.child("schematics/"));
	
	/** The file directory for mods. */
	public static final FileHandle MODS_DIR = IOUtil.createDir(APP_DIR.child("mods/"));
	
	/** The directory in which console output logs should be saved. */
	public static final FileHandle LOG_DIR = IOUtil.createDir(APP_DIR.child("log/"));
	
	
	/**
	 * Finds and returns the main directory for the application.
	 * 
	 * @param appName The name of the application.
	 * 
	 * @return The {@code File} representing the main application directory.
	 * @throws NullPointerException if {@code appName} is {@code null}.
	 */
	private static FileHandle getApplicationPath(String appName) {
		appName = "." + appName + "/";
		String dir = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		int osID = os.contains("windows") ? 0 : (os.contains("mac") ? 1 : (os.contains("linux") ? 2 : -1));
		
		FileHandle appDir = null;
		switch(osID) {
			case 0:		// Windows
				String appDataDir = System.getenv("APPDATA");
				if(appDataDir != null)
					appDir = new FileHandle(new File(appDataDir, appName));
				else
					appDir = new FileHandle(new File(dir, appName));
				break;
			case 1:		// Mac
				appDir = new FileHandle(new File(dir, "Library/Application Support/" + appName));
				break;
			case 2:		// Linux
				appDir = new FileHandle(new File(dir, appName));
				break;
			default:	// Other
				Log.get().postSevere("OS not supported");
				throw new InternalError("OS not supported");
		}
		
		return IOUtil.createDir(appDir);
	}
	
	/**
	 * Reads a text file from the file system. Each element in the returned
	 * array represents one line of the file.
	 * 
	 * @param file The file.
	 * 
	 * @return The file's contents.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 * @throws IOException if the file does not exist, or an I/O error occurs.
	 */
	public static String[] readTextFile(FileHandle file) throws IOException {
		if(!file.exists())
			throw new IOException("Text resource does not exist!");
		
		BufferedReader br = new BufferedReader(file.reader());
		
		ArrayList<String> strings = new ArrayList<>();
		String s;
		
		try {
			while((s = br.readLine()) != null)
				strings.add(s);
		} finally {
			br.close();
		}
		
		return strings.toArray(new String[0]);
	}
	
	/**
	 * Creates a texture from a .png image of the specified name in the {@link
	 * #IMAGE_DIR image directory}.
	 * 
	 * <p>Usage example:
	 * <pre>Texture myTexture = Resources.texture("myTexture");</pre>
	 * 
	 * @param name The name of the texture source on the filesystem.
	 * 
	 * @return The created texture.
	 */
	public static Texture texture(String name) {
		return new Texture(IMAGE_DIR.child(name + ".png"));
	}
	
	/**
	 * Generates a BitmapFont from the source file relative to the {@link
	 * FONT_DIR font directory}.
	 * 
	 * @param name The name of the font file.
	 * @param param The font parameters. May be {@code null}.
	 * 
	 * @return The generated font.
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public static BitmapFont font(String name, FreeTypeFontParameter param) {
		return font(Resources.FONT_DIR.child(name.endsWith(".ttf") ? name : name + ".ttf"), param);
	}
	
	/**
	 * Generates a BitmapFont from the source file using the specified
	 * parameters.
	 * 
	 * @param file The font file (should be a .ttf file).
	 * @param param The font parameters. May be {@code null}.
	 * 
	 * @return The generated font.
	 * @throws NullPointerException if {@code file} is {@code null}.
	 */
	public static BitmapFont font(FileHandle file, FreeTypeFontParameter param) {
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(file);
		BitmapFont font = gen.generateFont(param);
		gen.dispose();
		return font;
	}
	
	
	// non-instantiable
	private Resources() {}
	
}
