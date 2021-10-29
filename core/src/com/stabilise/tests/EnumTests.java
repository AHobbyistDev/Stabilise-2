package com.stabilise.tests;

import java.util.ArrayList;
import java.util.List;

public class EnumTests {
    
    interface IThingo {
        int getID();
    }
    
    
    enum Bing implements IThingo {
        A(0),
        B(1),
        C(2);
        
        
        private final int id;
        Bing(int id) { this.id = id; StaticStuff.arr[id] = 5; System.out.println(this.ordinal()); }
        public int getID() { return id; }
        
        static class StaticStuff {
            static int[] arr = new int[3];
        }
    }
    
    enum Bong implements IThingo {
        X(1),
        Y(2),
        Z(3);
        
        private final int id;
        Bong(int id) { this.id = id; }
        public int getID() { return id; }
    }
    
    
    
    static class Manager<T extends Enum<T> & IThingo> {
        
        Class<T> clazz;
        int[] ids;
        ITarget<T> target;
    
        Manager(Class<T> clazz, ITarget<T> target) {
            this.clazz = clazz;
            T[] arr = clazz.getEnumConstants();
            ids = new int[arr.length];
            for(int i = 0; i < arr.length; i++)
                ids[i] = arr[i].getID();
            this.target = target;
        }
        
        void test() {
            for(T t : clazz.getEnumConstants())
                target.accept(t);
        }
        
    }
    
    interface ITarget<T extends Enum<T> & IThingo> {
        void accept(T t);
    }
    
    static class Target implements ITarget<Bing> {
        public Target() {}
        
        public void accept(Bing bing) {
            switch(bing) {
                case A:
                    System.out.println("That's A Bing-o!");
                    break;
                case B:
                    System.out.println("B is for Bing");
                    break;
                case C:
                    System.out.println("CBing");
                    break;
            }
        }
    }
    
    public static void main(String[] args) {
        Target t = new Target();
        Manager<Bing> bingManager = new Manager<>(Bing.class, t);
        Manager<Bong> bongManager = new Manager<>(Bong.class, null);
        //Manager<Bong> bongManager2 = new Manager<>(Bing.class, null); // doesn't work - good!
        //Manager<Bong> bongManager3 = new Manager<>(Bong.class, t); // doesn't work - good!
        bingManager.test();
        
        System.out.println(Bing.StaticStuff.arr[0]);
    }
    
}
