package com.stabilise.tests.gameoflife;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

import com.stabilise.tests.gameoflife.GeneticSim.Result;
import com.stabilise.util.ArrayUtil;


public class GeneticSim implements Callable<Result> {
    
    public static class Params {
        public final float filledGenChance;
        public final float mutBestChance;
        public final float mutChance;
        public final float prefWeakerChance;
        public final int centreClearDist;
        
        public Params(float fillChance, float mutBestChance, float mutationChance,
                float preferWeakerChance, int centreClearDist) {
            this.filledGenChance = fillChance;
            this.mutBestChance = mutBestChance;
            this.mutChance = mutationChance;
            this.prefWeakerChance = preferWeakerChance;
            this.centreClearDist = centreClearDist;
        }
        
        public String toString() {
            return String.format("%.2f", filledGenChance) + " / " 
                    + String.format("%.2f", mutBestChance) + " / "
                    + String.format("%.2f", mutChance) + " / "
                    + String.format("%.2f", prefWeakerChance) + " / "
                    + String.format("%2d", centreClearDist);
        }
    }
    
    public static class Result implements Comparable<Result> {
        
        public final Params params;
        public final int[][] board;
        public final int[][] reducedBoard;
        public final int mana;
        public final float score;
        public final int gen;
        public final int cells;
        public final int cellsReduced;
        
        public Result(int[][] board) {
            this(Simulation.buildSim(board), null);
        }
        
        public Result(Simulation s, Params p) {
            this.params = p;
            this.board = ArrayUtil.deepCopy(s.initial);
            this.reducedBoard = Simulation.reduce(s.initial);
            this.mana = s.mana;
            this.score = s.fitness;
            this.gen = s.age;
            this.cells = Simulation.countCells(board);
            this.cellsReduced = Simulation.countCells(reducedBoard);
        }
        
        @Override
        public int compareTo(Result o) {
            return Float.compare(o.score, score);
        }
        
        public void print() {
            System.out.println("Score " + score + " at gen " + gen + " for "
                    + mana + " mana (" + cells + " cells, " + cellsReduced + " reduced):");
            //System.out.println("Double-check: " + Simulation.runSim(board));
            //Simulation.printBoard(board);
            System.out.println("Reduced board: " + Simulation.runSim(reducedBoard));
            Simulation.printBoard(reducedBoard);
        }
        
    }
    
    public static final int nSim = 18; //24
    public static final int generations = 1024; //16*1024
    
    public final Params params;
    private final Simulation[] sims = new Simulation[nSim];
    private final Random rnd = new Random();
    
    public GeneticSim(Params params) {
        this.params = params;
        
        for(int i = 0; i < nSim; i++) {
            sims[i] = new Simulation();
            //if(i == 0)
            //    sims[i].board = Simulation.deserialize(Simulation.s34200_10c);
            //else
            sims[i].fillRandom(rnd, params.filledGenChance, params.centreClearDist);
        }
    }
    
    @Override
    public Result call() {
        Simulation best = new Simulation();
        for(int i = 0; i < generations; i++) {
            for(Simulation s : sims) {
                if(s.run() > best.fitness) {
                    best = s.clone();
                }
            }
            
            Arrays.sort(sims);
            
            Simulation[] newSims = new Simulation[nSim];
            int newCount = 0;
            int backCount = nSim-1;
            
            int[] keep = { 0, 1, 2, 3, 4, 5, 6, 10 };
            // Do only minor modifications to keep-y ones
            for(int j = 0; j < nSim; j++) {
                for(int k : keep) {
                    if(j == k) {
                        sims[j].set(sims[j].initial, c -> rnd.nextFloat() < params.mutBestChance ? 1 - c : c);
                        newSims[newCount++] = sims[j];
                        break;
                    }
                }
            }
            
            // Breed the top with the 2nd, 3rd, 4th, middle-th, middle+1-th, store in worst 5
            newSims[newCount++] = breed(sims[0], sims[1], sims[backCount--]);
            newSims[newCount++] = breed(sims[0], sims[2], sims[backCount--]);
            newSims[newCount++] = breed(sims[0], sims[3], sims[backCount--]);
            newSims[newCount++] = breed(sims[0], sims[nSim/2], sims[backCount--]);
            newSims[newCount++] = breed(sims[0], sims[nSim/2+1], sims[backCount--]);
            // Breed 2nd with 3rd
            newSims[newCount++] = breed(sims[1], sims[2], sims[backCount--]);
            // Reduce and reuse the best one
            //newSims[newCount++] = reduce(sims[0], sims[backCount--]);
            
            // Fill up the rest with randoms
            while(newCount < nSim) {
                Simulation s = null;
                for(int k : keep) {
                    if(backCount == k) {
                        s = new Simulation();
                        backCount--;
                        break;
                    }
                }
                if(s == null)
                    s = sims[backCount--];
                s.fillRandom(rnd, params.filledGenChance, params.centreClearDist);
                newSims[newCount++] = s;
            }
            
            for(int j = 0; j < nSim; j++) {
                sims[j] = newSims[j];
                sims[j].reset();
            }
        }
        
        return new Result(best, params);
    }
    
    private Simulation breed(Simulation s1, Simulation s2, Simulation tgt) {
        // ensure s1 is fitter than s2
        if(s1.fitness < s2.fitness) {
            Simulation tmp = s1;
            s1 = s2;
            s2 = tmp;
        }
        
        for(int y = 0; y < Simulation.size; y++) {
            for(int x = 0; x < Simulation.size; x++) {
                int c1 = s1.initial[y][x];
                int c2 = s2.initial[y][x];
                if(c1 == c2) {
                    tgt.board[y][x] = rnd.nextFloat() < params.mutChance ? 1 - c1 : c1;
                } else {
                    tgt.board[y][x] = rnd.nextFloat() < params.prefWeakerChance ? c2 : c1;
                }
            }
        }
        
        return tgt;
    }
    
    @SuppressWarnings("unused")
    private static Simulation reduce(Simulation s, Simulation dest) {
        dest.board = Simulation.reduce(s.initial);
        return dest;
    }
    
}
