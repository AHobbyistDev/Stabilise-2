package com.stabilise.util;


/**
 * Runtime exception thrown to indicate that the feature is on my TODO list.
 */
public class TODOException extends RuntimeException {
    
    private static final long serialVersionUID = -549151175232305444L;
    
    public TODOException() {
        
    }
    
    public TODOException(String msg) {
        super(msg);
    }
    
}
