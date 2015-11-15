package com.stabilise.screen.menugdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.stabilise.core.Resources;
import com.stabilise.core.app.Application;
import com.stabilise.core.main.Stabilise;
import com.stabilise.core.state.LoadingState;


public class MainMenu implements Screen {
    
    private ScreenViewport viewport;
    private Stage stage;
    private TextureAtlas atlas;
    private Skin skin;
    private Table table;
    private TextButton btnPlay, btnExit;
    private BitmapFont font;
    private Label heading;
    
    public MainMenu() {
        
    }
    
    @Override
    public void show() {
        viewport = new ScreenViewport();
        
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        
        FileHandle atlasSrc = Resources.IMAGE_DIR.child("sheets/pack.atlas");
        atlas = new TextureAtlas(atlasSrc);
        skin = new Skin(atlas);
        
        table = new Table(skin);
        
        FreeTypeFontParameter textParam = new FreeTypeFontParameter();
        textParam.size = 32;
        font = Resources.font("arialbd.ttf", textParam);
        
        LabelStyle headingStyle = new LabelStyle(font, Color.WHITE);
        heading = new Label(Stabilise.GAME_NAME, headingStyle);
        heading.setFontScale(5f);
        
        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.up = skin.getDrawable("btnup");
        btnStyle.down = skin.getDrawable("btndown");
        btnStyle.pressedOffsetX = 1;
        btnStyle.pressedOffsetY = -1;
        btnStyle.font = font;
        btnStyle.fontColor = Color.BLACK;
        
        btnPlay = new TextButton("Play", btnStyle);
        btnPlay.pad(15);
        btnPlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                Application.get().setState(new LoadingState());
            }
        });
        
        btnExit = new TextButton("Exit", btnStyle);
        btnExit.pad(15);
        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                Application.get().shutdown();
            }
        });
        
        table.add(heading).spaceBottom(50).row();
        table.add(btnPlay).spaceBottom(20).row();
        table.add(btnExit).spaceBottom(50).row();
        //table.debug();
        stage.addActor(table);
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        table.invalidateHierarchy();
        table.setSize(width, height);
    }
    
    @Override
    public void pause() {
        
    }
    
    @Override
    public void resume() {
        
    }
    
    @Override
    public void hide() {
        
    }
    
    @Override
    public void dispose() {
        stage.dispose();
        atlas.dispose();
        skin.dispose();
        font.dispose();
    }
    
}
