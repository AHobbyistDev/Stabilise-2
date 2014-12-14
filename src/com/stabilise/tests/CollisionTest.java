package com.stabilise.tests;

import com.stabilise.core.Application;
import com.stabilise.core.Resources;
import com.stabilise.core.state.State;
import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.opengl.Sprite;
import com.stabilise.opengl.Texture;
import com.stabilise.util.Colour;
import com.stabilise.util.Log;
import com.stabilise.util.shape.Rectangle;
import com.stabilise.util.shape.RotatableShape;

public class CollisionTest extends Application {
	
	public CollisionTest() {
		super(60);
	}
	
	/*
	@Override
	protected Screen getScreen() {
		return ScreenLWJGL.get(500, 500, "Collision Test");
	}
	*/
	
	@Override
	protected State getInitialState() {
		return new State() {
			
			private final float rotation = 0.2f;
			private final float rotationRadians = rotation * (float)Math.PI / 180;
			
			public RotatableShape<Rectangle> r1;
			public RotatableShape<Rectangle> r2;
			
			public Font font;
			@SuppressWarnings("unused")
			public FontStyle style;
			public Sprite r1Sprite;
			public Sprite r2Sprite;
			
			@SuppressWarnings("unused")
			public final int xOffset = 4;
			public final int r1Width = 40;
			public final int r1Height = 40;
			public final int r2Width = 40;
			public final int r2Height = 40;
			public int r1X, r1Y, r2X, r2Y;
			
			@SuppressWarnings("unused")
			public boolean collides = false;
			
			@Override
			public void update() {
				r1Sprite.rotation += rotation * 3;
				r2Sprite.rotation += rotation * 7;
				
				r1.rotate(rotationRadians * 3);
				r2.rotate(rotationRadians * 7);
				
				// Oddity: Compiler complains if I don't first invoke get() on r1:
				// "The type AbstractPolygon is not visible"; eclipse's only
				// suggestion is to make translate(x,y) public, but it already is...
				collides = r1.translate(r1X, r1Y).intersects(r2.get().translate(r2X, r2Y));
			}
			
			@Override
			public void render(float delta) {
				r1Sprite.draw();
				r2Sprite.draw();
				
				//font.drawLine("Collision: " + collides, screen.getCentreX(), screen.getCentreY() - 50, style);
			}
			
			@Override
			public void start() {
				r1 = new RotatableShape<Rectangle>(new Rectangle(-r1Width/2, -r1Height/2, r1Width, r1Height));
				r2 = new RotatableShape<Rectangle>(new Rectangle(-r2Width/2, -r2Height/2, r2Width, r2Height));
				
				font = new Font("sheets/font1");
				style = new FontStyle(8, Colour.BLACK, FontStyle.Alignment.CENTRE, 4, 0);
				
				r1Sprite = new Sprite("square");
				r2Sprite = new Sprite("square");
				
				r1Sprite.setPivot(r1Sprite.getWidth() / 2, r1Sprite.getHeight() / 2);
				r1Sprite.setScaledWidth(r1Width);
				r1Sprite.setScaledHeight(r1Height);
				r1Sprite.filter(Texture.NEAREST);
				//r1Sprite.setScale(r1Sprite.getScaleX() * 2);
				
				//r1Sprite.x = (int) (-25 * r1Sprite.getScaleX());
				/*
				r1Sprite.x = r1X = screen.getWidth()/2 - r1Width/2 - xOffset;
				r1Sprite.y = r1Y = screen.getHeight() / 2;
				*/
				
				r2Sprite.setPivot(r2Sprite.getWidth() / 2, r2Sprite.getHeight() / 2);
				r2Sprite.setScaledWidth(r2Width);
				r2Sprite.setScaledHeight(r2Height);
				r2Sprite.filter(Texture.NEAREST);
				//r2Sprite.setScale(r1Sprite.getScaleX() * 2);
				
				/*
				r2Sprite.x = r2X = screen.getWidth()/2 + r2Width/2 + xOffset;
				r2Sprite.y = r2Y = screen.getHeight() / 2;
				*/
				
				Log.getAgent().logIntegers(r1X, r1Y, r2X, r2Y);
			}
			
			@Override
			public void pause() {
				// nothing
			}
			
			@Override
			public void resume() {
				// nothing
			}
			
			@Override
			public void dispose() {
				font.destroy();
				r1Sprite.destroy();
				r2Sprite.destroy();
			}
			
			@Override
			public void resize(int width, int height) {
				
			}
			
		};
	}
	
	@Override
	protected void produceCrashLog() {
		// do nothing
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.getAgent().logMessage("The application path is: " + Resources.APP_DIR);
		
		// Load the OS-specific natives required for lwjgl to run
		System.setProperty("org.lwjgl.librarypath", Resources.NATIVES_DIR.getAbsolutePath());
		
		new CollisionTest();
	}
	
}
