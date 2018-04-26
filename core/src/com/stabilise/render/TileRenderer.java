package com.stabilise.render;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.Slice.SLICE_SIZE_MINUS_ONE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.HostWorld;
import com.stabilise.world.World;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.world.Slice;
import com.stabilise.world.tile.Tile;

/**
 * The TileRenderer class manages the rendering of the tiles that constitute a
 * world.
 */
public class TileRenderer implements Renderer {
    
    private static final Color transparentRed = new Color(0xFF000040);
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
    
    // minCorner and maxCorner provide screen border cutoffs so that
    // renderSlice() can save some work by only rendering tiles on the
    // screen.
    private final Position minCorner = Position.create();
    private final Position maxCorner = Position.create();
    private final Position camPosOtherDim = Position.create();
    
    
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
        
        tiles = new TextureRegion[32];
        Tile.TILES.forEachEntry(t -> {
            if(t._2 != 0) // skip air
                tiles[t._2] = wr.skin.getRegion("tile/" + t._1.split(":")[1]);
        });
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
        minCorner.set(camPos, -wr.tilesHorizontal, -wr.tilesVertical).align().clampToTile();
        maxCorner.set(camPos, wr.tilesHorizontal, wr.tilesVertical + 1).align().clampToTile();
        
        for(int x = minCorner.getSliceX(); x <= maxCorner.getSliceX(); x++) {
            for(int y = minCorner.getSliceY(); y <= maxCorner.getSliceY(); y++) {
                renderSlice(world.getSliceAt(x, y), wr.camObj.pos, (dx,dy) -> true);
            }
        }
        //worldRenderer.batch.enableBlending();
    }
    
    /**
     * Renders a slice.
     */
    private void renderSlice(Slice slice, Position camPos, TilePredicate pred) {
        if(slice.isDummy())
            return;
        
        int x = slice.x;
        int y = slice.y; 
        
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
                if(id != 0 && pred.test(cx, cy)) { // i.e. not air
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
                if(id != 0 && pred.test(cx, cy)) { // i.e. not air
                    wr.batch.setColor(lightLevels[slice.getLightAt(tx, ty)]);
                    wr.batch.draw(tiles[id], cx, cy, 1f, 1f);
                }
                cx += 1f;
            }
            cy += 1f;
        }
        
        slicesRendered++;
    }
    
    public void renderSliceBorders(ShapeRenderer shapes) {
        Position camPos = wr.camObj.pos;
        
        int minX = camPos.getSliceX() - wr.slicesHorizontal;
        int maxX = camPos.getSliceX() + wr.slicesHorizontal + 1;
        int minY = camPos.getSliceY() - wr.slicesVertical;
        int maxY = camPos.getSliceY() + wr.slicesVertical + 1;
        
        // Draw horizontal lines
        for(int y = minY; y <= maxY; y++) {
            shapes.setColor(Maths.remainder2(y, REGION_SIZE) == 0 ? Color.RED : Color.YELLOW);
            shapes.line(camPos.diffX(minX, 0f), camPos.diffY(y, 0f), camPos.diffX(maxX, 0f), camPos.diffY(y, 0f));
        }
        
        // Draw vertical lines
        for(int x = minX; x <= maxX; x++) {
            shapes.setColor(Maths.remainder2(x, REGION_SIZE) == 0 ? Color.RED : Color.YELLOW);
            shapes.line(camPos.diffX(x, 0f), camPos.diffY(minY, 0f), camPos.diffX(x, 0f), camPos.diffY(maxY, 0f));
        }
    }
    
    public void renderRegionTint(ShapeRenderer shapes) {
        if(!world.isHost())
            return;
        HostWorld w = world.asHost();
        
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
    
    public void renderPortalView(Entity pe) {
        CPortal pc = (CPortal) pe.core;
        if(!pc.isOpen())
            return;
        
        // If true, other dimension will display to the right of portal;
        // if false, other dimension will display to the left of portal
        boolean drawToRight = !pe.facingRight;
        
        Position camPos = wr.camObj.pos;
        
        // Need tileDiffX separately or else some unlucky very precise addition
        // will end up rendering tiles in front of the portal, and flickering
        // sometimes.
        float tileDiffX = camPos.diffX(pe.pos);
        float diffX = tileDiffX + (drawToRight ? pe.aabb.minX() : pe.aabb.maxX());
        float diffY = camPos.diffY(pe.pos);
        
        if((drawToRight && diffX <= 0) || (!drawToRight && diffX >= 0))
            return;
        
        float minGradDy, maxGradDy;
        
        if(drawToRight) {
        	minGradDy = diffY + pe.aabb.minY();
            maxGradDy = diffY + pe.aabb.maxY();
        } else {
        	minGradDy = diffY + pe.aabb.maxY();
            maxGradDy = diffY + pe.aabb.minY();
        }
        
        // The camera's position if it were in the other dimension.
        camPosOtherDim.setSum(camPos, pc.offset).align();
        
        minCorner.set(camPosOtherDim, drawToRight ? tileDiffX : -wr.tilesHorizontal, -wr.tilesVertical)
        		.clampToTile().align();
        maxCorner.set(camPosOtherDim, drawToRight ? wr.tilesHorizontal : tileDiffX, wr.tilesVertical + 1)
        		.clampToTile().align();
        
        World w = pc.pairedWorld(world);
        
        for(int x = minCorner.getSliceX(); x <= maxCorner.getSliceX(); x++) {
            for(int y = minCorner.getSliceY(); y <= maxCorner.getSliceY(); y++) {
                renderSlice(w.getSliceAt(x, y), camPosOtherDim, (dx,dy) -> {
                    dy += 0.5f; // centre on the tile
                    dx += 0.5f; // centre on the tile
                    // We want
                    // dy/dx > minGradDy/camDiffX, and
                    // dy/dx < maxGradDy/camDiffX.
                    // Rearranging to avoid division by zero, we get...
                    return dy*diffX > dx*minGradDy && dy*diffX < dx*maxGradDy;
                });
            }
        }
    }
    
    @FunctionalInterface
    private static interface TilePredicate {
        /**
         * Returns true if the tile at the given coordinates relative to the
         * camera should be rendered.
         * 
         * @param x The x-coordinate of the tile, relative to the camera
         * @param y The y-coordinate of the tile, relative to the camera
         */
        boolean test(float x, float y);
    }
    
}