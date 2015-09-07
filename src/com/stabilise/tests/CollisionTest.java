package com.stabilise.tests;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.Polygon;
import com.stabilise.util.shape2.Polygon2;
import com.stabilise.util.shape3.Collider;
import com.stabilise.util.shape3.Polygon3;

public class CollisionTest {
    
    public CollisionTest(int warmup) {
        boolean print = warmup == 0;
        if(!print)
            System.out.println("Doing warmup " + warmup);
        else
            System.out.println("Doing test...");
        
        final int times = 512*8;
        final int verts = 32;
        final float dx = 0.5f;
        final float dy = 0.5f;
        Polygon p1 = circlePoly1(verts);
        Polygon2 p2 = circlePoly2(verts);
        Polygon3 p3 = circlePoly3(verts);
        
        TaskTimer t1  = run("[v1]", times, print, () -> p1.intersects(p1.translate(dx, dy)));
        TaskTimer t2 = run("[v2]", times, print, () -> p2.intersects(p2.translate(dx, dy)));
        TaskTimer t3  = run("[v3]", times, print, () -> Collider.intersects(p3, p3, dx, dy));
        
        if(warmup == 0) {
            t1.printComparison(t2);
            t2.printComparison(t3);
            t3.printComparison(t1);
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
    
    public static Polygon circlePoly1(int n) {
        Vec2[] verts = new Vec2[n];
        float angle = Maths.TAUf / n;
        for(int i = 0; i < n; i++) {
            verts[i] = Vec2.immutable(
                    MathUtils.cos(i*angle),
                    MathUtils.sin(i*angle)
            );
        }
        return new Polygon(verts);
    }
    
    public static Polygon2 circlePoly2(int n) {
        float[] verts = new float[2*n];
        float angle = Maths.TAUf / n;
        for(int i = 0; i < n; i++) {
            verts[2*i] = MathUtils.cos(i*angle);
            verts[2*i+1] =  MathUtils.sin(i*angle);
        }
        return new Polygon2(verts);
    }
    
    public static Polygon3 circlePoly3(int n) {
        float[] verts = new float[2*n];
        float angle = Maths.TAUf / n;
        for(int i = 0; i < n; i++) {
            verts[2*i] = MathUtils.cos(i*angle);
            verts[2*i+1] =  MathUtils.sin(i*angle);
        }
        return new Polygon3(verts);
    }
    
    public static void main(String[] args) {
        // Do warmups
        int warmups = 5;
        for(int i = 1; i <= warmups; i++)
            new CollisionTest(i);
        new CollisionTest(0);
        
    }
    
}
