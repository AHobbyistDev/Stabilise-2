package com.stabilise.tests;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.util.shape.old.*;

@SuppressWarnings("deprecation")
public class CollisionTest {
    
    public CollisionTest(int warmup) {
        boolean print = warmup == 0;
        if(!print)
            System.out.println("Doing warmup " + warmup);
        else
            System.out.println("Doing test...");
        print = true;
        
        final int times = 512*256;
        final int verts = 32;
        final float dx = 1.5f;
        final float dy = 0.5f;
        PolygonOld p1 = circlePoly1(verts);
        AABBOld b1 = new AABBOld(0, 0, 1, 1);
        Polygon p2 = circlePoly2(verts);
        AABB b2 = new AABB(0, 0, 1, 1);
        
        TaskTimer t11  = run("[v1 1/2]", times, print, () -> p1.intersects(p1.translate(dx, dy)));
        TaskTimer t21  = run("[v2 1/2]", times, print, () -> p2.intersects(p2, dx, dy));
        TaskTimer t12  = run("[v1 2/2]", times, print, () -> p1.intersects(b1.translate(dx, dy)));
        TaskTimer t22  = run("[v2 2/2]", times, print, () -> p2.intersects(b2, -dx, -dy));
        
        if(print) {
            t11.printComparison(t21);
            t12.printComparison(t22);
        }
    }
    
    public static TaskTimer run(String s, int times, boolean print, BooleanSupplier f) {
        TaskTimer t = new TaskTimer(s);
        t.start();
        while(--times > 0)
            f.getAsBoolean();
        boolean result = f.getAsBoolean();
        t.stop();
        if(print) {
            System.out.print(result + ": ");
            t.printResult(TimeUnit.MILLISECONDS);
        }
        return t;
    }
    
    public static PolygonOld circlePoly1(int n) {
        Vec2[] verts = new Vec2[n];
        float angle = Maths.TAUf / n;
        for(int i = 0; i < n; i++) {
            verts[i] = Vec2.immutable(
                    MathUtils.cos(i*angle),
                    MathUtils.sin(i*angle)
            );
        }
        return new PolygonOld(verts);
    }
    
    public static Polygon circlePoly2(int n) {
        float[] verts = new float[2*n];
        float angle = Maths.TAUf / n;
        for(int i = 0; i < n; i++) {
            verts[2*i] = MathUtils.cos(i*angle);
            verts[2*i+1] =  MathUtils.sin(i*angle);
        }
        return new Polygon(verts);
    }
    
    public static void main(String[] args) {
        // Do warmups
        int warmups = 5;
        for(int i = 1; i <= warmups; i++)
            new CollisionTest(i);
        new CollisionTest(0);
        
    }
    
}
