package com.stabilise.desktop;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.stabilise.core.Resources;

public class TextureBuilder {
    
    public static void main(String[] args) {
        Settings settings = new Settings();
        settings.duplicatePadding = true;
        settings.combineSubdirectories = true;
        FileHandle inputDir = Resources.DIR_IMG.child("raw/ingame/");
        FileHandle outputDir = Resources.DIR_IMG;
        TexturePacker.process(settings, inputDir.path(), outputDir.path(), "atlasIngame");
    }
    
}
