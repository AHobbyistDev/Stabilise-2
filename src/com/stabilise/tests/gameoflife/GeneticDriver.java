package com.stabilise.tests.gameoflife;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.stabilise.tests.gameoflife.GeneticSim.Params;
import com.stabilise.tests.gameoflife.GeneticSim.Result;


public class GeneticDriver {
    
    public List<Result> run() {
        int ncpu = Runtime.getRuntime().availableProcessors();
        
        ExecutorService exec = Executors.newFixedThreadPool(ncpu);
        
        List<Future<Result>> futures = new ArrayList<>(ncpu);
        List<Result> results = new ArrayList<>(ncpu);
        
        for(Params p : genParams())
            futures.add(exec.submit(new GeneticSim(p)));
        
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
    
    private Params[] genParams() {
        float[] fillCh = { 0.1f, 0.5f, 0.8f };
        float[] mutBestCh = { 0.01f, 0.08f, 0.25f };
        float[] mutCh = { 0.15f, 0.5f };
        float[] prefWeakCh = { 0.15f };
        int[] centreClearDist = { 8 };
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
