package com.stabilise.entity;

import com.stabilise.util.io.data.DataCompound;


/**
 * TODO
 */
public class PositionFixed extends Position {
    
    /** Local coordinates, in tile-lengths, relative to the slice specified by
     * {@link #sx} and {@link #sy}. */
    public int lx, ly;
    
    
    PositionFixed() {
        // fields are initialised to default values of all zeros, ty java
    }
    
    @Override
    public float lx() {
        return lx;
    }
    
    @Override
    public float ly() {
        return ly;
    }
    
    @Override
    public int ltx() {
        return lx;
    }
    
    @Override
    public int lty() {
        return ly;
    }
    
    @Override
    public void setLx(float lx) {
        this.lx = Position.tileCoordFreeToTileCoordFixed2(lx);
    }
    
    @Override
    public void setLy(float ly) {
        this.ly = Position.tileCoordFreeToTileCoordFixed2(ly);
    }
    
    @Override
    public void setLx(int lx) {
        this.lx = lx;
    }
    
    @Override
    public void setLy(int ly) {
        this.ly = ly;
    }
    
    @Override
    public PositionFixed set(int sliceX, int sliceY, float localX, float localY) {
        this.sx = sliceX;
        this.sy = sliceY;
        this.lx = Position.tileCoordFreeToTileCoordFixed2(localX);
        this.ly = Position.tileCoordFreeToTileCoordFixed2(localY);
        return this;
    }
    
    @Override
    public PositionFixed set(int sliceX, int sliceY, int localX, int localY) {
        this.sx = sliceX;
        this.sy = sliceY;
        this.lx = localX;
        this.ly = localY;
        return this;
    }
    
    @Override
    public PositionFixed set(double x, double y) {
        this.sx = sliceCoordFromTileCoord(x);
        this.sy = sliceCoordFromTileCoord(y);
        this.lx = Position.tileCoordFreeToTileCoordFixed(
                Position.tileCoordRelativeToSliceFromTileCoordFree(x));
        this.ly = Position.tileCoordFreeToTileCoordFixed(
                Position.tileCoordRelativeToSliceFromTileCoordFree(y));
        return this;
    }
    
    @Override
    public PositionFixed set(Position p) {
        sx = p.sx;
        sy = p.sy;
        lx = p.ltx();
        ly = p.lty();
        return this;
    }
    
    @Override
    public PositionFixed set(Position p, float dx, float dy) {
        sx = p.sx;
        sy = p.sy;
        lx = Position.tileCoordFreeToTileCoordFixed2(p.lx() + dx);
        ly = Position.tileCoordFreeToTileCoordFixed2(p.ly() + dy);
        return this;
    }
    
    @Override
    public PositionFixed setX(Position p, float dx) {
        sx = p.sx;
        lx = Position.tileCoordFreeToTileCoordFixed2(p.lx() + dx);
        return this;
    }
    
    @Override
    public PositionFixed setY(Position p, float dy) {
        sy = p.sy;
        ly = Position.tileCoordFreeToTileCoordFixed2(p.ly() + dy);
        return this;
    }
    
    @Override
    public PositionFixed setSum(Position p1, Position p2) {
        sx = p1.sx + p2.sx;
        sy = p1.sy + p2.sy;
        // Should really use lx() instead of ltx() and *then* round, but for
        // almost every use case this shouldn't matter
        lx = p1.ltx() + p2.ltx();
        ly = p1.lty() + p2.lty();
        return this;
    }
    
    @Override
    public PositionFixed setDiff(Position p1, Position p2) {
        sx = p1.sx - p2.sx;
        sy = p1.sy - p2.sy;
        // Should really use lx() instead of ltx() and *then* round, but for
        // almost every use case this shouldn't matter
        lx = p1.ltx() - p2.ltx();
        ly = p1.lty() - p2.lty();
        return this;
    }
    
    @Override
    public PositionFixed addX(float dx) {
        lx += dx;
        return this;
    }
    
    @Override
    public PositionFixed addY(float dy) {
        ly += dy;
        return this;
    }
    
    @Override
    public PositionFixed addX(int dx) {
        lx += dx;
        return this;
    }
    
    @Override
    public PositionFixed addY(int dy) {
        ly += dy;
        return this;
    }
    
    @Override
    public PositionFixed reflect() {
        sx = -sx;
        sy = -sy;
        lx = -lx;
        ly = -ly;
        return this;
    }
    
    @Override
    public PositionFixed align() {
        alignX();
        alignY();
        return this;
    }
    
    @Override
    public PositionFixed alignX() {
        sx += Position.sliceCoordFromTileCoord(lx);
        lx = Position.tileCoordRelativeToSliceFromTileCoord(lx);
        return this;
    }
    
    @Override
    public PositionFixed alignY() {
        sy += Position.sliceCoordFromTileCoord(ly);
        ly = Position.tileCoordRelativeToSliceFromTileCoord(ly);
        return this;
    }
    
    @Override
    public PositionFixed globalify() {
        lx += tileCoordFromSliceCoord(sx);
        ly += tileCoordFromSliceCoord(sy);
        sx = 0;
        sy = 0;
        return this;
    }
    
    @Override
    public PositionFixed clampToTile() {
        return this; // Nothing to do here
    }
    
    /**
     * Returns this PositionFixed object.
     */
    @Override
    public PositionFixed fixed() {
        return this;
    }
    
    @Override
    public PositionFixed clone() {
        return new PositionFixed().set(this);
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        super.importFromCompound(c);
        lx = c.getI32("lx");
        ly = c.getI32("ly");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        super.exportToCompound(c);
        c.put("lx", lx);
        c.put("ly", ly);
    }
    
    @Override
    public int hashCode() {
        // a bit crap of a hashcode but meh
        return sx ^ sy ^ lx ^ ly;
    }
    
    @Override
    public boolean equalsPos(Position p) {
        return sx == p.sx && sy == p.sy && lx == p.ltx() && ly == p.lty();
    }
    
    @Override
    public String toString() {
        return "Pos[(" + sx + "," + sy + "); (" + lx + "," + ly + ")]";
    }
        
}
