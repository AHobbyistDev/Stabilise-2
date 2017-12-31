package com.stabilise.entity.model;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.maths.TransMat;
import com.stabilise.util.shape.Shape;

/**
 * todo
 */
@Incomplete
public class Bone {
    
    /** The bone's base, untransformed hitbox.*/
    protected final Shape baseHitbox;
    /** The bone's effective hitbox. */
    protected Shape hitbox;
    /** The local transformation matrix of this bone (encapsulates rotation &
     * translation). */
    public final TransMat localTransMat;
    /** The total transformation matrix of this bone. If this bone does not
     * have a parent, this is just a reference to localTransMat. If this bone
     * <i>does</i> have a parent, this is equal to {@code parent.totalTransMat
     * * this.localTransMat}. */
    public final TransMat totalTransMat;
    
    /** This bone's parent. The bone is located relative to the parent, and
     * transformations to the parent propagate down to us. May be null, which
     * means no parent. */
    public Bone parent = null;
    
    /** True if a property has been changed without updating the local/total
     * trans matrices. */
    private boolean dirty = false;
    /** True if the totalTransMat was recently updated. Used to indicate to any
     * child bones that they will need to update. */
    private boolean flushed = false;
    
    
    /**
     * Creates a new Bone.
     * 
     * @param hitbox
     */
    public Bone(Shape hitbox) {
        baseHitbox = this.hitbox = hitbox;
        localTransMat = totalTransMat = new TransMat();
    }
    
    public Bone(Shape hitbox, Bone parent) {
        baseHitbox = this.hitbox = hitbox;
        localTransMat = new TransMat();
        totalTransMat = new TransMat();
        this.parent = parent;
    }
    
    /**
     * Gets this bone's (appropriately transformed) hitbox.
     */
    public Shape getHitbox() {
        return hitbox;
    }
    
    /**
     * Sets the bone's rotation.
     * 
     * @param rotation The angle by which to rotate the bone anticlockwise
     * from its originally-defined position, in radians.
     */
    public void setRotation(float rotation) {
        
    }
    
    private void updateLocalTransMat() {
        
    }
    
    /**
     * Flushes positional changes to this bone. If this is a child bone, it
     * will be positioned appropriately relative to its parent.
     */
    protected void flush() {
        if(dirty) {
            updateLocalTransMat();
            if(parent != null)
                parent.totalTransMat.mul(localTransMat, totalTransMat);
            dirty = false;
            flushed = true;
        } else if(parent != null && parent.flushed){
            parent.totalTransMat.mul(localTransMat, totalTransMat);
            flushed = true;
        }
    }
    
}
