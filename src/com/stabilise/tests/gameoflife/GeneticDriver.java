package com.stabilise.tests.gameoflife;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.tests.gameoflife.GeneticSim.Params;
import com.stabilise.tests.gameoflife.GeneticSim.Result;


public class GeneticDriver {
    
    private int tasks;
    private final AtomicInteger done = new AtomicInteger();
    
    public List<Result> run() {
        int ncpu = Runtime.getRuntime().availableProcessors();
        
        ExecutorService exec = Executors.newFixedThreadPool(ncpu/2);
        
        List<Future<Result>> futures = new ArrayList<>(ncpu);
        List<Result> results = new ArrayList<>(ncpu);
        
        Params[] params = genParams();
        tasks = params.length;
        for(Params p : params)
            futures.add(submit(exec, p));
        
        for(Future<Result> f : futures) {
            try {
                results.add(f.get());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        results.sort(null);
        results.get(0).print();
        
        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    private Future<Result> submit(ExecutorService exec, Params p) {
        GeneticSim sim = new GeneticSim(p);
        return exec.submit(() -> {
            Result r = sim.call();
            System.out.println(done.incrementAndGet() + " / " + tasks + " done.");
            return r;
        });
    }
    
    private Params[] genParams() {
        float[] fillCh = { 0.10f };
        float[] mutBestCh = { 0.01f, 0.1f, 0.25f };
        float[] mutCh = { 0.01f, 0.1f, 0.20f };
        float[] prefWeakCh = { 0.05f };
        int[] centreClearDist = { 2, 16 };
        int len = fillCh.length * mutBestCh.length * mutCh.length * prefWeakCh.length * centreClearDist.length;
        System.out.println(len + " different param combinations");
        Params[] p = new Params[len];
        int i = 0;
        for(float p1 : fillCh) {
            for(float p2 : mutBestCh) {
                for(float p3 : mutCh) {
                    for(float p4 : prefWeakCh) {
                        for(int p5 : centreClearDist) {
                            p[i++] = new Params(p1, p2, p3, p4, p5);
                        }
                    }
                }
            }
        }
        return p;
    }
    
}
