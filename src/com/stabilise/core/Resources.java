package com.stabilise.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.stabilise.util.Log;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Format;

/**
 * Manages application resources.
 */
public class Resources {
    
    public static final FileHandle
            /** The application's root working directory. */
            DIR_APP = getApplicationPath("stabilise"),
            
            /** The config file directory. */
            DIR_CONFIG = DIR_APP.child("config/"),
            
            /** Root directory for save data. */
            DIR_SAVES = DIR_APP.child("saves/"),
            DIR_CHARS = DIR_SAVES.child("chars/"),
            DIR_WORLDS = DIR_SAVES.child("worlds/"),
            
            /** Root directory for application resources e.g. images, sounds. */
            DIR_RESOURCES = DIR_APP.child("res/"),
            DIR_IMG = DIR_RESOURCES.child("img/"),
            DIR_FONT = DIR_RESOURCES.child("fonts/"),
            DIR_SOUND = DIR_RESOURCES.child("sound/"),
            
            /** The file directory for mods. */
            DIR_MODS = DIR_APP.child("mods/"),
            
            /** The directory in which console output logs should be saved. */
            DIR_LOG = DIR_APP.child("log/");
    
    
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
        File appDir = null;
        
        if(os.contains("windows")) {
            String appDataDir = System.getenv("APPDATA");
            if(appDataDir != null)
                appDir = new File(appDataDir, appName);
            else
                appDir = new File(dir, appName);
        } else if(os.contains("mac")) {
            appDir = new File(dir, "Library/Application Support/" + appName);
        } else if(os.contains("linux")) {
            appDir = new File(dir, appName);
        } else {
            Log.get().postSevere("OS not supported");
            throw new InternalError("OS not supported");
        }
        
        //return Gdx.files.external(appDir.getPath());
        return new FileHandle(appDir);
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
    public static List<String> readTextFile(FileHandle file) throws IOException {
        if(!file.exists())
            throw new IOException("Text resource does not exist!");
        
        BufferedReader br = new BufferedReader(file.reader());
        
        List<String> strings = new ArrayList<>();
        String s;
        
        try {
            while((s = br.readLine()) != null)
                strings.add(s);
        } finally {
            br.close();
        }
        
        return strings;
    }
    
    /**
     * Creates a texture from a .png image of the specified name in the {@link
     * #DIR_IMG image directory}.
     * 
     * <p>Usage example:
     * <pre>Texture myTexture = Resources.texture("myTexture");</pre>
     * 
     * @param name The name of the texture source on the filesystem.
     * 
     * @return The created texture.
     */
    public static Texture texture(String name) {
        return new Texture(DIR_IMG.child(name + ".png"));
    }
    
    /**
     * Creates a texture from a .png image of the specified name in the {@link
     * #DIR_IMG image directory}. The texture will be generated with mipmaps.
     * 
     * <p>Usage example:
     * <pre>Texture myTexture = Resources.texture("myTexture");</pre>
     * 
     * @param name The name of the texture source on the filesystem.
     * 
     * @return The created texture.
     */
    public static Texture textureMipmaps(String name) {
        return new Texture(DIR_IMG.child(name + ".png"), true);
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
        return font(DIR_FONT.child(name.endsWith(".ttf") ? name : name + ".ttf"), param);
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
