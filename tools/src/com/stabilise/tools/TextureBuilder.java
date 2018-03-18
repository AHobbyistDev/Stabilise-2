package com.stabilise.tools;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.stabilise.core.Resources;


public class TextureBuilder {
    
    public static void main(String[] args) {
        Settings settings = new Settings();
        settings.combineSubdirectories = true;
        settings.duplicatePadding = true;
        FileHandle inputDir = Resources.DIR_IMG.child("raw/ingame/");
        FileHandle outputDir = Resources.DIR_IMG;
        TexturePacker.process(settings, inputDir.path(), outputDir.path(), "atlasIngame");
    }
    
}
