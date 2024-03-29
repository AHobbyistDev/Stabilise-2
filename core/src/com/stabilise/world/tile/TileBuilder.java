package com.stabilise.world.tile;

import java.util.function.Function;
import java.util.Objects;

/**
 * This class provides an easy-to-use builder with which to construct tiles for
 * registration.
 */
class TileBuilder {
    
    /** Hardness templates. */
    private static final float
            H_DIRT = 1f,
            H_WOOD = 3f,
            H_STONE = 10f,
            H_INVUL = Float.MAX_VALUE;
    /** Default friction. */
    private static final float
            F_DEF = 0.07f,
            F_ICE = 0.008f,
            F_AIR = 0.001f;
    
    
    /**
     * Templates for various tile types.
     */
    enum Template {
        
        AIR(),
        DIRT(H_DIRT, F_DEF),
        GRASS(H_DIRT, F_DEF, TileGrass::new),
        STONE(H_STONE, F_DEF),
        WOOD(H_WOOD, F_DEF),
        INVUL(H_INVUL, F_DEF),
        ICE(H_DIRT, F_ICE),
        WATER(0.12f),
        LAVA(0.12f),
        CHEST(H_WOOD, F_DEF, TileChest::new),
        SPWNR(H_STONE, F_DEF, TileMobSpawner::new),
        ORE(H_STONE, F_DEF, TileOre::new),
        SPREAD(H_STONE, F_DEF, TileVoidSpread::new);
        
        private final Function<TileBuilder, Tile> constructor;
        
        private final boolean solid;
        private final float hardness;
        private final float friction;
        private final float viscosity;
        private final byte light;
        private final byte falloff;
        
        
        /** Air tile */
        Template() {
            constructor = TileAir::new;
            solid = false;
            hardness = 0f;
            friction = F_AIR;
            viscosity = 0f;
            light = 2;
            falloff = 1;
        }
        
        /** Solid tile */
        Template(float hardness, float friction) {
            this(hardness, friction, Tile::new);
        }
        
        /** Solid tile */
        Template(float hardness, float friction, Function<TileBuilder, Tile> b) {
            this.constructor = b;
            solid = true;
            this.hardness = hardness;
            this.friction = friction;
            viscosity = 0f;
            light = 5;
            falloff = 2;
        }
        
        /** Fluid tile */
        Template(float viscosity) {
            constructor = TileFluid::new;
            solid = false;
            hardness = friction = 0f;
            this.viscosity = viscosity;
            light = 2;
            falloff = 2;
        }
        
    }
    
    private boolean registered = true;
    
    private Template template = null;
    private Function<TileBuilder, Tile> constructor;
    int id;
    String name;
    boolean solid;
    float hardness;
    float friction;
    byte light;
    byte falloff;
    
    // liquids only
    float viscosity;
    
    
    
    /** package-private constructor */
    TileBuilder() {
        
    }
    
    /**
     * Commences tile building for a tile using the specified template, ID and
     * name. This also registers the tile previously built, if it was not
     * already.
     * 
     * @throws NullPointerException if either {@code t} or {@code name} are
     * {@code null}.
     * @throws IllegalArgumentException if {@code id < 0}.
     */
    TileBuilder begin(Template t, int id, String name) {
        if(id < 0)
            throw new IllegalArgumentException("id < 0");
        
        end();
        
        this.template = Objects.requireNonNull(t);
        this.id = id;
        this.name = Objects.requireNonNull(name);
        
        constructor = template.constructor;
        solid = t.solid;
        hardness = t.hardness;
        friction = t.friction;
        viscosity = t.viscosity;
        light = t.light;
        falloff = t.falloff;
        
        registered = false;
        
        return this;
    }
    
    /**
     * Flushes the most recent registration.
     */
    public void end() {
        if(!registered && this.template != null)
            register();
    }
    
    public TileBuilder constructor(Function<TileBuilder, Tile> constructor) {
        this.constructor = Objects.requireNonNull(constructor);
        return this;
    }
    
    public TileBuilder solid(boolean solid) {
        this.solid = solid;
        return this;
    }
    
    public TileBuilder hardness(float hardness) {
        this.hardness = hardness;
        return this;
    }
    
    public TileBuilder viscosity(float viscosity) {
        this.viscosity = viscosity;
        return this;
    }
    
    /** Casted to byte */
    public TileBuilder light(int light) {
        this.light = (byte) light;
        return this;
    }
    
    public TileBuilder falloff(byte falloff) {
        this.falloff = falloff;
        return this;
    }
    
    /**
     * Registers a tile via {@link Tile#registerTile(Tile)}.
     * 
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if the tile or its name is {@code null}.
     * @throws IllegalStateException if no more tiles may be registered, or
     * this builder wasn't reset since the last registration.
     */
    public void register() {
        register(constructor);
    }
    
    /**
     * Registers a tile, constructing it as per the specified function.
     * 
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if {@code f} is {@code null}, or it
     * produces a {@code null} tile, or the tile's name is null.
     * @throws IllegalStateException if no more tiles may be registered, or
     * this builder wasn't reset since the last registration.
     */
    public void register(Function<TileBuilder, Tile> f) {
        if(registered)
            throw new IllegalStateException("Already registered");
        Tile.registerTile(f.apply(this));
        registered = true;
    }
    
}
