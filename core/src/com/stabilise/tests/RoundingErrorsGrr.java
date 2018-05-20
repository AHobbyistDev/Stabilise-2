package com.stabilise.tests;

import com.stabilise.entity.Position;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;

/**
 * I'm going to understand the cause of this rounding error screwing with
 * tile collision even if it's the death of me.
 */
public class RoundingErrorsGrr {
    
    public static void main(String[] args) {
        reconstruction1();
    }
    
    public static void reconstruction1() {
        Position p1 = Position.create().add(0.4f, 0f);
        Position p2 = p1.clone().add(-0.00001f, 0f);
        AABB b = new AABB(-0.4f, 0f, 0.8f, 1.8f);
        float leadingEdge = b.minX();
        boolean res = Maths.floor(p2.lx()+leadingEdge) == Maths.floor(p1.lx()+leadingEdge);
        System.out.println(res);
    }
    
    public static void reconstruction2() {
        Position p1 = Position.create().add(0.4f, 0f);
        Position p2 = p1.clone().add(-0.0001f, 0f);
        AABB b = new AABB(-0.4f, 0f, 0.8f, 1.8f);
        float leadingEdge = b.minX();
        float x1 = p2.lx() + leadingEdge;
        float x2 = p1.lx() + leadingEdge;
        float tx1 = Maths.floor(x1);
        float tx2 = Maths.floor(x2);
        boolean res = tx1 == tx2;
        System.out.println(x1);
        System.out.println(x2);
        System.out.println(tx1);
        System.out.println(tx2);
        System.out.println(res);
    }
    
}
