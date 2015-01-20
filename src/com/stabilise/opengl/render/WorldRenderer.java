package com.stabilise.opengl.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.stabilise.core.Application;
import com.stabilise.core.Game;
import com.stabilise.entity.*;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticleDamageIndicator;
import com.stabilise.entity.particle.ParticleExplosion;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.opengl.render.model.ModelPlayer;
import com.stabilise.util.Profiler;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.IWorld;
import com.stabilise.world.SingleplayerWorld;
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
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The scale at which the world is rendered.
	 * Note that non-nice numbers (which I'm assuming to be non-integers for
	 * now) can cause graphical weirdness with tiles. */
	float scale = 48;
	
	/** Holds a reference to the world. */
	public final ClientWorld<?> world;
	
	/** The tile renderer. */
	public TileRenderer tileRenderer;
	/** The number of tiles which may fit horizontally on the screen. */
	int tilesHorizontal;
	/** The number of tiles which may fit vertically on the screen. */
	int tilesVertical;
	/** The number of slices which may fit horizontally on the screen. */
	int slicesHorizontal;
	/** The number of slices which may fit vertically on the screen. */
	int slicesVertical;
	
	SpriteBatch batch;
	
	Viewport viewport;
	
	BitmapFont font;
	
	// Textures for different game objects
	Texture texEnemy;
	Texture texFireball;
	Texture texExplosion;
	Texture texItems;
	Texture texParticles;
	
	/** The player model. */
	ModelPlayer personModel;
	
	/** The HUD renderer. */
	public final HUDRenderer hudRenderer;
	
	/** The background. */
	Rectangle background;
	
	/** The x-offset of everything due to the camera. */
	public int offsetX;
	/** The y-offset of everything due to the camera. */
	public int offsetY;
	
	/** The profiler. */
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
		hudRenderer = new HUDRenderer(game, this);
		
		loadResources();
	}
	
	@Override
	public void loadResources() {
		viewport = new ScreenViewport();
		
		font = new Font("sheets/font1");
		
		personModel = new ModelPlayer();
		
		texEnemy = new Sprite("enemy");
		texEnemy.setPivot(texEnemy.getTextureWidth() / 2, 0);
		texEnemy.filter(Texture.NEAREST);
		
		//arrowSprite = new Sprite("arrow");
		//arrowSprite.setPivot(arrowSprite.getTextureWidth() * 3 / 4, arrowSprite.getTextureHeight() / 2);
		//arrowSprite.filter(Texture.NEAREST);
		
		texFireball = new Sprite("fireball");
		texFireball.setPivot(texFireball.getTextureWidth(), texFireball.getTextureHeight() / 2);
		texFireball.filter(Texture.NEAREST);
		
		texExplosion = new Sprite("explosion");
		texExplosion.setPivot(texExplosion.getTextureWidth()/2, texExplosion.getTextureHeight()/2);
		texExplosion.filter(Texture.LINEAR);
		
		texItems = new SpriteSheet("sheets/items", 8, 8);
		texItems.filter(Texture.NEAREST);
		
		texParticles = new SpriteSheet("sheets/particles", 8, 8);
		texParticles.filter(Texture.NEAREST);
		
		//background = new Rectangle(screen.getWidth(), screen.getHeight());
		background.fill(new Colour(0x92D1E4));
		//background.colourVertices(new Colour(0xFFFFFF), new Colour(0xFF00FF), new Colour(0x00FFFF), new Colour(0xFFFF00));
		//background.gradientTopToBottom(new Colour(0x000000), new Colour(0xC9300E));
		
		hudRenderer.loadResources();
		
		setScale(scale);
	}

	@Override
	public void unloadResources() {
		font.destroy();
		personModel.destroy();
		texEnemy.destroy();
		//arrowSprite.destroy();
		texFireball.destroy();
		texExplosion.destroy();
		texItems.destroy();
		texParticles.destroy();
		background.destroy();
		tileRenderer.unloadResources();
		hudRenderer.unloadResources();
	}
	
	@Override
	public void resize(int width, int height) {
		
	}
	
	@Override
	public void update() {
		//if(world.loading) return; 
		
		/*
		if(screen.wasResized()) {
			recalculateTiles();
			background.setSize(screen.getWidth(), screen.getHeight());
		}
		*/
		
		profiler.start("hud");
		hudRenderer.update();
		profiler.next("tileRenderer");
		tileRenderer.update();
		profiler.end();
	}
	
	@Override
	public void render() {
		/*
		offsetX = (int) (screen.getCentreX() - world.camera.x * scale);
		offsetY = (int) (screen.getCentreY() - world.camera.y * scale);
		*/
		
		profiler.start("background");
		background.draw();
		
		profiler.next("tiles");
		tileRenderer.render();
		
		profiler.next("entities");
		// Temporary way of ensuring the player is rendered on top
		boolean first = true;
		Entity playerEntity = null;
		for(Entity e : world.getEntities()) {
			if(first) {
				playerEntity = e;
				first = false;
			} else {
				e.render(this);
			}
		}
		playerEntity.render(this);
		
		profiler.next("particles");
		for(Particle p : world.getParticles())
			p.render(this);
		
		profiler.next("hud");
		hudRenderer.render();
		
		profiler.end();
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
		// TODO: for now the fireball and big fireball code is the same, and the repetition is hence inelegant
		
		texFireball.x = (float) (e.x * scale + offsetX);
		texFireball.y = (float) (e.y * scale + offsetY);
		texFireball.rotation = (float) Math.toDegrees(((EntityProjectile)e).rotation);
		if(e.facingRight && texFireball.getFlipped())
			texFireball.setPivot(texFireball.getTextureWidth() * 3 / 4, texFireball.getTextureHeight() / 2);
		else if(!e.facingRight && !texFireball.getFlipped())
			texFireball.setPivot(texFireball.getTextureWidth() * 1 / 4, texFireball.getTextureHeight() / 2);
		texFireball.setFlipped(!e.facingRight);
		texFireball.draw();
	}
	
	/**
	 * Renders an enemy entity.
	 * 
	 * @param e The enemy entity.
	 */
	public void renderEnemy(EntityEnemy e) {
		if(e.hasTint) {
			if(e.dead) {
				texEnemy.tint(Colour.RED, e.tintStrength);
				//mobSprite.setAlpha(m.tintStrength);
			} else {
				texEnemy.tint(Colour.WHITE, e.tintStrength);
			}
		} else {
			texEnemy.removeTint();
			//mobSprite.setAlpha(1.0f);
		}
		texEnemy.x = (float) (e.x * scale + offsetX);
		texEnemy.y = (float) (e.y * scale + offsetY);
		texEnemy.setFlipped(!e.facingRight);
		texEnemy.draw();
	}
	
	/**
	 * Renders a fireball entity.
	 * 
	 * @param e The fireball entity.
	 */
	public void renderFireball(EntityFireball e) {
		// TODO: for now the fireball and big fireball code is the same, and the repetition is hence inelegant
		
		texFireball.x = (float) (e.x * scale + offsetX);
		texFireball.y = (float) (e.y * scale + offsetY);
		texFireball.rotation = (float) Math.toDegrees(((EntityProjectile)e).rotation);
		if(e.facingRight && texFireball.getFlipped())
			texFireball.setPivot(texFireball.getTextureWidth() * 3 / 4, texFireball.getTextureHeight() / 2);
		else if(!e.facingRight && !texFireball.getFlipped())
			texFireball.setPivot(texFireball.getTextureWidth() * 1 / 4, texFireball.getTextureHeight() / 2);
		texFireball.setFlipped(!e.facingRight);
		texFireball.draw();
	}
	
	/**
	 * Renders an item entity.
	 * 
	 * @param e The item entity.
	 */
	public void renderItem(EntityItem e) {
		/*
		itemSprites.drawSprite(e.stack.getItem().getID() - 1,
				(int) ((e.x - 0.5f) * scale + offsetX),
				(int) (e.y * scale + offsetY));
		//*/
	}
	
	/**
	 * Renders a person entity.
	 * 
	 * @param e The person entity.
	 */
	public void renderPerson(EntityPerson e) {
		personModel.setFlipped(!e.facingRight);
		personModel.setState(e.getState(), e.stateTicks);
		personModel.render((int)Math.round(e.x * scale + offsetX), (int)Math.round(e.y * scale + offsetY));
	}
	
	// ----------Particle rendering----------
	
	/**
	 * Renders a damage indicator particle.
	 * 
	 * @param p The damage indicator particle.
	 */
	public void renderDamageIndicator(ParticleDamageIndicator p) {
		font.drawLine(p.text, (int)(p.x * scale) + offsetX, (int)(p.y * scale) + offsetY, p.getFontStyle());
	}
	
	/**
	 * Renders an explosion particle.
	 * 
	 * @param p The explosion particle.
	 */
	public void renderExplosion(ParticleExplosion p) {
		texExplosion.tint(p.colour);
		texExplosion.setAlpha(p.alpha);
		int size = (int)(p.radius * scale);
		texExplosion.setScaledDimensions(size, size);
		texExplosion.drawSprite((int)(p.x * scale + offsetX), (int)(p.y * scale + offsetY));
	}
	
	/**
	 * Renders a flame particle.
	 * 
	 * @param p The flame particle.
	 */
	public void renderFlame(ParticleFlame p) {
		texParticles.setAlpha(p.opacity);
		texParticles.drawSprite(2, 0, (int)(p.x * scale + offsetX - texParticles.getScaledSpriteWidth() / 2), (int)(p.y * scale + offsetY));
		texParticles.setAlpha(1.0f);
	}
	
	/**
	 * Renders a smoke particle.
	 * 
	 * @param p The smoke particle.
	 */
	public void renderSmoke(ParticleSmoke p) {
		texParticles.drawSprite(0, 0, (int)(p.x * scale + offsetX - texParticles.getScaledSpriteWidth() / 2), (int)(p.y * scale + offsetY));
	}
	
	/**
	 * Gets the scale of the world renderer.
	 * 
	 * @return The scale.
	 */
	public float getScale() {
		return scale;
	}
	
	/**
	 * Sets the scale of the renderer.
	 * 
	 * @param scale The new scale.
	 */
	public void setScale(float scale) {
		if(scale <= 0)
			return;
		
		this.scale = scale;
		
		personModel.rescale(scale);
		texEnemy.setScale(scale / texEnemy.getTextureWidth());
		//arrowSprite.setScale(scale / arrowSprite.getTextureWidth());
		texFireball.setScale(scale / texFireball.getTextureWidth());
		texItems.setScale(scale / texItems.getSpriteWidth());
		texParticles.setScale(scale * 0.25f / texParticles.getSpriteWidth());
		tileRenderer.tiles.setScale(scale / tileRenderer.tiles.getSpriteWidth());
		
		recalculateTiles();
	}
	
	/**
	 * Recalculates the number of tiles which may fit on the screen.
	 */
	private void recalculateTiles() {
		//tilesHorizontal = (int)(screen.getCentreX() / scale) + 1;
		//tilesVertical = (int)(screen.getCentreY() / scale) + 1;
		slicesHorizontal = (int)Math.ceil((float)tilesHorizontal / Slice.SLICE_SIZE);
		slicesVertical = (int)Math.ceil((float)tilesVertical / Slice.SLICE_SIZE);
	}
	
}
