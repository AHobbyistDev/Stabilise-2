package com.stabilise.tests;

import com.stabilise.util.maths.Vec2;
import com.stabilise.util.shape.Polygon;

public class Hw1 {
	
	/** r c xmin ymin xmax ymax x1 y1 x2 y2 x3 y3 */
	static final String[] INPUTS = {
			"7 11 -1 0 1 1 -0.4 -0.1 0.95 1.1 -1 0.33",
			"5 7 0 0 12 12 1 2 6 10 11 2",
			"9 11 0 0 20 16 3 1 17 1 17 15",
			"3 4 1 0 4 2 2 3 5 2 3 -1",
			"5 7 0 0 12 8 3 1 17 1 17 15",
			"3 4 0 0 6 4 1 1 3 2 5 3"
	};
	
	public Hw1(String input) {
		print(input);
		print(":\n");
		
		String[] args = input.split(" ");
		int a = 0;
		int rows = Integer.parseInt(args[a++]);
		int cols = Integer.parseInt(args[a++]);
		Vec2 min = Vec2.immutable(Float.parseFloat(args[a++]), Float.parseFloat(args[a++]));
		Vec2 max = Vec2.immutable(Float.parseFloat(args[a++]), Float.parseFloat(args[a++]));
		Vec2 v1 = Vec2.immutable(Float.parseFloat(args[a++]), Float.parseFloat(args[a++]));
		Vec2 v2 = Vec2.immutable(Float.parseFloat(args[a++]), Float.parseFloat(args[a++]));
		Vec2 v3 = Vec2.immutable(Float.parseFloat(args[a++]), Float.parseFloat(args[a++]));
		Polygon tri = new Polygon(v1, v2, v3);
		Vec2 p = Vec2.mutable(0, 0);
		
		float unitX = (max.x() - min.x()) / (cols - 1);
		float unitY = (max.y() - min.y()) / (rows - 1);
		
		print("+");
		for(int i = 0; i < cols; i++)
			print("-");
		print("+\n");
		
		for(int y = 0; y < rows; y++) {
			print("|");
			p.set(p.x(), max.y() - y*unitY);
			for(int x = 0; x < cols; x++) {
				p.set(min.x() + x*unitX, p.y());
				if(tri.containsPoint(p.x(), p.y()))
					print("*");
				else
					print(" ");
			}
			print("|\n");
		}
		
		print("+");
		for(int i = 0; i < cols; i++)
			print("-");
		print("+\n");
	}
	
	private static void print(String s) {
		System.out.print(s);
	}
	
	public static void main(String[] args) {
		for(String i : INPUTS)
			new Hw1(i);
	}
	
}
