package com.stabilise.core.console;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public abstract class Command {
    
    private static final char cmdChar = '/';
    private static final char groupingChar = '"';
    private static final NavigableMap<String, Command> commands = new TreeMap<>();
    
    public Command() {
        
    }
    
    public static String[] interpret(char[] cmd) {
        if(cmd.length == 0 || cmd[0] != cmdChar)
            return new String[0];
        
        List<String> args = new ArrayList<>();
        int start = 1, cur = 0;
        boolean quotes = false, whitespace = false;
        char c;
        
        while(++cur < cmd.length) {
            c = cmd[cur];
            if(c == ' ') {
                if(!quotes && !whitespace) {
                    append(args, cmd, start, cur);
                    whitespace = true;
                }
            } else if(c == groupingChar) {
                if(quotes) {
                    // This is the endquote
                    quotes = false;
                    append(args, cmd, start, cur);
                    whitespace = true; // functional reset
                } else if(whitespace) {
                    // This is the startquote
                    quotes = true;
                    whitespace = false;
                    start = cur + 1; // start with the char after the quote
                }
            } else {
                if(whitespace && !quotes) {
                    whitespace = false;
                    start = cur;
                }
            }
        }
        
        append(args, cmd, start, cur); // cur == cmd.length
        
        return args.toArray(new String[args.size()]);
    }
    
    private static void append(List<String> list, char[] c, int start, int end) {
        if(start - end == 0)
            return;
        list.add(new String(c, start, end - start));
    }
    
}
