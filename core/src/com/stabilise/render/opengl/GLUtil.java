package com.stabilise.render.opengl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;


public class GLUtil {
    
    private GLUtil() {}
    
    
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
    
}
