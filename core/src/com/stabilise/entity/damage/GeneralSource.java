package com.stabilise.entity.damage;


public class GeneralSource implements IDamageSource {
    
    private final boolean original;
    
    public int damage;
    public DamageType type;
    public final long sourceID;
    public float fx = 0f, fy = 0f;
    
    
    protected GeneralSource(boolean original, DamageType type, long sourceID, int damage) {
        this.original = original;
        this.type = type;
        this.sourceID = sourceID;
        this.damage = damage;
    }
    
    @Override
    public int damage() {
        return damage;
    }
    
    public GeneralSource setDamage(int damage) {
        this.damage = damage;
        return this;
    }
    
    @Override
    public DamageType type() {
        return type;
    }
    
    @Override
    public long sourceID() {
        return sourceID;
    }
    
    @Override
    public float impulseX() {
        return fx;
    }
    
    @Override
    public float impulseY() {
        return fy;
    }
    
    @Override
    public GeneralSource clone() {
        return original ? doClone() : this;
    }
    
    protected GeneralSource doClone() {
        GeneralSource s = new GeneralSource(false, type, sourceID, damage);
        s.fx = fx;
        s.fy = fy;
        return s;
    }
    
    public static GeneralSource forAttack(long srcID, int damage) {
        return new GeneralSource(true, DamageType.ATTACK, srcID, damage);
    }
    
    public static GeneralSource fire(int damage) {
        return new GeneralSource(true, DamageType.FIRE, -1, damage);
    }
    
}
