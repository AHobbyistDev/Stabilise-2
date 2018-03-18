package com.stabilise.world.tile;

import com.stabilise.entity.Position;
import com.stabilise.world.World;


/**
 * A class of tiles which spreads to other tiles
 */
public class TileVoidSpread extends Tile {
    
    TileVoidSpread(TileBuilder b) {
        super(b);
    }
    
    @Override
    public void update(World w, Position pos) {
        Position tmp = Position.create();
        int n = 2 + w.rnd().nextInt(2);
        for(int i = 0; i < n; i++)
            spread(w, pos, tmp);
    }
    
    private void spread(World w, Position pos, Position tmp) {
        int dx = w.rnd().nextInt(5) - 2; // -2 to 2
        int dy = w.rnd().nextInt(5) - 2; // -2 to 2
        tmp.set(pos, dx, dy).realign();
        Tile t = w.getTileAt(tmp);
        if(t == Tiles.dirt || t == Tiles.grass)
            w.setTileAt(tmp, Tiles.voidDirt);
        else if(t == Tiles.stone || t == Tiles.stoneBrick)
            w.setTileAt(tmp, Tiles.voidRock);
        else if(t instanceof TileOre)
            w.setTileAt(tmp, Tiles.voidRockDense);
    }
    
}
