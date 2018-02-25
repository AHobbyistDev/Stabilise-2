package com.stabilise.core.console;

import java.util.Arrays;

import com.stabilise.world.World;

/**
 * Simple test command.
 */
public class CmdTest extends Command {
    
    public CmdTest() {
        
    }
    
    @Override
    public boolean exec(World world, String[] args) {
        System.out.println("Hello world: " + Arrays.toString(args));
        return true;
    }
    
}
