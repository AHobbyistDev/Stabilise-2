package com.stabilise.world.structure;

public abstract class Schematic extends Structure {
    
    public int x, y;
    public int width, height;
    
    
    public Schematic() {
        
    }
    
    protected abstract int[][] template();
    
    /**
     * Gets the schematic's file.
     * 
     * @return The schematic's file, or {@code null} if the schematic lacks a
     * name.
     */
    /*
    private FileHandle getFile() {
        if(name == "")
            return null;
        
        return Resources.SCHEMATIC_DIR.child(name + ".schematic");
    }
    */
    
    /**
     * Loads this schematic.
     * 
     * @throws IOException if an I/O error occurs.
     */
    /*
    public void load() throws IOException {
        FileHandle file = getFile();
        
        if(file == null)
            throw new IOException("Attempting to load an unnamed schematic!");
        
        NBTTagCompound schematicTag = NBTIO.readCompressed(file);
        
        height = schematicTag.getShort("height");
        width = schematicTag.getShort("width");
        
        int[] tiles1D = schematicTag.getIntArray("tiles");
        tiles = new int[height][width];
        
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                tiles[r][c] = tiles1D[r * width + c];
            }
        }
        
        x = schematicTag.getInt("x");
        y = schematicTag.getInt("y");
    }
    */
    
    /**
     * Saves the schematic.
     * 
     * @throws IOException if an I/O error occurs.
     */
    /*
    public void save() throws IOException {
        FileHandle file = getFile();
        
        if(file == null)
            throw new IOException("Attempting to save an unnamed schematic!");
        
        NBTTagCompound schematicTag = new NBTTagCompound();
        
        //byte height = (byte)tiles.length;
        //byte width = (byte)tiles[0].length;
        
        schematicTag.addShort("height", height);
        schematicTag.addShort("width", width);
        
        int[] tiles1D = new int[height * width];
        
        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                tiles1D[r * width + c] = tiles[r][c];
            }
        }
        
        schematicTag.addIntArray("tiles", tiles1D);
        
        schematicTag.addInt("x", x);
        schematicTag.addInt("y", y);
        
        NBTIO.writeCompressed(file, schematicTag);
    }
    */
    
    /*
    public static final Schematic TREE_1 = new Schematic(ArrayUtil.flip2DIntArray(new int[][] {
            { -1,-1, 5, 5, 5,-1,-1 },
            { -1, 5, 5, 5, 5, 5,-1 },
            {  5, 5, 5, 4, 5, 5, 5 },
            {  5, 5, 5, 4, 5, 5, 5 },
            { -1,-1,-1, 4,-1,-1,-1 },
            { -1,-1,-1, 4,-1,-1,-1 },
            { -1,-1,-1, 4,-1,-1,-1 },
            { -1,-1,-1, 4,-1,-1,-1 },
            { -1,-1,-1, 2,-1,-1,-1 },
    }), 3, 0);
    */
    
}
