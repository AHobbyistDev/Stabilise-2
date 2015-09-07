package com.stabilise.entity2;


public class ECore {
    
    private int id;
    private final int uid;
    
    protected boolean destroyed = false;
    
    public ECore(int id, int uid) {
        this.id = id;
        this.uid = uid;
    }
    
    /** returns true if destroyed */
    public boolean update(WorldDemo world) {
        return destroyed;
    }
    
    public void destroy() {
        destroyed = true;
    }
    
    public int id() {
        return id;
    }
    
    public int uid() {
        return uid;
    }
    
}
