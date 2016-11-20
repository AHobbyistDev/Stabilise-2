package com.stabilise.item.resource;

import com.stabilise.util.annotation.Incomplete;

// Honestly this class has no purpose at the moment and is just filled with
// random arbitrary values that I came up with 3 or so year ago.

/**
 * A resource represents a class of material.
 */
@Incomplete
public class Resource {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The number of different resources. */
    private static final int NUM_RESOURCES = 32;
    /** The array of resources. */
    private static final Resource[] RESOURCES = new Resource[NUM_RESOURCES];
    
    // Ignoring CAPS_CONVENTIONS
    //id, name, rarity, density, hardness, toughness, malleability, enchantability
    public static final Resource
            wood        = new Resource(0, "Wood",   1f, 1f, 0.2f, 0.2f, 0.0f, 0.8f),
            stone       = new Resource(1, "Stone",  1f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            coal        = new Resource(2, "Coal",   3f, 2f, 0.4f, 0.4f, 0.0f, 0.3f),
            iron        = new Resource(3, "Iron",   10f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            steel       = new Resource(4, "Steel",  20f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            copper      = new Resource(5, "Copper", 7f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            tin         = new Resource(6, "Tin",    7f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            bronze      = new Resource(7, "Bronze", 15f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            silver      = new Resource(8, "Silver", 25f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            gold        = new Resource(9, "Gold",   50f, 5f, 0.4f, 0.4f, 0.0f, 0.3f),
            aluminium   = new Resource(10, "Aluminium", 9f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            paper       = new Resource(20, "Paper", 1f, 0.1f, 0.4f, 0.4f, 0.0f, 0.3f),
            bone        = new Resource(21, "Bone",  4f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            leather     = new Resource(22, "Leather", 2f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            wool        = new Resource(23, "Wool",  1f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            cotton      = new Resource(24, "Cotton", 1f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            silk        = new Resource(25, "Silk",  2f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            cloth       = new Resource(26, "Cloth", 1f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            clay        = new Resource(27, "Clay",  4f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            ceramic     = new Resource(28, "Ceramic", 4f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            glass       = new Resource(29, "Glass", 5f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            obsidian    = new Resource(30, "Obsidian", 30f, 3f, 0.4f, 0.4f, 0.0f, 0.3f),
            tallow      = new Resource(31, "Tallow", 2f, 3f, 0.4f, 0.4f, 0.0f, 0.3f);
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The ID of the resource. */
    private final int id;
    /** The name of the resource. */
    private final String name;
    
    /** The rarity of the resource; price scales with this.
     * <p>Though a proper pricing metric has yet to be defined, for now let a
     * general guide be the following:<br>
     * 1 - Ubiquitous<br>
     * 10 - Common<br>
     * 100 - Uncommon<br>
     * 1000 - Rare<br>
     * 10000 - Very Rare<br>
     * 100000 - Near Unique<br>
     * 1000000 - Unique */
    private float rarity;
    /** Mass per unit volume. */
    private float density;
    /** Resistance against scratching, wear and tear and indentation. (Reduces
     * quality degradation from use.) */
    private float hardness;
    /** Resistance against shattering from impacts. */
    private float toughness;
    /** Ability to be reshaped without cracking. (Ease in crafting.) */
    private float malleability;
    /** The enchantability of the resource. */
    private float enchantability;
    // Strength (survive forces without bending, shattering or deforming)
    // Elasticity (ability to absorb force and return to its original state)
    // Plasticity (ability to change in shape permanently)
    // Ductility (ability to be deformed)
    // Tensile strength (ability to be stretched without breaking)
    
    
    /**
     * Registers a resource.
     * 
     * @param id The ID of the resource.
     * @param name The name of the resource.
     * 
     * @throws IllegalArgumentException Thrown if the given ID has already been
     * used.
     * @throws ArrayIndexOutOfBoundsException Thrown if the given ID is
     * negative or exceeds the allowable range of IDs.
     */
    public Resource(int id, String name) {
        if(RESOURCES[id] != null)
            throw new IllegalArgumentException("Error while attempting to register resource \"" + name + "\"; A resource of ID " + id + " (" + RESOURCES[id].name + ") already exists!");
    
        this.id = id;
        this.name = name;
        
        RESOURCES[id] = this;
    }
    
    /**
     * Registers a resource.
     * 
     * @param id The ID of the resource.
     * @param name The name of the resource.
     * @param rarity The resource's rarity value.
     * @param density The resource's density value.
     * @param hardness The resource's hardness value.
     * @param toughness The resource's toughness value.
     * @param malleability The resource's malleability value.
     * @param enchantability The resource's enchantability value.
     * 
     * @throws IllegalArgumentException Thrown if the given ID has already been
     * used.
     * @throws ArrayIndexOutOfBoundsException Thrown if the given ID is
     * negative or exceeds the allowable range of IDs.
     */
    public Resource(int id, String name, float rarity, float density, float hardness, float toughness, float malleability, float enchantability) {
        this(id, name);
        
        this.rarity = rarity;
        this.density = density;
        this.hardness = hardness;
        this.toughness = toughness;
        this.malleability = malleability;
        this.enchantability = enchantability;
    }
    
    /**
     * Gets the ID of the resource.
     * 
     * @return The resource's ID.
     */
    public int getID() {
        return id;
    }
    
    /**
     * Gets the name of the resource.
     * 
     * @return The resource's name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the rarity of the resource.
     * 
     * @return The rarity value.
     */
    public float getRarity() {
        return rarity;
    }
    
    /**
     * Gets the density of the resource.
     * 
     * @return The density value.
     */
    public float getDensity() {
        return density;
    }
    
    /**
     * Gets the hardness of the resource.
     * 
     * @return The hardness value.
     */
    public float getHardness() {
        return hardness;
    }
    
    /**
     * Gets the toughness of the resource.
     * 
     * @return The toughness value.
     */
    public float getToughness() {
        return toughness;
    }
    
    /**
     * Gets the malleability of the resource.
     * 
     * @return The malleability value.
     */
    public float getMalleability() {
        return malleability;
    }
    
    /**
     * Gets the enchantability of the resource.
     * 
     * @return The enchantability value.
     */
    public float getEnchantability() {
        return enchantability;
    }
    
    /**
     * Sets the rarity of the resource.
     * 
     * @param rarity The rarity value.
     * 
     * @return The resource, for chain construction.
     */
    protected Resource setRarity(float rarity) {
        this.rarity = rarity;
        return this;
    }
    
    /**
     * Sets the density of the resource.
     * 
     * @param density The density value.
     * 
     * @return The resource, for chain construction.
     */
    protected Resource setDensity(float density) {
        this.density = density;
        return this;
    }
    
    /**
     * Sets the hardness of the resource.
     * 
     * @param hardness The hardness value.
     * 
     * @return The resource, for chain construction.
     */
    protected Resource setHardness(float hardness) {
        this.hardness = hardness;
        return this;
    }
    
    /**
     * Sets the toughness of the resource.
     * 
     * @param toughness The toughness value.
     * 
     * @return The resource, for chain construction.
     */
    protected Resource setToughness(float toughness) {
        this.toughness = toughness;
        return this;
    }
    
    /**
     * Sets the malleability of the resource.
     * 
     * @param malleability The malleability value.
     * 
     * @return The malleability, for chain construction.
     */
    protected Resource setMalleability(float malleability) {
        this.malleability = malleability;
        return this;
    }
    
    /**
     * Sets the enchantability of the resource.
     * 
     * @param enchantability The enchantability value.
     * 
     * @return The resource, for chain construction.
     */
    protected Resource setEnchantability(float enchantability) {
        this.enchantability = enchantability;
        return this;
    }
    
}
