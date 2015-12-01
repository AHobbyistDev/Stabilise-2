package com.stabilise.util.collect.registry;

import com.stabilise.util.Log;

/**
 * Defines policies for handling duplicate map or registry entries.
 */
public enum DuplicatePolicy {
    
    /** Duplicate entries override old ones using this policy. */
    OVERWRITE {
        @Override
        boolean handle(Log log, String msg) {
            log.postWarning(msg + "; replacing old mapping");
            return false;
        }
    },
    /** Duplicate entries are rejected using this policy. */
    REJECT {
        @Override
        boolean handle(Log log, String msg) {
            log.postWarning(msg + "; ignoring new mapping");
            return true;
        }
    },
    /** Duplicate entries are rejected through an {@code
     * IllegalArgumentException} being thrown using this policy. */
    THROW_EXCEPTION {
        @Override
        boolean handle(Log log, String msg) {
            throw new IllegalArgumentException(msg + "; rejecting new mapping");
        }
    };
    
    /**
     * Handles an attempt to register a duplicate entry.
     * 
     * @param log The logging agent.
     * @param msg The details of the attempt to register a duplicate.
     * 
     * @return {@code true} if the duplicate should be ignored; {@code false}
     * if it should be allowed to override the old entry.
     * @throws IllegalArgumentException if this is the {@link #THROW_EXCEPTION}
     * policy.
     */
    abstract boolean handle(Log log, String msg);
    
}
