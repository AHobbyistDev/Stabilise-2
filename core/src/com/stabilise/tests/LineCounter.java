package com.stabilise.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * counts the total number of lines of code in this project
 */
public class LineCounter {
    
    String dir;
    String[] ignores;
    int lineTotal = 0;
    int realLineTotal = 0;
    int classTotal = 0;
    
    /**
     * @param dir source dir
     * @param ignores any filepath containing any of these strings will be ignored
     */
    public LineCounter(String dir, String... ignores) throws IOException {
        this.dir = dir;
        this.ignores = ignores;
        File f = new File(dir);
        doTheThing(f);
        System.out.println("\nTotal: " + realLineTotal + "/" + lineTotal + " (" + classTotal + ")");
    }
    
    void doTheThing(File f) throws IOException {
        String correctedPath = f.getPath().replace("\\", "/");
        for(String s : ignores)
            if(correctedPath.contains(s))
                return;
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
        int lines = 0, realLines = 0;
        String s;
        while((s = br.readLine()) != null) {
            lines++;
            lineTotal++;
            if(!isComment(s)) {
                realLines++;
                realLineTotal++;
            }
        }
        br.close();
        isr.close();
        fis.close();
        classTotal++;
        System.out.println(String.format("%4d  %4d - %s", realLines, lines, path.replace(dir, "")));
    }
    
    private boolean isComment(String s) {
        boolean whitespace = true;
        char c;
        for(int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if(whitespace) {
                if(c == '*') // interpret as multiline comment
                    return true;
                else if(c == '/')
                    whitespace = false;
                else if(c != ' ')
                    return false;
            } else {
                return c == '/' || c == '*'; // "//" or "/*"
            }
        }
        return true; // blank line is considered a comment
    }
    
    public static void main(String[] args) throws IOException {
        new LineCounter(
                "C:/Users/Adam/Documents/GitHub/Stabilise-2/core/src",
                //"C:/Users/Administrator/Documents/Java/Stabilise II/core/src",
                new String[] {
                        "com/stabilise/tests",
                }
        );
    }
    
}
