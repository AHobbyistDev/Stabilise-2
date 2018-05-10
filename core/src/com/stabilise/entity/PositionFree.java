package com.stabilise.entity;

import com.stabilise.util.io.data.DataCompound;

/**
 * TODO
 */
public class PositionFree extends Position {
    
    /** Local coordinates, in tile-lengths, relative to the slice specified by
     * {@link #sx} and {@link #sy}. */
    public float lx, ly;
    
    
    
    PositionFree() {
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
        return Position.tileCoordFreeToTileCoordFixed2(lx);
    }
    
    @Override
    public int lty() {
        return Position.tileCoordFreeToTileCoordFixed2(ly);
    }
    
    @Override
    public void setLx(float lx) {
        this.lx = lx;
    }
    
    @Override
    public void setLy(float ly) {
        this.ly = ly;
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
    public PositionFree set(int sliceX, int sliceY, float localX, float localY) {
        this.sx = sliceX;
        this.sy = sliceY;
        this.lx = localX;
        this.ly = localY;
        return this;
    }
    
    @Override
    public PositionFree set(int sliceX, int sliceY, int localX, int localY) {
        this.sx = sliceX;
        this.sy = sliceY;
        this.lx = localX;
        this.ly = localY;
        return this;
    }
    
    @Override
    public PositionFree set(double x, double y) {
        this.sx = sliceCoordFromTileCoord(x);
        this.sy = sliceCoordFromTileCoord(y);
        this.lx = (float)tileCoordRelativeToSliceFromTileCoordFree(x);
        this.ly = (float)tileCoordRelativeToSliceFromTileCoordFree(y);
        return this;
    }
    
    @Override
    public PositionFree set(Position p) {
        sx = p.sx;
        sy = p.sy;
        lx = p.lx();
        ly = p.ly();
        return this;
    }
    
    @Override
    public PositionFree set(Position p, float dx, float dy) {
        sx = p.sx;
        sy = p.sy;
        lx = p.lx() + dx;
        ly = p.ly() + dy;
        return this;
    }
    
    @Override
    public PositionFree setX(Position p, float dx) {
        sx = p.sx;
        lx = p.lx();
        return this;
    }
    
    @Override
    public PositionFree setY(Position p, float dy) {
        sy = p.sy;
        ly = p.ly();
        return this;
    }
    
    @Override
    public PositionFree setSum(Position p1, Position p2) {
        sx = p1.sx + p2.sx;
        sy = p1.sy + p2.sy;
        lx = p1.lx() + p2.lx();
        ly = p1.ly() + p2.ly();
        return this;
    }
    
    @Override
    public PositionFree setDiff(Position p1, Position p2) {
        sx = p1.sx - p2.sx;
        sy = p1.sy - p2.sy;
        lx = p1.lx() - p2.lx();
        ly = p1.ly() - p2.ly();
        return this;
    }
    
    @Override
    public PositionFree addX(float dx) {
        lx += dx;
        return this;
    }
    
    @Override
    public PositionFree addY(float dy) {
        ly += dy;
        return this;
    }
    
    @Override
    public PositionFree addX(int dx) {
        lx += dx;
        return this;
    }
    
    @Override
    public PositionFree addY(int dy) {
        ly += dy;
        return this;
    }
    
    @Override
    public PositionFree reflect() {
        sx = -sx;
        sy = -sy;
        lx = -lx;
        ly = -ly;
        return this;
    }
    
    @Override
    public PositionFree alignX() {
        sx += sliceCoordFromTileCoord2(lx);
        lx = tileCoordRelativeToSliceFromTileCoordFree2(lx);
        return this;
    }
    
    @Override
    public PositionFree alignY() {
        sy += sliceCoordFromTileCoord2(ly);
        ly = tileCoordRelativeToSliceFromTileCoordFree2(ly);
        return this;
    }
    
    @Override
    public PositionFree globalify() {
        lx += tileCoordFromSliceCoord(sx);
        ly += tileCoordFromSliceCoord(sy);
        sx = 0;
        sy = 0;
        return this;
    }
    
    @Override
    public PositionFree clampToTile() {
        lx = tileCoordFreeToTileCoordFixed2(lx);
        ly = tileCoordFreeToTileCoordFixed2(ly);
        return this;
    }
    
    /**
     * Returns this PositionFree object.
     */
    @Override
    public PositionFree free() {
        return this;
    }
    
    @Override
    public PositionFree clone() {
        return new PositionFree().set(this);
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        super.importFromCompound(c); // does sx, sy
        lx = c.getF32("lx");
        ly = c.getF32("ly");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        super.exportToCompound(c); // does sx, sy
        c.put("lx", lx);
        c.put("ly", ly);
    }
    
    @Override
    public int hashCode() {
        // a bit crap of a hashcode but meh
        return sx ^ sy ^ Float.hashCode(lx) ^ Float.hashCode(ly);
    }
    
    @Override
    public boolean equalsPos(Position p) {
        return sx == p.sx && sy == p.sy && lx == p.lx() && ly == p.ly();
    }
    
    @Override
    public String toString() {
        return "Pos[(" + sx + "," + sy + "); (" + lx + "," + ly + ")]";
    }
    
}
