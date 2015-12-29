package com.stabilise.core.main;

import com.stabilise.core.Constants;
import com.stabilise.core.app.Application;
import com.stabilise.core.state.State;


public class Launcher extends Application {
    
    public Launcher() {
        super(Constants.TICKS_PER_SECOND);
    }

    @Override
    protected State getInitialState() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
