package com.stabilise.tests;

import com.stabilise.util.maths.Vec2;

public class PhysQ10 {
	
	public static void main(String[] args) {
		for(int n = 2; true; n++) {
			double w1 = new Configuration(n).addCircumference(n).calcWork();
			double w2 = new Configuration(n).addCircumference(n-1).addCentre().calcWork();
			if(w2 < w1) {
				System.out.println("For C2 with " + n + " electrons, there are "
						+ new Configuration(n-1).addCircumference(n-1).closeVectors()
						+ " electrons closer than the centre one.");
				break;
			}
		}
	}
	
	static class Configuration {
		
		Vec2[] electrons;
		int num = 0;
		
		Configuration(int electrons) {
			this.electrons = new Vec2[electrons];
		}
		
		Configuration addCircumference(int n) {
			if(n <= 0)
				return this;
			double angle = 2 * Math.PI / n;
			for(int i = 0; i < n; i++) {
				electrons[num++] = Vec2.immutable(
						(float)Math.cos(i*angle),
						(float)Math.sin(i*angle)
				);
			}
			return this;
		}
		
		Configuration addCentre() {
			electrons[num++] = Vec2.immutable(0, 0);
			return this;
		}
		
		private double dist(Vec2 v1, Vec2 v2) {
			Vec2 v = v1.sub(v2);
			return Math.sqrt(v.dot(v));
		}
		
		double calcWork() {
			// The total energy is the work it takes to stick an electron at a
			// point in an E-field: qV, which is proportional to 1/r.
			// So the total work is proportional to the sum across every
			// possible pair of electrons of 1/r
			double work = 0d;
			for(int i = 0; i < num; i++) {
				for(int j = i+1; j < num; j++) {
					work += 1/dist(electrons[i], electrons[j]);
				}
			}
			return work;
		}
		
		int closeVectors() {
			// Close vectors are ones for which the dist < 1
			int count = 0;
			Vec2 e0 = electrons[0];
			for(int i = 1; i < num; i++) {
				if(dist(e0, electrons[i]) < 1.0D)
					count++;
			}
			return count;
		}
		
	}

}
