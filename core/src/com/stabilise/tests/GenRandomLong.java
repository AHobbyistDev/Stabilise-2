package com.stabilise.tests;

import java.util.Random;


public class GenRandomLong {
    
    public static void main(String[] args) {
        System.out.println(Long.toHexString(new Random().nextLong()));
    }
    
}
