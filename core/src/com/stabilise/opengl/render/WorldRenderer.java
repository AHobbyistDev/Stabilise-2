package com.stabilise.opengl.render;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.stabilise.core.Application;
import com.stabilise.core.Resources;
import com.stabilise.core.game.Game;
import com.stabilise.entity.*;
import com.stabilise.entity.component.controller.PlayerController;
import com.stabilise.entity.component.core.*;
import com.stabilise.entity.particle.*;
import com.stabilise.item.Item;
import com.stabilise.item.Items;
import com.stabilise.opengl.render.model.ModelPlayer;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Shape;
import com.stabilise.world.AbstractWorld;
import com.stabilise.world.Slice;

/**
 * The WorldRenderer class handles the rendering of a world and its
 * inhabitants.
 */
public class WorldRenderer implements Renderer {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    private static final float DEFAULT_COL = new Color(1f, 1f, 1f, 1f).toFloatBits();
    
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    float pixelsPerTile = 32;
    
    /** Holds a reference to the world. */
    public final AbstractWorld world;
    /** The camera. */
    public final GameCamera playerCamera;
    public final Entity player;
    public final PlayerController controller;
    
    //public final HUDRenderer hudRenderer;
    
    public TileRenderer tileRenderer;
    public HUDRenderer hudRenderer;
    
    /** The number of tiles which may fit horizontally and vertically within
     * half of the screen. */
    int tilesHorizontal, tilesVertical;
    /** The number of slices which may fit horizontally and vertically within
     * half of the screen. */
    int slicesHorizontal, slicesVertical;
    
    SpriteBatch batch;
    OrthographicCamera camera;
    OrthographicCamera hudCamera;
    ScreenViewport viewport;
    ScreenViewport hudViewport;
    ShaderProgram shader;
    
    BitmapFont indicatorFontRed;
    BitmapFont indicatorFontOrange;
    BitmapFont debugFont;
    BitmapFont msgFont;
    
    // Textures for different game objects
    TextureRegion texEnemy;
    TextureRegion texFireball;
    TextureRegion texPortalClosed;
    TextureRegion texPortalOpened;
    TextureRegion texExplosion;
    TextureRegion texFlame;
    TextureRegion texSmoke;
    TextureRegion[] texItems;
    
    ModelPlayer personModel;
    
    TextureAtlas atlas;
    Skin skin;
    
    // Shape renderer for debug
    ShapeRenderer shapes;
    public boolean renderHitboxes = false;
    
    private final Vector2 vec = new Vector2();
    private final List<ParticleIndicator> indicators = new ArrayList<>();
    
    // List for automatic resource disposal
    private List<Disposable> disposables = new ArrayList<>();
    
    private final Log log = Log.getAgent("WorldRenderer");
    private final Profiler profiler = Application.get().profiler;
    
    
    /**
     * Creates a new world renderer.
     * 
     * @param game The game.
     * @param world The game world.
     * @param player The player entity.
     */
    public WorldRenderer(Game game, AbstractWorld world, Entity player, PlayerController controller) {
        super();
        
        this.world = world;
        
        this.player = player;
        playerCamera = world.camera;
        this.controller = controller;
        
        tileRenderer = new TileRenderer(this);
        hudRenderer = new HUDRenderer(game, this);
        
        loadResources();
    }
    
