package com.stabilise.core.main;

import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.state.LauncherState;
import com.stabilise.core.state.State;


public class Launcher extends Application {
    
    public Launcher() {
        super(Constants.TICKS_PER_SECOND);
    }
    
    @Override
    protected State getInitialState() {
        return new LauncherState();
    }
    
}
