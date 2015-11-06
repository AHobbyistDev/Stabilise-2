package com.stabilise.tests;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.stabilise.util.TaskTimer;
import com.stabilise.util.box.IntBox;
import com.stabilise.util.collect.FragList;
import com.stabilise.util.collect.UnorderedArrayList;

public abstract class FragListTest {
    
    private static int prime1 = 0xced1c241;
    private static int prime2 = 0xbe1f14b1;
    
    private static int hashID(int id) {
        id *= prime1;
        return (id ^ id >>> 13);
    }
    
    private static int hashGen(int gen) {
        gen *= prime2;
        return (gen ^ gen >>> 13);
    }
    
    // --------------------------------------
    
    public static void main(String[] args) {
        for(int i = 0; i < 3; i++)
            doTest(true);
        doTest(false);
    }
    
    public static void doTest(boolean warmup) {
        if(warmup)
            System.out.println("----------WARMUP----------");
        else
            System.out.println("-----------TEST-----------");
        new TestLinkedList().test();
        new TestFragList().test();
        new TestUnorderedArrayList().test();
        if(warmup)
            System.out.println("----------END WARMUP----------");
        else
            System.out.println("-----------END TEST-----------");
    }
    
    // --------------------------------------
    
    public void test() {
        final int spawnerWaves = 256*128;
        final int entitiesPerWave = 25;
        final int finalWaves = 0;
        int id = 0;
        int generation = 0;
        final int killMask = 0b0101010101;
        final IntBox hash = new IntBox(hashGen(generation));
        Predicate<Entity> pred = e -> ((e.hash ^ hash.get()) & killMask) == killMask;
        final TaskTimer t = timer();
        t.start();
        
        for(int i = 0; i < spawnerWaves; i++) {
            for(int j = 0; j < entitiesPerWave; j++) {
                add(new Entity(id++));
            }
            iterate(pred);
            generation++;
            hash.set(hashGen(generation));
        }
        
        for(int i = 0; i < finalWaves; i++) {
            iterate(pred);
            generation++;
            hash.set(hashGen(generation));
        }
        
        t.stop();
        System.out.print(size() + ": ");
        t.printResult(TimeUnit.MILLISECONDS);
    }
    
    abstract TaskTimer timer();
    abstract void add(Entity e);
    abstract void iterate(Predicate<Entity> pred);
    abstract int size();
    
    static final class TestLinkedList extends FragListTest {
        final LinkedList<Entity> list = new LinkedList<>();
        
        @Override TaskTimer timer() { return new TaskTimer("LinkedList"); }
        @Override void add(Entity e) { list.add(e); }
        @Override void iterate(Predicate<Entity> pred) { list.removeIf(pred); }
        @Override int size() { return list.size(); }
    }
    
    static final class TestFragList extends FragListTest {
        final FragList<Entity> list = new FragList<>(1024, 0.22f);
        
        @Override TaskTimer timer() { return new TaskTimer("FragList"); }
        @Override void add(Entity e) { list.put(e); }
        @Override void iterate(Predicate<Entity> pred) { list.iterate(pred); }
        @Override int size() { return list.size(); }
    }
    
    static final class TestUnorderedArrayList extends FragListTest {
        final UnorderedArrayList<Entity> list = new UnorderedArrayList<>(1024, 2);
        
        @Override TaskTimer timer() { return new TaskTimer("UnorderedArrayList"); }
        @Override void add(Entity e) { list.add(e); }
        @Override void iterate(Predicate<Entity> pred) { list.iterate(pred); }
        @Override int size() { return list.size(); }
    }
    
    // --------------------------------------------
    
    private static class Entity {
        private final int id;
        private final int hash;
        
        public Entity(int id) {
            this.id = id;
            this.hash = hashID(id);
        }
        
        public String toString() {
            return "Entity[" + id + "/" + hash + "]";
        }
    }
    
}
