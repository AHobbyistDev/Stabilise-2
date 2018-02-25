package com.stabilise.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Very simple console program to help memorise course content by quizzing
 * the user over the contents of specified text files of the form:
 * 
 * <pre>
 * answer1::question1
 * answer2::question2
 * ...
 * answern::questionn</pre>
 * 
 * e.g.,
 * 
 * <pre>
 * Injective::Synonym for 1-1 function
 * Surjective::Range(f) == Codom(f)
 * Bijective::Injective and surjective
 * Bijective::Condition for invertibility
 * </pre>
 */
public class Memorisation {
    
    public static void main(String[] args) {
        new Memorisation().start();
    }
    
    // ------------------------------------------------------------------------
    
    private final BufferedReader consoleIn =
            new BufferedReader(new InputStreamReader(System.in));
    
    private File workingDir;
    
    
    public Memorisation() {
        
    }
    
    public void start() {
        File dir;
        while(true) {
            print("Enter working directory");
            String s = read();
            if(s.equalsIgnoreCase("x")) {
                if(confirm("quit"))
                    return;
            } else {
                dir = new File(s);
                if(dir.isDirectory())
                    break;
            }
        }
        workingDir = dir;
        selectQuestionSet:
        while(true) {
            File f = selectFile();
            if(f == null) // user quit
                return;
            FileContents c = new FileContents(f);
            while(true) {
                for(int i = 0; i < c.questions.length; i++) {
                    if(askQuestion(c.questions[i]))
                        continue selectQuestionSet;
                }
                print("Question set completed!");
                if(!confirm("try again"))
                    continue selectQuestionSet;
                c.shuffleQuestions();
            }
        }
    }
    
    private File selectFile() {
        File[] files = workingDir.listFiles();
        listOptions:
        while(true) {
            print("> Select one, or X to close:");
            for(int i = 0; i < files.length; i++)
                print(i + " - " + files[i].getName());
            while(true) {
                String s = read();
                try {
                    int i = tryReadInt(s);
                    if(i >= 0 && i < files.length)
                        return files[i];
                    else
                        print("Number not in range 0 - " + (files.length - 1) + "!");
                } catch(NumberFormatException e) {
                    if(s.equalsIgnoreCase("x")) {
                        if(confirm("quit"))
                            return null;
                        else
                            continue listOptions;
                    } else
                        print("Invalid number!");
                }
            }
        }
    }
    
    private boolean askQuestion(StringPair q) {
        while(true) {
            print(q.question);
            String s = read();
            if(s.equalsIgnoreCase(q.answer)) {
                print("Correct!");
                return false;
            } else if((s.equalsIgnoreCase("quit") || s.equalsIgnoreCase("exit")
                    || s.equalsIgnoreCase("close") || s.equalsIgnoreCase("end"))) {
                if(confirm("exit these questions"))
                    return true;
                continue;
            } else {
                print("Incorrect. The answer is \"" + q.answer + "\"");
                return false;
            }
        }
    }
    
    private void print(String out) {
        System.out.println(out);
    }
    
    private String read() {
        try {
            return consoleIn.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private int tryReadInt(String s) throws NumberFormatException {
        return Integer.parseInt(s);
    }
    
    private boolean confirm(String thingToDo) {
        print("Are you sure you want to " + thingToDo + "? y/n");
        while(true) {
            String s = read();
            if(s.equalsIgnoreCase("y"))
                return true;
            else if(s.equalsIgnoreCase("n"))
                return false;
        }
    }
    
    private static class FileContents {
        
        public final StringPair[] questions;
        
        public FileContents(File f) {
            String[] lines = readFile(f);
            StringPair[] q = new StringPair[lines.length];
            int validQuestions = 0;
            for(int i = 0; i < lines.length; i++) {
                StringPair p = parseQuestion(lines[i]);
                if(p == null)
                    continue;
                q[validQuestions++] = p;
            }
            questions = Arrays.copyOf(q, validQuestions);
            shuffleQuestions();
        }
        
        public void shuffleQuestions() {
            randomSwapSort(questions, 4*questions.length);
        }
        
        private String[] readFile(File f) {
            try {
                InputStream is = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                
                ArrayList<String> strings = new ArrayList<>();
                String s;
                
                try {
                    while((s = br.readLine()) != null)
                        strings.add(s);
                } finally {
                    br.close();
                    isr.close();
                    is.close();
                }
                
                return strings.toArray(new String[0]);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        private StringPair parseQuestion(String s) {
            String[] strs = s.split("::");
            if(strs.length != 2)
                return null;
            return new StringPair(strs[1].trim(), strs[0].trim());
        }
        
        private void randomSwapSort(StringPair[] p, int iterations) {
            Random rnd = new Random();
            for(int i = 0; i < iterations; i++) {
                int ind1 = rnd.nextInt(p.length);
                int ind2 = rnd.nextInt(p.length);
                StringPair temp = p[ind1];
                p[ind1] = p[ind2];
                p[ind2] = temp;
            }
        }
        
    }
    
    private static class StringPair {
        public final String question, answer;
        public StringPair(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
    
}
