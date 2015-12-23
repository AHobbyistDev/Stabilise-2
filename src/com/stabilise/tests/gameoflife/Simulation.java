package com.stabilise.tests.gameoflife;

import java.util.Random;
import java.util.function.IntUnaryOperator;

import com.stabilise.util.ArrayUtil;


public class Simulation implements Comparable<Simulation> {
    
    /*
     * Rules:
     * 0-1 neighbours, it will die
     * 2 neighbours, stay alive
     * 3 neighbours, stay alive, or come to life
     * 4 or more neighbours, it will die
     * Neighbours are defined as adjacent in all 8 directions
     * 
     * can't spawn in 3x3 about centre
     * board wiped as soon as something reaches centre 3x3
     * mana granted per cell = 150*lifetime
     */
    
    public static final int range = 12;
    public static final int size = 2*range + 1;
    public static final int maxLife = 60;
    public static final int manaPerGen = 150;
    public static final int eatRange = 1;
    public static final int noSpawnRange = 1;
    
    public int age = 0;
    public int[][] prev = new int[size][size];
    public int[][] board = new int[size][size];
    public int[][] initial;
    public int mana = 0;
    public int fitness = 0;
    
    public Simulation() {
        
    }
    
    public Simulation(int[][] board) {
        this.board = board;
    }
    
    public Simulation(String board) {
        this(deserialize(board, 'O'));
    }
    
    public void reset() {
        age = 0;
        mana = 0;
        fitness = 0;
    }
    
