package com.stabilise.tests;

import static java.lang.Math.*;

import com.badlogic.gdx.math.Matrix3;

class MatrixStuff {
	
	public static void main(String[] args) {
		Matrix3 m1 = new Matrix3(new float[] {
				1f, 0f, 1f,
				0f, 0f, 0f,
				1f, 0f, 1f
		});
		Matrix3 m2 = new Matrix3(new float[] {
				2f, 1f, 1f,
				1f, 0f, -1f,
				1f, -1f, 2f
		});
		Matrix3 u = new Matrix3(new float[] {
				-1/(float)sqrt(6), 1/(float)sqrt(3), 1/(float)sqrt(2),
				2/(float)sqrt(6), 1/(float)sqrt(3), 0f,
				1/(float)sqrt(6), -1/(float)sqrt(3), 1/(float)sqrt(2)
		});
		Matrix3 u_t = new Matrix3(u).transpose();
		
		Matrix3 m1_diag = new Matrix3(u_t).mul(m1).mul(u);
		Matrix3 m2_diag = new Matrix3(u_t).mul(m2).mul(u);
		
		System.out.println(m1_diag + "\n");
		System.out.println(m2_diag + "\n");
		System.out.println(new Matrix3(m1).mul(m2));
		System.out.println(new Matrix3(m2).mul(m1));
	}
	
}
