package com.stabilise.core.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GDXTestState implements State {
	
	private SpriteBatch batch;
	private BitmapFont font12;
	
	private Viewport viewport;
	
	public GDXTestState() {
		
	}
	
	@Override
	public void start() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.absolute("D:/USB Backup; 2014-07-05 - Full/Useful Stuff/Fonts/BLKCHCRY.ttf")
		);
		FreeTypeFontParameter param = new FreeTypeFontParameter();
		param.size = 128; // font size 12 pixels
		font12 = generator.generateFont(param);
		generator.dispose(); // don't forget to dispose to avoid memory leaks!
		
		batch = new SpriteBatch();
		
		viewport = new ScreenViewport();
	}
	
	@Override
	public void dispose() {
		font12.dispose();
		batch.dispose();
	}
	
	@Override
	public void pause() {
		
	}
	
	@Override
	public void resume() {
		
	}
	
	@Override
	public void resize(int width, int height) {
		System.out.println("Resized to " + width + "x" + height);
		viewport.update(width, height);
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin();
		font12.draw(batch, "Hello world!", 0, 128);
		batch.end();
	}
	
}
