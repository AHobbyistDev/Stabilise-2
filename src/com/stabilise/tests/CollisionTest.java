package com.stabilise.tests;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.Polygon;
import com.stabilise.util.shape2.Polygon2;

public class CollisionTest {
	
	public CollisionTest(int warmup) {
		boolean print = warmup == 0;
		if(!print)
			System.out.println("Doing warmup " + warmup);
		
		final int times = 512*512;
		final int verts = 32;
		Polygon p11 = circlePoly1(verts, 0f, 0f);
		Polygon p12 = circlePoly1(verts, 3f, 3f);
		Polygon2 p21 = circlePoly2(verts, 0f, 0f);
		Polygon2 p22 = circlePoly2(verts, 3f, 3f);
		
		TaskTimer t1in  = run("[v1] p1 v p1", times, print, () -> p11.intersects(p11));
		TaskTimer t1out = run("[v1] p1 v p2", times, print, () -> p11.intersects(p12));
		TaskTimer t2in  = run("[v2] p1 v p1", times, print, () -> p21.intersects(p21));
		TaskTimer t2out = run("[v2] p1 v p2", times, print, () -> p21.intersects(p22));
		
		if(warmup == 0) {
			t2in.printComparison(t1in);
			t2out.printComparison(t1out);
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
	
	public static Polygon circlePoly1(int n, float x, float y) {
		Vec2[] verts = new Vec2[n];
		float angle = Maths.TAUf / n;
		for(int i = 0; i < n; i++) {
			verts[i] = Vec2.immutable(
					x + MathUtils.cos(i*angle),
					y + MathUtils.sin(i*angle)
			);
		}
		return new Polygon(verts);
	}
	
	public static Polygon2 circlePoly2(int n, float x, float y) {
		float[] verts = new float[2*n];
		float angle = Maths.TAUf / n;
		for(int i = 0; i < n; i++) {
			verts[2*i] = x + MathUtils.cos(i*angle);
			verts[2*i+1] = y + MathUtils.sin(i*angle);
		}
		return new Polygon2(verts);
	}
	
	public static void main(String[] args) {
		// Do warmups
		int warmups = 5;
		for(int i = 1; i <= warmups; i++)
			new CollisionTest(i);
		new CollisionTest(0);
		
	}
	
}