    @Override
    public void loadResources() {
        //shader = register(new BetterShader());
        //if(!shader.isCompiled())
        //    throw new RuntimeException("Shader could not compile: " + shader.getLog());
        
        batch = register(new SpriteBatch(4096));
        
        camera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        hudViewport = new ScreenViewport(hudCamera);
        
        FreeTypeFontGenerator fontGen = new FreeTypeFontGenerator(
                Gdx.files.classpath("arialbd.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = 17;
        param.borderWidth = 1.5f;
        param.borderColor = Color.WHITE;
        param.color = Color.RED;
        indicatorFontRed = register(fontGen.generateFont(param));
        param.color = Color.ORANGE;
        indicatorFontOrange = register(fontGen.generateFont(param));
        
        param = new FreeTypeFontParameter();
        param.size = 17;
        param.shadowOffsetX = 2;
        param.shadowOffsetY = 2;
        param.shadowColor = Color.BLACK;
        debugFont = register(fontGen.generateFont(param));
        msgFont = register(fontGen.generateFont(param));
        
        fontGen.dispose();
        
        FileHandle atlasSrc = Resources.DIR_IMG.child("atlasIngame.atlas");
        atlas = register(new TextureAtlas(atlasSrc));
        skin = register(new Skin(atlas));
        
        //for(AtlasRegion r : atlas.getRegions()) {
        //    System.out.println("Detected region: " + r.name);
        //}
        
        personModel = register(new ModelPlayer());
        
        texEnemy = skin.getRegion("entity/enemy");
        texFireball = skin.getRegion("entity/fireball");
        texPortalClosed = skin.getRegion("entity/portalClosed");
        texPortalOpened = skin.getRegion("entity/portalOpen");
        
        texExplosion = skin.getRegion("particle/explosion");
        texFlame = skin.getRegion("particle/flame");
        texSmoke = skin.getRegion("particle/smoke");
        
        texItems = new TextureRegion[8]; // TODO: temp length
        Item.ITEMS.forEachEntry(i -> {
            if(i._2 <= 1) return; // NO_ITEM, ItemTile
            texItems[i._2] = skin.getRegion("item/" + i._1.split(":")[1]);
        });
        
        //background = new Rectangle(screen.getWidth(), screen.getHeight());
        //----background.fill(new Colour(0x92D1E4));
        //background.colourVertices(new Colour(0xFFFFFF), new Colour(0xFF00FF), new Colour(0x00FFFF), new Colour(0xFFFF00));
        //background.gradientTopToBottom(new Colour(0x000000), new Colour(0xC9300E));
        
        tileRenderer.loadResources();
        hudRenderer.loadResources();
        
        shapes = register(new ShapeRenderer());
        
        setPixelsPerTile(pixelsPerTile, false); // kickstarts things
    }
    
    private <T extends Disposable> T register(T d) {
        disposables.add(d);
        return d;
    }
    
    @Override
    public void unloadResources() {
        for(Disposable d : disposables)
            d.dispose();
        
        tileRenderer.unloadResources();
        hudRenderer.unloadResources();
    }
    
    /**
     * @throws ArithmeticException if {@code pixelsPerTile} is {@code 0}.
     */
    public void setPixelsPerTile(float pixelsPerTile, boolean resize) {
        this.pixelsPerTile = pixelsPerTile;
        viewport.setUnitsPerPixel(1f / pixelsPerTile);
        
        if(resize)
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    
    public float getPixelsPerTile() {
        return pixelsPerTile;
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hudViewport.update(width, height);
        
        tilesHorizontal = (int)(width / (2 * pixelsPerTile)) + 1;
        tilesVertical = (int)(height / (2 * pixelsPerTile)) + 1;
        slicesHorizontal = Maths.ceil((float)tilesHorizontal / Slice.SLICE_SIZE);
        slicesVertical = Maths.ceil((float)tilesVertical / Slice.SLICE_SIZE);
    }
    
    private void updateMatrices(boolean hud) {
        Matrix4 mat = hud ? hudCamera.combined : camera.combined;
        batch.setProjectionMatrix(mat);
        shapes.setProjectionMatrix(mat);
    }
    
    /**
     * Gets the world coordinates over which the mouse is hovering.
     */
    public Vector2 mouseCoords() {
        return viewport.unproject(vec.set(Gdx.input.getX(), Gdx.input.getY()));
    }
    
    @Override
    public void update() {
        //if(world.loading) return; 
        
        profiler.start("hud"); // root.update.renderer.hud
        hudRenderer.update();
        
        profiler.next("tileRenderer"); // root.update.renderer.tileRenderer
        tileRenderer.update();
        
        profiler.next("camera"); // root.update.renderer.camera
        playerCamera.update(world);
        camera.position.set((float)playerCamera.pos.getGlobalX(), (float)playerCamera.pos.getGlobalY(), 0f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        
        profiler.end(); // root.update.renderer
    }
    
    @Override
    public void render() {
        profiler.start("background"); // root.render.background
        Color bCol = new Color(0x92D1E4FF); // RGBA is annoying in this case: ARGB > RGBA
        Gdx.gl.glClearColor(bCol.r, bCol.g, bCol.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        updateMatrices(false);
        
        batch.begin();
        
        profiler.next("tiles"); // root.render.tiles
        tileRenderer.render();
        
        batch.setColor(DEFAULT_COL);
        
        profiler.next("entities"); // root.render.entities
        profiler.start("nonplayer"); // root.render.entities.nonplayer
        
        // Temporary way of ensuring the player is rendered on top
        Entity playerEntity = null;
        
        for(Entity e : world.getEntities()) {
            if(e.isPlayerControlled())
                playerEntity = e;
            else
                e.render(this);
        }
        batch.flush();
        
        profiler.next("player");
        if(playerEntity != null)
            playerEntity.render(this); // render the player on top
        else
            log.postWarning("No player entity to render!");
        profiler.end();
        
        profiler.next("particles"); // root.render.particles
        world.getParticles().forEach(p -> p.render(this));
        
        doRenderIndicators();
        
        batch.end();
        
        profiler.next("hitboxes"); // root.render.hitboxes
        if(renderHitboxes) {
            shapes.begin(ShapeType.Line);
            shapes.setColor(Color.BLUE);
            world.getEntities().forEach(
                    e -> renderAABB(e.aabb, (float)e.pos.getGlobalX(), (float)e.pos.getGlobalY())
            );
            shapes.setColor(Color.RED);
            world.getHitboxes().forEach(
                    h -> renderShape(h.boundingBox, (float)h.pos.getGlobalX(), (float)h.pos.getGlobalY())
            );
            shapes.end();
        }
        
        profiler.next("hud"); // root.render.hud
        
        batch.begin();
        
        profiler.start("cursor"); // root.render.hud.cursor
        renderCursorItem();
        
        updateMatrices(true);
        
        profiler.next("hud"); // root.render.hud.hud
        hudRenderer.render();
        
        profiler.next("endbatch");
        batch.end();
        
        profiler.end(); // root.render.hud
        profiler.end(); // root.render
        
    }
    
    // ----------Entity rendering----------
    
    private void renderOn(TextureRegion tex, Entity e) {
        batch.draw(
                tex, // region
                (float)e.pos.getGlobalX() + e.aabb.minX(), // x
                (float)e.pos.getGlobalY() + e.aabb.minY(), // y
                e.aabb.width(), // width
                e.aabb.height() // height
        );
    }
    
    /**
     * Renders an enemy entity.
     */
    public void renderEnemy(Entity e, CGenericEnemy c) {
        if(c.hasTint) {
            if(c.dead)
                batch.setColor(c.tintStrength, 0f, 0f, 1f);
            else
                batch.setColor(c.tintStrength, c.tintStrength, c.tintStrength, 1);
        }
        
        /*
        batch.draw(
                texEnemy, // texture
                (float)e.x - 0.5f, // x
                (float)e.y, // y
                0f, // originX
                0f, // originY
                1f, // width
                2f, // height
                1f, // scaleX
                1f, // scaleY
                0f, // rotation
                0, // srcX
                0, // srcY
                texEnemy.getWidth(), // srcWidth
                texEnemy.getHeight(), // srcHeight
                !e.facingRight, // flipX
                false // flipY
        );
        */
        batch.draw(
                texEnemy, // region
                (float)e.pos.getGlobalX() - (e.facingRight ? 0.5f : -0.5f), // x
                (float)e.pos.getGlobalY(), // y
                0f, // originX
                0f, // originY
                1f, // width
                2f, // height
                e.facingRight ? 1f : -1f, // scaleX
                1f, // scaleY
                0f // rotation
        );
        
        batch.setColor(DEFAULT_COL);
    }
    
    /**
     * Renders a fireball entity.
     */
    public void renderFireball(Entity e, CFireball c) {
        /*
        batch.draw(
                texFireball, // texture
                (float)e.x - 0.75f, // x
                (float)e.y - 0.25f, // y
                0.75f, // originX
                0.25f, // originY
                1f, // width
                0.5f, // height
                1f, // scaleX
                1f, // scaleY
                Maths.toDegrees(c.rotation), // rotation
                0, // srcX
                0, // srcY
                texFireball.getWidth(), // srcWidth
                texFireball.getHeight(), // srcHeight
                false, //!e.facingRight, // flipX
                false // flipY
        );
        */
        batch.draw(
                texFireball, // region
                (float)e.pos.getGlobalX() - 0.75f, // x
                (float)e.pos.getGlobalY() - 0.25f, // y
                0.75f, // originX
                0.25f, // originY
                1f, // width
                0.5f, // height
                1f, // scaleX
                1f, // scaleY
                Maths.toDegrees(c.rotation) // rotation
        );
    }
    
    /**
     * Renders an item entity.
     */
    public void renderItem(Entity e, CItem c) {
        if(c.stack.getItem().equals(Items.TILE))
            renderOn(tileRenderer.tiles[c.stack.getData()], e);
        else
            renderOn(texItems[c.stack.getItem().getID()], e);
            //renderOn(shtItems.getRegion(c.stack.getItem().getID() - 1), e);
    }
    
    /**
     * Renders a person entity.
     */
    public void renderPerson(Entity e, CPerson s) {
        personModel.setFlipped(!e.facingRight);
        personModel.setState(s.getState(), s.stateTicks);
        personModel.render(batch, (float)e.pos.getGlobalX(), (float)e.pos.getGlobalY());
    }
    
    public void renderPortal(Entity e, CPortal c) {
        renderOn(c.isOpen() ? texPortalOpened : texPortalClosed, e);
    }
    
    // ----------Particle rendering----------
    
    /**
     * Renders a damage indicator particle.
     * 
     * @param p The damage indicator particle.
     */
    public void renderIndicator(ParticleIndicator p) {
        indicators.add(p);
    }
    
    private void doRenderIndicators() {
        updateMatrices(true);
        for(ParticleIndicator p : indicators) {
            BitmapFont fnt = p.orange ? indicatorFontOrange : indicatorFontRed;
            hudViewport.unproject(viewport.project(vec.set((float)p.pos.getGlobalX(), (float)p.pos.getGlobalY())));
            fnt.draw(batch, p.text, vec.x - 25, -vec.y, 50, Align.center, false);
        }
        indicators.clear();
        updateMatrices(false);
    }
    
    /**
     * Renders an explosion particle.
     * 
     * @param p The explosion particle.
     */
    public void renderExplosion(ParticleExplosion p) {
        batch.setColor(p.colour);
        /*
        batch.draw(
                texExplosion, // texture
                (float)p.x, // x
                (float)p.y, // y
                0.5f, // originX
                0.5f, // originY
                1f, // width
                1f, // height
                p.radius, // scaleX
                p.radius, // scaleY
                0f, // rotation
                0, // srcX
                0, // srcY
                texExplosion.getWidth(), // srcWidth
                texExplosion.getHeight(), // srcHeight
                false, // flipX
                false // flipY
        );
        */
        batch.draw(
                texExplosion, // region
                (float)p.pos.getGlobalX() - 0.5f, // x
                (float)p.pos.getGlobalY() - 0.5f, // y
                0.5f, // originX
                0.5f, // originY
                1f, // width
                1f, // height
                p.radius, // scaleX
                p.radius, // scaleY
                0f // rotation
        );
        batch.setColor(DEFAULT_COL);
    }
    
    /**
     * Renders a flame particle.
     * 
     * @param p The flame particle.
     */
    public void renderFlame(ParticleFlame p) {
        batch.setColor(1f, 1f, 1f, p.opacity);
        batch.draw(
                texFlame, // region
                (float)p.pos.getGlobalX() - 0.125f, // x
                (float)p.pos.getGlobalY() - 0.125f, // y
                0.25f, // originX
                0.25f, // originY
                0.25f, // width
                0.25f, // height
                1f, // scaleX
                1f, // scaleY
                0f // rotation
        );
        batch.setColor(DEFAULT_COL);
    }
    
    /**
     * Renders a smoke particle.
     * 
     * @param p The smoke particle.
     */
    public void renderSmoke(ParticleSmoke p) {
        batch.setColor(1f, 1f, 1f, p.opacity);
        batch.draw(
                texSmoke, // region
                (float)p.pos.getGlobalX() - 0.25f, // x
                (float)p.pos.getGlobalY() - 0.25f, // y
                0.25f, // originX
                0.25f, // originY
                0.25f, // width
                0.25f, // height
                1f, // scaleX
                1f, // scaleY
                0f // rotation
        );
        batch.setColor(DEFAULT_COL);
    }
    
    private void renderCursorItem() {
        batch.setColor(1f, 1f, 1f, 0.5f);
        TextureRegion r = tileRenderer.tiles[controller.tileID];
        controller.doInRadius(this, (x,y) -> batch.draw(
                r, // region
                Position.tileCoordFreeToTileCoordFixed(x), // x
                Position.tileCoordFreeToTileCoordFixed(y), // y
                1f, // width
                1f // height
        ));
        /*
        ItemStack stack = player.inventory.getStack(player.curSlot);
        if(stack == ItemStack.NO_STACK)
            return;
        if(stack.getItem().equals(Items.TILE))
            batch.draw(
                    tileRenderer.tiles.getRegion(7 + stack.getData()), // region
                    coords.x - 0.5f, // x
                    coords.y - 0.5f, // y
                    1f, // width
                    1f // height
            );
        else
            batch.draw(
                    shtItems.getRegion(stack.getItem().getID() - 1), // region
                    coords.x - 0.5f, // x
                    coords.y - 0.5f, // y
                    1f, // width
                    1f // height
            );
        */
        batch.setColor(DEFAULT_COL);
    }
    
    // Shape rendering --------------------------------------------------------
    
    private void renderAABB(AABB aabb, float x, float y) {
        shapes.rect(
                aabb.minX() + x, // x
                aabb.minY() + y, // y
                aabb.width(), // width
                aabb.height() // height
        );
    }
    
    private void renderShape(Shape s, float x, float y) {
        float[] verts = s.cpyVertices();
        for(int i = 0; i < verts.length; i += 2) {
            verts[i]   += x;
            verts[i+1] += y;
        }
        shapes.polygon(verts);
    }
    
}
