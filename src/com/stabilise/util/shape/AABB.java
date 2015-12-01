package com.stabilise.util.shape;

/**
 * An Axis-Aligned Bounding Box, or AABB for short, is a rectangle whose edges
 * are aligned with the x and y axes. AABBs are typically much faster than
 * ordinary polygons due to the large number of assumptions that can be made
 * about their behaviour.
 */
public class AABB extends Shape {
    
    /** Array indices */
    public static final int XMIN = 0, YMIN = 1, XMAX = 2, YMAX = 3;
    
    /** The min and max vertices of this AABB. */
    final float[] verts;
    
    
    /**
     * Creates a new AABB. It is implicitly trusted that the given values for
     * {@code width} and {@code height} are non-negative.
     * 
     * @param x The x-coordinate of the AABB's bottom-left vertex.
     * @param y The y-coordinate of the AABB's bottom-left vertex.
     * @param width The AABB's width.
     * @param height The AABB's height.
     */
    public AABB(float x, float y, float width, float height) {
        verts = new float[] {
            x, y,
            x + width, y + height
        };
    }
    
    /**
     * Creates a new AABB. No checking is done on the provided vertex array.
     */
    public AABB(float[] verts) {
        this.verts = verts;
    }
    
    @Override
    public AABB transform(VertexFunction f) {
        float[] newVerts = new float[4];
        f.apply(newVerts, XMIN, verts[XMIN], verts[YMIN]);
        f.apply(newVerts, XMAX, verts[XMAX], verts[YMAX]);
        return new AABB(newVerts);
    }
    
    /**
     * Throws an {@code UnsupportedOperationException}.
     */
    @Override
    public AABB rotate(float rotation) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot rotate an AABB!");
    }
    
    @Override
    public AABB reflect() {
        // We need to manually do this since AABB breaks if v00 is to the right
        // of v11, as we make algorithmic simplifications on the assumption
        // that v00 is always to the left of v11.
        return new AABB(new float[] {
            -verts[XMAX], verts[YMIN],
            -verts[XMIN], verts[YMAX]
        });
    }
    
    @Override
    protected float[] getVertices() {
        return new float[] {
                verts[XMIN], verts[YMIN], //v00
                verts[XMAX], verts[YMIN], //v10
                verts[XMAX], verts[YMAX], //v11
                verts[XMIN], verts[YMAX]  //v01
        };
    }
    
    /**
     * Gets the x-coordinate of the bottom-left vertex - or the origin - of
     * this AABB.
     */
    public float minX() {
        return verts[XMIN];
    }
    
    /**
     * Gets the y-coordinate of the bottom-left vertex - or the origin - of
     * this AABB.
     */
    public float minY() {
        return verts[YMIN];
    }
    
    /**
     * Gets the x-coordinate of the top-right vertex of this AABB.
     */
    public float maxX() {
        return verts[XMAX];
    }
    
    /**
     * Gets the y-coordinate of the top-right vertex of this AABB.
     */
    public float maxY() {
        return verts[YMAX];
    }
    
    /**
     * Calculates and returns the width of this AABB.
     */
    public float width() {
        return maxX() - minX();
    }
    
    /**
     * Calculates and returns the height of this AABB.
     */
    public float height() {
        return maxY() - minY();
    }
    
    /** {@link #width() width}{@code () / 2} */
    public float centreX() {
        return width() / 2;
    }
    
    /** {@link #height() height}{@code () / 2} */
    public float centreY() {
        return height() / 2;
    }
    
    int getKey() {
        return Collider.K_AABB;
    }
    
}
