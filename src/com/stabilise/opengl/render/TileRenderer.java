package com.stabilise.opengl.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.stabilise.core.Resources;
import com.stabilise.opengl.TextureSheet;
import com.stabilise.world.World;
import com.stabilise.world.Slice;

/**
 * The TileRenderer class manages the rendering of the tiles that constitute a
 * world.
 */
public class TileRenderer implements Renderer {
    
    //--------------------==========--------------------
    //------------=====Member Variables=====------------
    //--------------------==========--------------------
    
    /** A reference to the world renderer. */
    public final WorldRenderer worldRenderer;
    /** A reference to the world. */
    public final World world;
    
    public TextureSheet tiles;
    
    /** Number of slices rendered on each render step. */
    int slicesRendered = 0;
    
    private float[] lightLevels;
    
    
    /**
     * Creates a new TileRenderer.
     * 
     * @param worldRenderer The world renderer.
     */
    public TileRenderer(WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;
        world = worldRenderer.world;
        
        loadResources();
    }
    
    @Override
    public void loadResources() {
        tiles = TextureSheet.sequentiallyOptimised(Resources.textureMipmaps("sheets/tiles"), 8, 8);
        tiles.texture.setFilter(TextureFilter.MipMapNearestLinear, TextureFilter.Nearest);
        tiles.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        
        lightLevels = new float[16];
        for(int i = 0; i < 16; i++) {
            lightLevels[i] = new Color(i*16/255f, i*16/255f, i*16/255f, 1f).toFloatBits();
        }
        
        //System.out.println(tiles);
    }
    
    @Override
    public void unloadResources() {
        tiles.dispose();
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
        for(int c = worldRenderer.playerCamera.sliceX - worldRenderer.slicesHorizontal;
                c <= worldRenderer.playerCamera.sliceX + worldRenderer.slicesHorizontal;
                c++)
            for(int r = worldRenderer.playerCamera.sliceY - worldRenderer.slicesVertical;
                    r <= worldRenderer.playerCamera.sliceY + worldRenderer.slicesVertical;
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
        
        final int tileXInit = x * Slice.SLICE_SIZE;
        int tileX;
        int tileY = y * Slice.SLICE_SIZE;
        
        for(int r = 0; r < Slice.SLICE_SIZE; r++) {
            tileX = tileXInit;
            for(int c = 0; c < Slice.SLICE_SIZE; c++) {
                // Offset of +8 due to tile breaking animations; offset of -1
                // because air has no texture: sums to +7
                int id = slice.getTileIDAt(c, r) + 7;
                
                if(id != 7) { // i.e. not air
                    worldRenderer.batch.setColor(lightLevels[slice.getLightAt(c, r)]);
                    worldRenderer.batch.draw(tiles.getRegion(id), tileX, tileY, 1f, 1f);
                }
                
                tileX++;
            }
            
            tileY++;
        }
    }
    
}