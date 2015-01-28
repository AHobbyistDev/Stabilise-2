package com.stabilise.opengl.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.stabilise.core.Application;
import com.stabilise.core.Game;
import com.stabilise.core.Resources;
import com.stabilise.entity.*;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticleDamageIndicator;
import com.stabilise.entity.particle.ParticleExplosion;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.opengl.TextureSheet;
import com.stabilise.opengl.render.model.ModelPlayer;
import com.stabilise.util.Profiler;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.Slice;

/**
 * The WorldRenderer class handles the rendering of a world and its
 * inhabitants.
 */
public class WorldRenderer implements Renderer {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The font style used for the loading screen. */
	//private static final FontStyle STYLE_LOADING_SCREEN = new FontStyle(36, Colour.BLACK, FontStyle.Alignment.CENTRE, 4, 0);
	
	private static final float COL_WHITE = Color.WHITE.toFloatBits();
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	float pixelsPerTile = 32;
	
	/** Holds a reference to the world. */
	public final ClientWorld<?> world;
	
	//public final HUDRenderer hudRenderer;
	public TileRenderer tileRenderer;
	/** The number of tiles which may fit horizontally and vertically within
	 * half of the screen. */
	int tilesHorizontal, tilesVertical;
	/** The number of slices which may fit horizontally and vertically within
	 * half of the screen. */
	int slicesHorizontal, slicesVertical;
	
	SpriteBatch batch;
	OrthographicCamera camera;
	ScreenViewport viewport;
	
	BitmapFont font;
	
	// Textures for different game objects
	Texture texEnemy;
	Texture texFireball;
	Texture texExplosion;
	TextureSheet shtItems;
	TextureSheet shtParticles;
	
	ModelPlayer personModel;
	
	private final Profiler profiler = Application.get().profiler;
	
	
	/**
	 * Creates a new world renderer.
	 * 
	 * @param game The game.
	 * @param world The game world.
	 */
	public WorldRenderer(Game game, ClientWorld<?> world) {
		super();
		
		this.world = world;
		
		tileRenderer = new TileRenderer(this);
		//hudRenderer = new HUDRenderer(game, this);
		
		loadResources();
	}
	
	@Override
	public void loadResources() {
		camera = new OrthographicCamera();
		viewport = new ScreenViewport(camera);
		batch = new SpriteBatch();
		
		FreeTypeFontParameter param = new FreeTypeFontParameter();
		param.size = 16;
		font = Resources.font("arialbd", param);
		
		personModel = new ModelPlayer();
		
		texEnemy = Resources.texture("enemy");
		texEnemy.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		//texEnemy.setPivot(texEnemy.getTextureWidth() / 2, 0);
		
		//arrowSprite = new Sprite("arrow");
		//arrowSprite.setPivot(arrowSprite.getTextureWidth() * 3 / 4, arrowSprite.getTextureHeight() / 2);
		//arrowSprite.filter(Texture.NEAREST);
		
		texFireball = Resources.texture("fireball");
		texFireball.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		//texFireball.setPivot(texFireball.getTextureWidth(), texFireball.getTextureHeight() / 2);
		
		texExplosion = Resources.texture("explosion");
		texExplosion.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		//texExplosion.setPivot(texExplosion.getTextureWidth()/2, texExplosion.getTextureHeight()/2);
		
		shtItems = TextureSheet.sequentiallyOptimised(Resources.texture("sheets/items"), 8, 8);
		shtItems.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		
		shtParticles = TextureSheet.sequentiallyOptimised(Resources.texture("sheets/particles"), 8, 8);
		shtParticles.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		
		//background = new Rectangle(screen.getWidth(), screen.getHeight());
		//----background.fill(new Colour(0x92D1E4));
		//background.colourVertices(new Colour(0xFFFFFF), new Colour(0xFF00FF), new Colour(0x00FFFF), new Colour(0xFFFF00));
		//background.gradientTopToBottom(new Colour(0x000000), new Colour(0xC9300E));
		
		//hudRenderer.loadResources();
		
		setPixelsPerTile(pixelsPerTile, false); // kickstarts things
	}

	@Override
	public void unloadResources() {
		font.dispose();
		
		texEnemy.dispose();
		texFireball.dispose();
		texExplosion.dispose();
		
		shtItems.dispose();
		shtParticles.dispose();
		
		personModel.dispose();
		
		tileRenderer.unloadResources();
		//hudRenderer.unloadResources();
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
		batch.setProjectionMatrix(viewport.getCamera().combined);
		
		tilesHorizontal = (int)((width/2) / pixelsPerTile) + 1;
		tilesVertical = (int)((height/2) / pixelsPerTile) + 1;
		slicesHorizontal = MathsUtil.ceil((float)tilesHorizontal / Slice.SLICE_SIZE);
		slicesVertical = MathsUtil.ceil((float)tilesVertical / Slice.SLICE_SIZE);
	}
	
	@Override
	public void update() {
		//if(world.loading) return; 
		
		profiler.start("hud"); // root.update.renderer.hud
		//hudRenderer.update();
		profiler.next("tileRenderer"); // root.update.renderer.tileRenderer
		tileRenderer.update();
		profiler.end(); // // root.update.renderer
	}
	
