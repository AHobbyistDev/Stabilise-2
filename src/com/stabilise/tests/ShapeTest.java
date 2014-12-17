package com.stabilise.tests;

import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.MathUtil;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.shape.AxisAlignedBoundingBox;
import com.stabilise.util.shape.FastAABB;
import com.stabilise.util.shape.Polygon;

public class ShapeTest {
	
	public ShapeTest(boolean print) {
		final AxisAlignedBoundingBox b1 = new AxisAlignedBoundingBox(-0.5f, -0.5f, 1f, 1f);
		final FastAABB b2 = new FastAABB(-0.5f, -0.5f, 1f, 1f);
		final AxisAlignedBoundingBox b1Pre = b1.precomputed();
		final FastAABB b2Pre = b2.precomputed();
		
		int collisions = 16384*8;
		
		time(print, "AABB v FastAABB as AbstractPolygon", collisions, () -> b1.intersects(b2));
		time(print, "AABB v FastAABB as AABB", collisions, () -> b1.intersectsAABB(b2));
		time(print, "AABB v FastAABB as AbstractPolygon (Precomputed)", collisions, () -> b1Pre.intersects(b2Pre));
		time(print, "AABB v FastAABB as AABB (Precomputed)", collisions, () -> b1Pre.intersectsAABB(b2Pre));
		
		final Polygon p = circlePoly(32);
		final Polygon pPre = p.precomputed();
		
		time(print, "Polygon v AABB", collisions, () -> p.intersects(b1));
		time(print, "Polygon v FastAABB", collisions, () -> p.intersects(b2));
		time(print, "Polygon (Pre) v AABB", collisions, () -> pPre.intersects(b1));
		time(print, "Polygon (Pre) v FastAABB", collisions, () -> pPre.intersects(b2));
		time(print, "Polygon v AABB (Pre)", collisions, () -> p.intersects(b1Pre));
		time(print, "Polygon v FastAABB (Pre)", collisions, () -> p.intersects(b2Pre));
		time(print, "Polygon (Pre) v AABB (Pre)", collisions, () -> pPre.intersects(b1Pre));
		time(print, "Polygon (Pre) v FastAABB (Pre)", collisions, () -> pPre.intersects(b2Pre));
		
		time(print, "Polygon (BotLeft) v FastAABB (Pre)", collisions, () -> p.translate(-1f, -1f).intersects(b2Pre));
		time(print, "Polygon (TopRight) v FastAABB (Pre)", collisions, () -> p.translate(1f, 1f).intersects(b2Pre));
	}
	
	private void time(boolean print, String name, int iterations, Runnable task) {
		TaskTimer t = new TaskTimer(name);
		t.start();
		while(iterations-- > 0)
			task.run();
		t.stop();
		if(print)
			t.printResult(TimeUnit.MILLISECONDS);
	}
	
	private Polygon circlePoly(int vertices) {
		if(vertices < 3)
			throw new IllegalArgumentException();
		Vector2[] verts = new Vector2[vertices];
		verts[0] = Vector2.X;
		double increments = MathUtil.TAU / vertices;
		double angle = 0D;
		for(int i = 1; i < vertices; i++) {
			angle += increments;
			verts[i] = new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
		}
		return new Polygon(verts);
	}
	
	public static void main(String[] args) {
		System.out.println("Starting warmup...\n----------\n");
		// warmup
		for(int i = 0; i < 10; i++) {
			System.out.println("Doing warmup " + i);
			new ShapeTest(false);
		}
		System.out.println("\nStarting benchmark...\n----------\n");
		
		// benchmarks
		new ShapeTest(true);
	}
	
}
