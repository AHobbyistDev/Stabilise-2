package com.stabilise.util.collect;


public interface IDuplicateResolver<T> {
    
    public static enum Action {
        OVERWRITE, REJECT, KEEP_BOTH
    }
    
    Action resolve(T other);
    
}
