package com.stabilise.core.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class MenuTestState implements State {
    
    private Viewport viewport;
    
    private SpriteBatch batch;
    private Texture texSplash;
    private Sprite sprSplash;
    private BitmapFont font;
    
    private ShapeRenderer shapes;
    
    private Texture texBtnUp, texBtnOver, texBtnDown;
    private TextureRegion regBtnUp, regBtnOver, regBtnDown;
    
    // Scene2D stuff
    private Stage stage;
    private Table table;
    private TextButton button;
    
    public MenuTestState() {
        
    }
    
    @Override
    public void start() {
        viewport = new ScreenViewport();
        
        batch = new SpriteBatch(128);
        
        texSplash = new Texture(Gdx.files.absolute("C:/Users/Adam/AppData/Roaming/.stabilise/res/img/loading.png"));
        sprSplash = new Sprite(texSplash);
        
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.absolute("C:/Users/Adam/AppData/Roaming/.stabilise/res/fonts/arialbd.ttf")
        );
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = 16; // font size 16 pixels
        font = generator.generateFont(param);
        generator.dispose();
        
        shapes = new ShapeRenderer();
        
        texBtnUp = new Texture(Gdx.files.absolute("C:/Users/Adam/Desktop/img/btnup.png"));
        texBtnOver = new Texture(Gdx.files.absolute("C:/Users/Adam/Desktop/img/btnover.png"));
        texBtnDown = new Texture(Gdx.files.absolute("C:/Users/Adam/Desktop/img/btndown.png"));
        regBtnUp = new TextureRegion(texBtnUp);
        regBtnOver = new TextureRegion(texBtnOver);
        regBtnDown = new TextureRegion(texBtnDown);
        
        // <-----<=- Scene2D -=>----->
        
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        TextButtonStyle style = new TextButtonStyle();
        style.up = new TextureRegionDrawable(regBtnUp);
        style.over = new TextureRegionDrawable(regBtnOver);
        style.down = new TextureRegionDrawable(regBtnDown);
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.DARK_GRAY;
        style.downFontColor = Color.LIGHT_GRAY;
        button = new TextButton("Click me!", style);
        
        table.addActor(button);
    }
    
    @Override
    public void predispose() {
        
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        
        texSplash.dispose();
        font.dispose();
        
        shapes.dispose();
        
        stage.dispose();
        
        texBtnUp.dispose();
        texBtnDown.dispose();
        texBtnOver.dispose();
    }
    
    @Override
    public void pause() {
        
    }
    
    @Override
    public void resume() {
        
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        
        sprSplash.setPosition(-sprSplash.getWidth()/2, -sprSplash.getHeight()/2);
    }
    
    @Override
    public void update() {
        
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        
        shapes.begin(ShapeType.Filled);
        shapes.setColor(Color.LIGHT_GRAY);
        shapes.rect(-width/2, -height/2, width, height);
        shapes.end();
        
        batch.begin();
        sprSplash.draw(batch);
        font.draw(batch, "Test thingy", -150, -100, 300, Align.center, true);
        batch.end();
        
        stage.draw();
        table.drawDebug(shapes);
    }
    
}
