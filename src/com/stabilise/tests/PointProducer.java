package com.stabilise.tests;

public class PointProducer {

	public PointProducer() {
		
	}
	
	/*
	 * for converting points in flash to spritesheet points for the character
	 * model spritesheet
	 */
	
	public static final float pixelsPerTile = 55;
	public static final int originX = 125;
	public static final int originY = 56;
	public static final int height = 256;
	
	public static void producePoints(int x, int y) {
		int originalX = x - originX;
		int originalY = height - originY - y;
		float scaledX = originalX / pixelsPerTile;
		float scaledY = originalY / pixelsPerTile;
		System.out.println("[X:"+originalX+", Y:"+originalY+"]");
		System.out.println("X:"+scaledX+", Y:"+scaledY);
	}
	
	public static void main(String[] args) {
		producePoints(139,77);
	}

}
