package com.stabilise.opengl.render;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.Slice.SLICE_SIZE;
import static com.stabilise.world.Slice.SLICE_SIZE_MINUS_ONE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;
import com.stabilise.entity.Position;
import com.stabilise.world.Slice;
import com.stabilise.world.tile.Tile;

/**
 * The TileRenderer class manages the rendering of the tiles that constitute a
 * world.
 */
public class TileRenderer implements Renderer {
    
    private static final Color transparentRed = new Color(0xFF000040);
    //private static final Color transparentOrange= new Color(0xFF800040);
    private static final Color transparentYellow = new Color(0xFFFF0040);
    private static final Color transparentGreen = new Color(0x00FF0040);
    private static final Color transparentBlue = new Color(0x00FFFF40);
    
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
    
    
    private Position minCorner = Position.create();
    private Position maxCorner = Position.create();
    
    
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
        // nothing to unload as everything is managed by WorldRenderer
    }
    
    @Override
    public void resize(int width, int height) {
        // nothing
    }
    
    @Override
    public void update() {
        // nothing
    }

    @Override
    public void render() {
        //worldRenderer.batch.disableBlending();
        slicesRendered = 0;
        Position camPos = wr.camObj.pos;
        minCorner.set(camPos, -wr.tilesHorizontal, -wr.tilesVertical).realign().clampToTile();
        maxCorner.set(camPos, wr.tilesHorizontal, wr.tilesVertical + 1).realign().clampToTile();
        int camSliceX = camPos.getSliceX();
        int camSliceY = camPos.getSliceY();
        for(int c = camSliceX - wr.slicesHorizontal;
                c <= camSliceX + wr.slicesHorizontal;
                c++) {
            for(int r = camSliceY - wr.slicesVertical;
                    r <= camSliceY + wr.slicesVertical;
                    r++) {
                renderSlice(c, r);
                slicesRendered++;
            }
        }
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
        
        if(slice.isDummy())
            return;
        
        Position camPos = wr.camObj.pos;
        
        // Relative to the camera, where the origin of this slice is
        float sliceOriginX = camPos.diffX(x, 0f);
        float sliceOriginY = camPos.diffY(y, 0f);
        
        // Casting the corners' localX/Y to int is fine since we already
        // clamped minCorner and maxCorner in render().
        int xMin = x == minCorner.getSliceX() ? (int)minCorner.getLocalX() : 0;
        int yMin = y == minCorner.getSliceY() ? (int)minCorner.getLocalY() : 0;
        int xMax = x == maxCorner.getSliceX() ? (int)maxCorner.getLocalX() : SLICE_SIZE_MINUS_ONE;
        int yMax = y == maxCorner.getSliceY() ? (int)maxCorner.getLocalY() : SLICE_SIZE_MINUS_ONE;
        
        // Camera x/y at which to place the tile.
        float cx, cy;
        
        // Draw background tiles
        cy = sliceOriginY + yMin;
        for(int ty = yMin; ty <= yMax; ty++) {
            cx = sliceOriginX + xMin;
            for(int tx = xMin; tx <= xMax; tx++) {
                int id = slice.getWallIDAt(tx, ty);
                if(id != 0) { // i.e. not air
                    // Temporary wall lighting; 2 + light/2
                    wr.batch.setColor(lightLevels[1 + slice.getLightAt(tx, ty)/4]);
                    wr.batch.draw(tiles[id], cx, cy, 1f, 1f);
                }
                cx += 1f;
            }
            cy += 1f;
        }
        
        // Draw tiles
        cy = sliceOriginY + yMin;
        for(int ty = yMin; ty <= yMax; ty++) {
            cx = sliceOriginX + xMin;
            for(int tx = xMin; tx <= xMax; tx++) {
                int id = slice.getTileIDAt(tx, ty);
                if(id != 0) { // i.e. not air
                    wr.batch.setColor(lightLevels[slice.getLightAt(tx, ty)]);
                    wr.batch.draw(tiles[id], cx, cy, 1f, 1f);
                }
                cx += 1f;
            }
            cy += 1f;
        }
        
        //if(x == 0 && y == -9)
        //    System.out.println("ayy lmao");
    }
    
    public void renderSliceBorders(ShapeRenderer shapes) {
        // Yellow slice borders
        shapes.setColor(Color.YELLOW);
        
        int camSliceX = wr.camObj.pos.getSliceX();
        int camSliceY = wr.camObj.pos.getSliceY();
        Position camPos = wr.camObj.pos;
        for(int x = camSliceX - wr.slicesHorizontal;
                x <= camSliceX + wr.slicesHorizontal;
                x++) {
            for(int y = camSliceY - wr.slicesVertical;
                    y <= camSliceY + wr.slicesVertical;
                    y++) {
                shapes.rect(camPos.diffX(x, 0f), camPos.diffY(y, 0f), SLICE_SIZE, SLICE_SIZE);
            }
        }
        
        // Red region borders
        shapes.setColor(Color.RED);
        
        int camRegionX = wr.camObj.pos.getRegionX();
        int camRegionY = wr.camObj.pos.getRegionY();
        int regionsHorizontal = wr.slicesHorizontal / REGION_SIZE;
        int regionsVertical = wr.slicesVertical / REGION_SIZE;
        for(int x = camRegionX - regionsHorizontal;
                x <= camRegionX + regionsHorizontal;
                x++) {
            for(int y = camRegionY - regionsVertical;
                    y <= camRegionY + regionsVertical;
                    y++) {
                shapes.rect(camPos.diffX(x*REGION_SIZE, 0f), camPos.diffY(y*REGION_SIZE, 0f),
                        REGION_SIZE_IN_TILES, REGION_SIZE_IN_TILES);
            }
        }
    }
    
    public void renderRegionTint(ShapeRenderer shapes) {
        if(!(world instanceof HostWorld))
            return;
        HostWorld w = (HostWorld)world;
        
        Position camPos = wr.camObj.pos;
        
        w.regions.forEach(r -> {
            if(r.state.isActive())
                shapes.setColor(transparentBlue);
            else if(r.state.isAnchored()) {
                shapes.setColor(transparentGreen);
            } else if(r.state.hasAnchoredNeighbours())
                shapes.setColor(transparentYellow);
            else
                shapes.setColor(transparentRed);
            shapes.rect(camPos.diffX(r.offsetX, 0f), camPos.diffY(r.offsetY, 0f),
                    REGION_SIZE_IN_TILES, REGION_SIZE_IN_TILES);
        });
    }
    
}