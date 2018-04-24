package com.stabilise.tests;

import java.util.Random;


/**
 * Generates random longs for me to use as random seed mixers.
 */
public class GenRandomLong {
    
    public static void main(String[] args) {
        System.out.println(Long.toHexString(new Random().nextLong()));
    }
    
}
