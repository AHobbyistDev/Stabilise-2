package com.stabilise.render;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.Slice.SLICE_SIZE_MINUS_ONE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
    
    /** Number of slices rendered on each render step. */
    int slicesRendered = 0;
    
    TextureRegion[] tiles;
    private float[] lightLevels;
    
    // minCorner and maxCorner provide screen border cutoffs so that
    // renderSlice() can save some work by only rendering tiles on the
    // screen.
    private final Position minCorner = Position.createFixed();
    private final Position maxCorner = Position.createFixed();
    private final Position camPosOtherDim = Position.create();
    
    
    /**
     * Creates a new TileRenderer.
     * 
     * @param worldRenderer The world renderer.
     */
    public TileRenderer(WorldRenderer worldRenderer) {
        this.wr = worldRenderer;
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
        minCorner.set(camPos, -wr.tilesHorizontal, -wr.tilesVertical).align();
        maxCorner.set(camPos, wr.tilesHorizontal, wr.tilesVertical + 1).align();
        
        for(int x = minCorner.sx(); x <= maxCorner.sx(); x++) {
            for(int y = minCorner.sy(); y <= maxCorner.sy(); y++) {
                renderSlice(wr.world.getSliceAt(x, y), wr.camObj.pos, (dx,dy) -> true);
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
        int xMin = x == minCorner.sx() ? minCorner.ltx() : 0;
        int yMin = y == minCorner.sy() ? minCorner.lty() : 0;
        int xMax = x == maxCorner.sx() ? maxCorner.ltx() : SLICE_SIZE_MINUS_ONE;
        int yMax = y == maxCorner.sy() ? maxCorner.lty() : SLICE_SIZE_MINUS_ONE;
        
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
                    wr.batch.setPackedColor(lightLevels[1 + slice.getLightAt(tx, ty)/4]);
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
                    wr.batch.setPackedColor(lightLevels[slice.getLightAt(tx, ty)]);
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
        
        int minX = camPos.sx() - wr.slicesHorizontal;
        int maxX = camPos.sx() + wr.slicesHorizontal + 1;
        int minY = camPos.sy() - wr.slicesVertical;
        int maxY = camPos.sy() + wr.slicesVertical + 1;
        
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
        if(!wr.world.isHost())
            return;
        HostWorld w = wr.world.asHost();
        
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
    
    /**
     * @param pe portal entity
     */
    public void renderPortalView(Entity pe) {
        CPortal pc = (CPortal) pe.core;
        if(!pc.isOpen())
            return;
        
        Position camPos = wr.camObj.pos;
        
        float diffX = camPos.diffX(pe.pos);
        float diffY = camPos.diffY(pe.pos);
        
        float dot = pc.direction.dot(diffX, diffY);
        
        if(!pc.doubleSided && dot > 0)
            return;
        
        float sgn = dot > 0 ? -1 : 1;
        float xOff = sgn*pc.halfHeight*MathUtils.sin(pc.rotation);
        float yOff = sgn*pc.halfHeight*MathUtils.cos(pc.rotation);
        float minGradDx = diffX - xOff;
        float maxGradDx = diffX + xOff;
        float minGradDy = diffY + yOff;
        float maxGradDy = diffY - yOff;
        
        // The camera's position if it were in the other dimension.
        camPosOtherDim.setSum(camPos, pc.offset).align();
        
        minCorner.set(camPosOtherDim, -wr.tilesHorizontal, -wr.tilesVertical)
                .clampToTile().align();
        maxCorner.set(camPosOtherDim, wr.tilesHorizontal, wr.tilesVertical + 1)
                .clampToTile().align();
        
        World w = pc.pairedWorld(wr.world);
        
        for(int sx = minCorner.sx(); sx <= maxCorner.sx(); sx++) {
            for(int sy = minCorner.sy(); sy <= maxCorner.sy(); sy++) {
                renderSlice(w.getSliceAt(sx, sy), camPosOtherDim, (x,y) -> {
                    x += 0.5f; // centre on the tile
                    y += 0.5f; // centre on the tile
                    // We want
                    // y/x > minGradDy/minGradDx, and
                    // y/x < maxGradDy/maxGradDx.
                    // Rearranging to avoid division by zero, we get...
                    return y*minGradDx > x*minGradDy && y*maxGradDx < x*maxGradDy
                    // We also only want tiles behind the portal to be rendered.
                    // Multiplication by dot lets double-sided portals work
                           && dot*pc.direction.dot(x-diffX, y-diffY) > 0;
                });
            }
        }
    }
    
    @FunctionalInterface
    private interface TilePredicate {
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