	@Override
	public void render() {
		camera.position.set((float)world.camera.x, (float)world.camera.y, 0f);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		profiler.start("background"); // root.render.background
		Color bCol = new Color(0x92D1E4FF); // RGBA is annoying in this case: ARGB > RGBA
		Gdx.gl.glClearColor(bCol.r, bCol.g, bCol.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin();
		
		profiler.next("tiles"); // root.render.tiles
		tileRenderer.render();
		
		profiler.next("entities"); // root.render.entities
		// Temporary way of ensuring the player is rendered on top
		Entity playerEntity = null;
		for(EntityMob e : world.getPlayers()) {
			playerEntity = e;
			break;
		}
		for(Entity e : world.getEntities())
			if(e != playerEntity)
				e.render(this);
		playerEntity.render(this);
		
		profiler.next("particles"); // root.render.particles
		for(Particle p : world.getParticles())
			p.render(this);
		
		profiler.next("hud"); // root.render.hud
		//hudRenderer.render();
		
		profiler.end(); // root.render
		
		batch.end();
	}
	
	// ----------Entity rendering----------
	
	/**
	 * Renders an arrow entity.
	 * 
	 * @param e The arrow entity.
	 */
	public void renderArrow(EntityArrow e) {
		/*
		arrowSprite.x = (float) (e.x * scale + offsetX);
		arrowSprite.y = (float) (e.y * scale + offsetY);
		arrowSprite.rotation = (float) Math.toDegrees(((EntityArrow) e).rotation);
		if(e.facingRight && arrowSprite.getFlipped())
			arrowSprite.setPivot(arrowSprite.getTextureWidth(), arrowSprite.getTextureHeight() / 2);
		else if(!e.facingRight && !arrowSprite.getFlipped())
			arrowSprite.setPivot(0, arrowSprite.getTextureHeight() / 2);
		arrowSprite.setFlipped(!e.facingRight);
		arrowSprite.draw();
		*/
	}
	
	/**
	 * Renders a big fireball entity.
	 * 
	 * @param e The big fireball entity.
	 */
	public void renderBigFireball(EntityBigFireball e) {
		renderFireball(e);
	}
	
	/**
	 * Renders an enemy entity.
	 * 
	 * @param e The enemy entity.
	 */
	public void renderEnemy(EntityEnemy e) {
		if(e.hasTint) {
			if(e.dead) {
				//texEnemy.tint(Color.RED, e.tintStrength);
			} else {
				//texEnemy.tint(Color.WHITE, e.tintStrength);
			}
		} else {
			//texEnemy.removeTint();
		}
		
		batch.draw(
				texEnemy, // texture
				(float)e.x, // x
				(float)e.y, // y
				0.5f, // originX
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
	}
	
	/**
	 * Renders a fireball entity.
	 * 
	 * @param e The fireball entity.
	 */
	public void renderFireball(EntityProjectile e) {
		batch.draw(
				texFireball, // texture
				(float)e.x, // x
				(float)e.y, // y
				0.75f, // originX
				0.5f, // originY
				1f, // width
				0.5f, // height
				1f, // scaleX
				1f, // scaleY
				(float)Math.toDegrees(e.rotation), // rotation
				0, // srcX
				0, // srcY
				texFireball.getWidth(), // srcWidth
				texFireball.getHeight(), // srcHeight
				false,//!e.facingRight, // flipX
				false // flipY
		);
	}
	
	/**
	 * Renders an item entity.
	 * 
	 * @param e The item entity.
	 */
	public void renderItem(EntityItem e) {
		batch.draw(
				shtItems.getRegion(e.stack.getItem().getID() - 1), // region
				(float)e.x, // x
				(float)e.y, // y
				0.5f, // originX
				0f, // originY
				1f, // width
				1f, // height
				1f, // scaleX
				1f, // scaleY
				0f, // rotation
				false // clockwise
		);
	}
	
	/**
	 * Renders a person entity.
	 * 
	 * @param e The person entity.
	 */
	public void renderPerson(EntityPerson e) {
		personModel.setFlipped(!e.facingRight);
		personModel.setState(e.getState(), e.stateTicks);
		personModel.render(batch, (float)e.x, (float)e.y);
	}
	
	// ----------Particle rendering----------
	
	/**
	 * Renders a damage indicator particle.
	 * 
	 * @param p The damage indicator particle.
	 */
	public void renderDamageIndicator(ParticleDamageIndicator p) {
		// TODO
		//font.drawLine(p.text, (int)(p.x * scale) + offsetX, (int)(p.y * scale) + offsetY, p.getFontStyle());
	}
	
	/**
	 * Renders an explosion particle.
	 * 
	 * @param p The explosion particle.
	 */
	public void renderExplosion(ParticleExplosion p) {
		batch.setColor(p.colour);
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
		batch.setColor(COL_WHITE);
	}
	
	/**
	 * Renders a flame particle.
	 * 
	 * @param p The flame particle.
	 */
	public void renderFlame(ParticleFlame p) {
		batch.setColor(1f, 1f, 1f, p.opacity);
		batch.draw(
				shtParticles.getRegion(2), // region
				(float)p.x, // x
				(float)p.y, // y
				0.25f, // originX
				0.25f, // originY
				0.25f, // width
				0.25f, // height
				1f, // scaleX
				1f, // scaleY
				0f // rotation
		);
		batch.setColor(COL_WHITE);
	}
	
	/**
	 * Renders a smoke particle.
	 * 
	 * @param p The smoke particle.
	 */
	public void renderSmoke(ParticleSmoke p) {
		batch.draw(
				shtParticles.getRegion(0), // region
				(float)p.x, // x
				(float)p.y, // y
				0.25f, // originX
				0.25f, // originY
				0.25f, // width
				0.25f, // height
				1f, // scaleX
				1f, // scaleY
				0f // rotation
		);
	}
	
}
