package com.stabilise.opengl.render;

import static com.stabilise.world.Slice.SLICE_SIZE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.stabilise.world.World;
import com.stabilise.world.Slice;
import com.stabilise.world.tile.Tile;

/**
 * The TileRenderer class manages the rendering of the tiles that constitute a
 * world.
 */
public class TileRenderer implements Renderer {
    
    //--------------------==========--------------------
    //------------=====Member Variables=====------------
    //--------------------==========--------------------
    
    /** A reference to the world renderer. */
    public final WorldRenderer wr;
    /** A reference to the world. */
    public final World world;
    
    /** Number of slices rendered on each render step. */
    int slicesRendered = 0;
    
    TextureRegion[] tiles;
    private float[] lightLevels;
    
    
    /**
     * Creates a new TileRenderer.
     * 
     * @param worldRenderer The world renderer.
     */
    public TileRenderer(WorldRenderer worldRenderer) {
        this.wr = worldRenderer;
        world = worldRenderer.world;
    }
    
    @Override
    public void loadResources() {
        lightLevels = new float[16];
        for(int i = 0; i < 16; i++) {
            lightLevels[i] = new Color(i*16/255f, i*16/255f, i*16/255f, 1f).toFloatBits();
        }
        
        tiles = new TextureRegion[32]; // TODO: temp length
        Tile.TILES.forEachEntry(t -> {
            if(t._2 != 0) // skip air
                tiles[t._2] = wr.skin.getRegion("tile/" + t._1.split(":")[1]);
        });
        
        //System.out.println(tiles);
    }
    
    @Override
    public void unloadResources() {
        
    }
    
    @Override
    public void resize(int width, int height) {
        // le nothing
    }
    
    @Override
    public void update() {
        // le nothing
    }

    @Override
    public void render() {
        //worldRenderer.batch.disableBlending();
        slicesRendered = 0;
        for(int c = wr.playerCamera.sliceX - wr.slicesHorizontal;
                c <= wr.playerCamera.sliceX + wr.slicesHorizontal;
                c++)
            for(int r = wr.playerCamera.sliceY - wr.slicesVertical;
                    r <= wr.playerCamera.sliceY + wr.slicesVertical;
                    r++)
                renderSlice(c, r);
        //worldRenderer.batch.enableBlending();
        //System.out.println(slicesRendered + " slices rendered");
    }
    
    /**
     * Renders a slice.
     * 
     * @param x The x-coordinate of the slice, in slice-lengths.
     * @param y The y-coordinate of the slice, in slice-lengths.
     */
    private void renderSlice(int x, int y) {
        Slice slice = world.getSliceAt(x, y);
        
        if(slice == null)
            return;
        
        slicesRendered++;
        
        int xMin = Math.max(x    *SLICE_SIZE, camX() - wr.tilesHorizontal);
        int xMax = Math.min((x+1)*SLICE_SIZE, camX() + wr.tilesHorizontal + 1);
        int yMin = Math.max(y    *SLICE_SIZE, camY() - wr.tilesVertical);
        int yMax = Math.min((y+1)*SLICE_SIZE, camY() + wr.tilesVertical + 1);
        
        // These two are to adjust the 0 index if we don't render all of the slice
        int rMin = Math.max(0, yMin - y*SLICE_SIZE);
        int cMin = Math.max(0, xMin - x*SLICE_SIZE);
        
        wr.batch.setColor(lightLevels[2]);
        
        for(int r = rMin, ty = yMin; r < SLICE_SIZE && ty < yMax; r++, ty++) {
            for(int c = cMin, tx = xMin; c < SLICE_SIZE && tx < xMax; c++, tx++) {
                int id = slice.getWallIDAt(c, r);
                if(id != 0) { // i.e. not air
                    wr.batch.draw(tiles[id], tx, ty, 1f, 1f);
                }
            }
        }
        
        for(int r = rMin, ty = yMin; r < SLICE_SIZE && ty < yMax; r++, ty++) {
            for(int c = cMin, tx = xMin; c < SLICE_SIZE && tx < xMax; c++, tx++) {
                int id = slice.getTileIDAt(c, r);
                if(id != 0) { // i.e. not air
                    wr.batch.setColor(lightLevels[slice.getLightAt(c, r)]);
                    wr.batch.draw(tiles[id], tx, ty, 1f, 1f);
                }
            }
        }
    }
    
    private int camX() {
        return wr.playerCamera.getTileX();
    }
    
    private int camY() {
        return wr.playerCamera.getTileY();
    }
    
}