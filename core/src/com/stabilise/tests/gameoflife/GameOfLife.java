package com.stabilise.tests.gameoflife;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.tests.gameoflife.GeneticSim.Result;
import com.stabilise.util.ArrayUtil;
import com.stabilise.util.io.IOUtil;
import com.stabilise.util.io.data.Compression;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Format;


public class GameOfLife {
    
    public static final FileHandle destFolder = new FileHandle("C:/Users/Adam/Documents");
    public static final FileHandle destFile = destFolder.child("GoL_LastResults.nbt");
    
    public static void main(String[] args) throws Exception {
        //simulate();
        //retrieveLastResults();
        sim(Simulation.s24000_12c);
    }
    
    public static void simulate() throws IOException {
        List<Result> results = new GeneticDriver().run();
        
        for(Result r : results) {
            System.out.println(
                    String.format("%5d (%3d/%2d): ", r.mana, r.cellsReduced, r.gen)
                    + r.params + " (score: " + r.score + ")");
        }
        
        // again but no params
        for(Result r : results) {
            System.out.println(
                    String.format("%5d (%3d/%2d)", r.mana, r.cellsReduced, r.gen));
        }
        
        DataCompound saveData = DataCompound.create();
        DataList resList = saveData.createList("results");
        for(Result r : results) {
            resList.add(ArrayUtil.to1D(r.reducedBoard));
        }
        
        IOUtil.write(saveData, Format.NBT_SIMPLE, Compression.UNCOMPRESSED, destFile);
    }
    
    public static void retrieveLastResults() throws IOException {
        List<Result> results = new ArrayList<>();
        
        DataCompound saveData = IOUtil.read(Format.NBT_SIMPLE, Compression.UNCOMPRESSED, destFile);
        DataList resList = saveData.getList("results");
        for(int i = 0; i < resList.size(); i++) {
            results.add(new Result(ArrayUtil.to2D(resList.getIntArr(), Simulation.size, Simulation.size)));
        }
        
        for(Result r : results)
            r.print();
    }
    
    public static void sim(String board) {
        Simulation s = new Simulation(board);
        s.playSim(300);
        new Result(s, null).print();
    }
    
}
