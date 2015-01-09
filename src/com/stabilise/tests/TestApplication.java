package com.stabilise.tests;

import com.stabilise.util.AppDriver;
import com.stabilise.util.Log;


public class TestApplication {
	
	final AppDriver driver;
	
	public TestApplication() {
		driver = new AppDriver(60, 60, Log.getAgent("TestApp")) {
			
			@Override
			protected void update() {
				if(driver.getUpdateCount() % 60 == 0)
					System.out.println(driver.profiler.getData().toString());
			}
			
			@Override
			protected void render() {
				
			}
			
		};
		driver.profiler.enable();
		driver.run();
	}
	
	public static void main(String[] args) {
		new TestApplication();
	}
	
}
