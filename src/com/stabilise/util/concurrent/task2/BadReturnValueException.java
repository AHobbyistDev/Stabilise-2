package com.stabilise.util.concurrent.task2;

/**
 * A {@code BadReturnValueException} is thrown during task execution if a
 * value-returning task unit returns {@code null}.
 */
public class BadReturnValueException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    BadReturnValueException(String msg) {
        super(msg);
    }
    
}
