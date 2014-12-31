package com.stabilise.core.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.stabilise.opengl.OrthoCamera;

public class GDXTestState implements State, InputProcessor {
	
	private SpriteBatch batch;
	private BitmapFont font12;
	private Texture texture;
	private Sprite sprite;
	
	private Viewport viewport;
	
	private Music music;
	
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
		
		texture = new Texture(Gdx.files.absolute("C:/Users/Adam/AppData/Roaming/.stabilise/res/img/loading.png"));
		batch = new SpriteBatch(256);
		
		sprite = new Sprite(texture);
		
		viewport = new ScreenViewport(new OrthoCamera());
		
		music = Gdx.audio.newMusic(Gdx.files.absolute("C:/Users/Adam/AppData/Roaming/.stabilise/res/sound/sarabande.wav"));
		music.setLooping(true);
		music.play();
		
		// -------------------------
		
		boolean isExtAvailable = Gdx.files.isExternalStorageAvailable();
		boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
		String extRoot = Gdx.files.getExternalStoragePath();
		String locRoot = Gdx.files.getLocalStoragePath();
		
		System.out.println("External: " + isExtAvailable + "; " + extRoot);
		System.out.println("Local: " + isLocAvailable + "; " + locRoot);
		
		//---------------------------
		
		Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void dispose() {
		font12.dispose();
		texture.dispose();
		batch.dispose();
		
		music.dispose();
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
		batch.setProjectionMatrix(viewport.getCamera().combined);
		
		sprite.setPosition(width/2 - sprite.getWidth()/2, height/2 - sprite.getHeight()/2);
	}
	
	@Override
	public void update() {
		
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin();
		sprite.draw(batch);
		font12.draw(batch, "Hello world!", 0, 128);
		batch.end();
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.ENTER) {
			Gdx.input.getTextInput(new TextInputListener() {
				public void input(String text) {
					System.out.println("Input: " + text);
				}
				public void canceled() {
					System.out.println("cancelled");
				}
			}, "Enter something pls", "", "right here");
		}
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		return false;
	}
	
	@Override
	public boolean keyTyped(char character) {
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
}
