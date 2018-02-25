package com.stabilise.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.io.IOUtil;

/**
 * Converts all tabs to spaces
 */
public class Spaceifier {
    
    String dir, tgt;
    
    /**
     * @param dir source dir
     */
    public Spaceifier(String dir, String tgt) throws IOException {
        this.dir = dir.replace("\\", "/");
        this.tgt = tgt.replace("\\", "/");
        File f = new File(dir);
        doTheThing(f);
    }
    
    void doTheThing(File f) throws IOException {
        String correctedPath = f.getPath().replace("\\", "/");
        if(f.isDirectory())
            doDir(f);
        else
            doFile(f, correctedPath);
    }
    
    void doDir(File dir) throws IOException {
        File[] files = dir.listFiles();
        for(File f : files)
            doTheThing(f);
    }
    
    void doFile(File file, String path) throws IOException {
        if(!path.endsWith(".java"))
            return;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        
        String outPath = path.replace(dir, tgt);
        File out = new File(outPath);
        if(out.exists())
            out.delete();
        IOUtil.createParentDir(new FileHandle(out));
        FileOutputStream fos = new FileOutputStream(out);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        
        int ch;
        while((ch = br.read()) != -1) {
            if(ch == '\t')
                bw.append("    ");
            else
                bw.append((char)ch);
        }
        
        br.close();
        isr.close();
        fis.close();
        
        bw.close();
        osw.close();
        fos.close();
    }
    
    public static void main(String[] args) throws IOException {
        /*
        new Spaceifier(
                "C:/Users/Adam/Documents/GitHub/Stabilise-2/Stabilise 2/core/src",
                "C:/Users/Adam/Documents/GitHub/Stabilise-2/Stabilise 2/core/src2"
        );
        */
    }
    
}