    public void fillRandom(Random rnd, float chance, int centreClearDist) {
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                board[y][x] = rnd.nextFloat() < chance ? 1 : 0;
            }
        }
        reduceCentrals(rnd, centreClearDist);
    }
    
    private void reduceCentrals(Random rnd, int centreClearDist) {
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                int dx = Math.abs(x - range);
                int dy = Math.abs(y - range);
                int r = dx + dy;
                if(rnd.nextInt(centreClearDist) >= r)
                    board[y][x] = 0;
            }
        }
    }
    
    private void ensureInvariants() {
        for(int y = range-noSpawnRange; y <= range+noSpawnRange; y++) {
            for(int x = range-noSpawnRange; x <= range+noSpawnRange; x++) {
                board[y][x] = 0;
            }
        }
    }
    
    public int run() {
        begin();
        while(step()) {}
        genFitness();
        return fitness;
    }
    
    public void begin() {
        ensureInvariants();
        initial = ArrayUtil.deepCopy(board);
    }
    
    public void playSim(int timestepMillis) {
        begin();
        do {
            print();
            try {
                Thread.sleep(timestepMillis);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        } while(step());
        print();
        genFitness();
    }
    
    /** @return true to keep going; false if finished */
    public boolean step() {
        age++;
        
        int count = 0;
        int[][] tmp = prev;
        prev = board;
        board = tmp;
        
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                int c = numAdjacent(prev, x, y, 1);
                if(c == 3 || (c == 2 && prev[y][x] == 1)) {
                    board[y][x] = 1;
                    count++;
                } else {
                    board[y][x] = 0;
                }
            }
        }
        
        if(count == 0)
            return false;
        
        int winners = 0;
        for(int y = range-eatRange; y <= range+eatRange; y++) {
            for(int x = range-eatRange; x <= range+eatRange; x++) {
                winners += board[y][x];
            }
        }
        
        if(winners != 0 || age == maxLife) {
            mana = winners * age * manaPerGen;
            return false;
        }
        
        return true;
    }
    
    private static int numAdjacent(int[][] b, int x, int y, int range) {
        int count = -b[y][x];
        for(int r = Math.max(y-range, 0); r <= Math.min(y+range, size-1); r++) {
            for(int c = Math.max(x-range, 0); c <= Math.min(x+range, size-1); c++) {
                count += b[r][c];
            }
        }
        return count;
    }
    
    private static int highestAdjacent(int[][] b, int x, int y, int range) {
        int max = 0;
        for(int r = Math.max(y-range, 0); r <= Math.min(y+range, size-1); r++) {
            for(int c = Math.max(x-range, 0); c <= Math.min(x+range, size-1); c++) {
                if(!(r == y && c == x) && b[r][c] > max)
                    max = b[r][c];
            }
        }
        return max;
    }
    
    private void genFitness() {
        fitness = mana - countCells(initial) - age;
        //fitness = (int)(mana / Math.sqrt(countCells(initial)));
        //fitness = (int)(mana / (double) countCells(initial) + Math.sqrt((maxLife - age) * 200));
    }
    
    @Override
    public int compareTo(Simulation o) {
        return o.fitness - fitness;
    }
    
    public void printInitial() {
        printBoard(initial);
    }
    
    public void print() {
        printBoard(board);
    }
    
    public String toString() {
        return "Score " + fitness + " at gen " + age + " for " + mana + " mana:\n" + serialize(initial);
    }
    
    public Simulation clone() {
        Simulation s = new Simulation();
        s.age = age;
        s.board = ArrayUtil.deepCopy(board);
        s.initial = ArrayUtil.deepCopy(initial);
        s.mana = mana;
        s.fitness = fitness;
        return s;
    }
    
    public void forEach(IntUnaryOperator action) {
        set(board, action);
    }
    
    public void set(int[][] base, IntUnaryOperator action) {
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                board[y][x] = action.applyAsInt(base[y][x]);
            }
        }
    }
    
    public static String serialize(int[][] b) {
        StringBuilder sb = new StringBuilder(size*(size + 1));
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                int xdist = Math.abs(x - range);
                int ydist = Math.abs(y - range);
                if(b[y][x] == 1)
                    sb.append('O');
                else if(xdist <= eatRange && ydist <= eatRange)
                    sb.append('X');
                else if(xdist <= noSpawnRange && ydist <= noSpawnRange)
                    sb.append('_');
                else
                    sb.append('_');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    public static void printBoard(int[][] b) {
        System.out.println(serialize(b));
    }
    
    public static void printComparison(int[][] b1, int[][] b2) {
        StringBuilder sb = new StringBuilder(size*(size + 1));
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                if(b1[y][x] == b2[y][x]) {
                    int xdist = Math.abs(x - range);
                    int ydist = Math.abs(y - range);
                    if(b1[y][x] == 1)
                        sb.append('O');
                    else if(xdist <= eatRange && ydist <= eatRange)
                        sb.append('X');
                    else if(xdist <= noSpawnRange && ydist <= noSpawnRange)
                        sb.append('_');
                    else
                        sb.append('_');
                } else {
                    sb.append('@');
                }
            }
            sb.append('\n');
        }
        System.out.println(sb.toString());
    }
    
    public static int runSim(int[][] board) {
        return buildSim(ArrayUtil.deepCopy(board)).mana;
    }
    
    public static Simulation buildSim(int[][] board) {
        Simulation s = new Simulation(board);
        s.run();
        return s;
    }
    
    public static int countCells(int[][] board) {
        int count = 0;
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                count += board[y][x];
            }
        }
        return count;
    }
    
    public static int[][] reduce(int[][] board) {
        int[][] newBoard = new int[size][size];
        int[][] adjBoard = new int[size][size];
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                adjBoard[y][x] = numAdjacent(board, x, y, 1);
            }
        }
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                int adj = adjBoard[y][x];
                int max = highestAdjacent(adjBoard, x, y, 1);
                newBoard[y][x] = adj <= 1 && max < 3 ? 0 : board[y][x];
            }
        }
        return newBoard;
    }
    
    public static final String s45000_190c =
            "_O__O_O_O_OOOOOO_O_O___OO" +
            "_O___OO__OOOO_O___OO____O" +
            "O__O_O_____O_O________O__" +
            "_O_OO____O____OO_O___OO_O" +
            "__O__O_______O_______OO_O" +
            "O_OO______O_____O________" +
            "_____O____O__OOOO______O_" +
            "_______O_____O_______O___" +
            "OOOO___OO________O__OOOOO" +
            "______OOO__OO____O__O_O__" +
            "O____OO___________O____O_" +
            "O__OO_OO___XXX_____OO_O_O" +
            "_O____OOO__XXX__O_OO__O_O" +
            "__OO_______XXX__O_O_OO_O_" +
            "__O_____O_O_____OOO____O_" +
            "_____________O_OO___O_OO_" +
            "OO__O_OO__O_O_____O___OO_" +
            "OO_____O_OO_____________O" +
            "OO____O__________O_______" +
            "____O__O_O_________O__O__" +
            "____O_O_O____OO__OO_O_O__" +
            "_O___O_O_OO__O____OO__O_O" +
            "__O_O___O_____OO_O_OO____" +
            "_________OO__OOO__OO_____" +
            "_OO_______OO_________O__O";
    
    public static final String s45000_183c =
            "_O__O_O_O_OOOOOO_O_O___OO" +
            "_O___OO__OOOO_O___OO____O" +
            "O__O_O_____O_O________O__" +
            "_O_OO____O____OO_O___OO_O" +
            "__O__O_______O_______OO_O" +
            "O_OO____________O________" +
            "_____O_______OOOO______O_" +
            "_______O_____O_______O___" +
            "OOOO___OO________O__OOOOO" +
            "______OOO__O_O___O__O_O__" +
            "O____OO___________O____O_" +
            "O__OO_OO___XXX_____OO_O_O" +
            "_O____OOO__XXX__O_OO__O_O" +
            "__OO_______XXX__O_O_OO_O_" +
            "__O_____O_O_____OOO____O_" +
            "_______________OO___O_OO_" +
            "OO__O_OO__O_O_____O___OO_" +
            "_O_____O_OO_____________O" +
            "O_____O__________O_______" +
            "____O__O_O_________O__O__" +
            "____O_O_O____OO__OO_O_O__" +
            "_____O_O_OO__O____OO__O_O" +
            "__O_O___O_____OO_O_OO____" +
            "_________OO__OOO__OO_____" +
            "_OO_______OO_________O___";
    
    public static final String s44250_209c =
            "__O__O__O___OO________OOO" +
            "O____OO_____O________OO__" +
            "_O___O___OO__OO__O_O__O_O" +
            "__O_____O_O_O__OOOOOO_OO_" +
            "_OO__O_____O_O____OOO___O" +
            "O_O_OO___O____O__O______O" +
            "___OOOO_O_O__O___O_O_OO_O" +
            "O____O_O_O__OO_OOOO___O__" +
            "___O_____O___________O_O_" +
            "O_O__O___O_O__O_OOO_OO__O" +
            "O__O__O____________O__OO_" +
            "__O____O___XXXO__________" +
            "_________O_XXXO____O_O__O" +
            "__O__OOO___XXX__O____O_O_" +
            "_______O__O____O___OO__OO" +
            "_O_OO_O__O_O____OO_______" +
            "____OO_O______________O__" +
            "_O__OO__OO_O___OO_____OO_" +
            "OO_O_____O___OOO__OO_OOOO" +
            "_OO__O_O___O_OO___O__O__O" +
            "___OOO__O___OOO_O___O____" +
            "O_O______O__OO__O____O_OO" +
            "O____O_OO_O_O____O____OO_" +
            "OOOO____________OOO___O_O" +
            "____OOO_OO_O______O_____O";
    
    public static final String s42000_40c =
            "_OO______________________" +
            "O________________________" +
            "_________________________" +
            "______O_O________________" +
            "OOO_____O________________" +
            "_______OO________________" +
            "_OO______OO_OO___________" +
            "_________________________" +
            "O___________O____________" +
            "_______________O_________" +
            "__O__________O___________" +
            "___O_______XXXO__________" +
            "_O_________XXX___________" +
            "________O__XXX___________" +
            "O_O______O_______________" +
            "__O______O_______________" +
            "__O______________________" +
            "_________________________" +
            "_________O_______________" +
            "________O______________O_" +
            "_______OOO_____________OO" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________";
    
    public static final String s27000_45c =
            "__OO_________________OO_O" +
            "___________O_O_O______OOO" +
            "__OO___________________O_" +
            "___O______O_O_O__O____OO_" +
            "_O__________O__O_O_O_____" +
            "___O__________________O_O" +
            "___O_____________O_______" +
            "_O_O_____________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "___________XXX___________" +
            "___________XXX___________" +
            "___________XXX___________" +
            "_________________________" +
            "_________________________" +
            "________________________O" +
            "______________________O__" +
            "________________________O" +
            "_O_____________________O_" +
            "_________________________" +
            "_OOO_____________________" +
            "O________________________" +
            "O___O____________________" +
            "O________________________";
    
    public static final String s34200_10c =
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "___________XXX___________" +
            "___________XXX___________" +
            "___________XXX___________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "____________O____________" +
            "___O________O________O___" +
            "__OO_______O_O_______OO__";
    
    public static final String s27000_31c =
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "__________O______________" +
            "__________O______________" +
            "__________O______________" +
            "_________________________" +
            "_O_______________________" +
            "O__________XXX___________" +
            "_O______O__XXX_O_________" +
            "O_____O_O__XXXO__________" +
            "________________O________" +
            "_________O_____________O_" +
            "_____O_O_O____________O__" +
            "_______________O________O" +
            "______O_____OOO__O_______" +
            "_______OO______O_________" +
            "_______________OO________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________";
    
    public static final String s8850_4c =
            "______O__________________" +
            "_________________________" +
            "_____OOO_________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "___________XXX___________" +
            "___________XXX___________" +
            "___________XXX___________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________" +
            "_________________________";
    
    public static int[][] deserialize(String str) {
        return deserialize(str, 'O');
    }
    
    public static int[][] deserialize(String str, char cell) {
        int[][] board = new int[size][size];
        int idx = 0;
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                board[y][x] = str.charAt(idx++) == cell ? 1 : 0;
            }
        }
        return board;
    }
    
}
