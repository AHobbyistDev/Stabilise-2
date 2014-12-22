package com.stabilise.opengl.render;

import com.stabilise.core.Application;
import com.stabilise.core.Game;
import com.stabilise.entity.*;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticleDamageIndicator;
import com.stabilise.entity.particle.ParticleExplosion;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleSmoke;
import com.stabilise.opengl.render.model.ModelPlayer;
import com.stabilise.util.Colour;
import com.stabilise.util.Profiler;
import com.stabilise.world.GameWorld;
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
	public final GameWorld world;
	
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
	
	/** The display font. */
	Font font1;
	
	/** The sprite for generic enemies. TODO: Temporary */
	Sprite mobSprite;
	/** The sprite for fireballs. */
	Sprite fireballSprite;
	/** The sprite for explosions. */
	Sprite explosionSprite;
	/** Sprites for items */
	SpriteSheet itemSprites;
	/** Sprites for particles. */
	SpriteSheet particleSprites;
	
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
	public WorldRenderer(Game game, GameWorld world) {
		super();
		
		this.world = world;
		
		tileRenderer = new TileRenderer(this);
		hudRenderer = new HUDRenderer(game, this);
		
		loadResources();
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
		
		fireballSprite.x = (float) (e.x * scale + offsetX);
		fireballSprite.y = (float) (e.y * scale + offsetY);
		fireballSprite.rotation = (float) Math.toDegrees(((EntityProjectile)e).rotation);
		if(e.facingRight && fireballSprite.getFlipped())
			fireballSprite.setPivot(fireballSprite.getTextureWidth() * 3 / 4, fireballSprite.getTextureHeight() / 2);
		else if(!e.facingRight && !fireballSprite.getFlipped())
			fireballSprite.setPivot(fireballSprite.getTextureWidth() * 1 / 4, fireballSprite.getTextureHeight() / 2);
		fireballSprite.setFlipped(!e.facingRight);
		fireballSprite.draw();
	}
	
	/**
	 * Renders an enemy entity.
	 * 
	 * @param e The enemy entity.
	 */
	public void renderEnemy(EntityEnemy e) {
		if(e.hasTint) {
			if(e.dead) {
				mobSprite.tint(Colour.RED, e.tintStrength);
				//mobSprite.setAlpha(m.tintStrength);
			} else {
				mobSprite.tint(Colour.WHITE, e.tintStrength);
			}
		} else {
			mobSprite.removeTint();
			//mobSprite.setAlpha(1.0f);
		}
		mobSprite.x = (float) (e.x * scale + offsetX);
		mobSprite.y = (float) (e.y * scale + offsetY);
		mobSprite.setFlipped(!e.facingRight);
		mobSprite.draw();
	}
	
	/**
	 * Renders a fireball entity.
	 * 
	 * @param e The fireball entity.
	 */
	public void renderFireball(EntityFireball e) {
		// TODO: for now the fireball and big fireball code is the same, and the repetition is hence inelegant
		
		fireballSprite.x = (float) (e.x * scale + offsetX);
		fireballSprite.y = (float) (e.y * scale + offsetY);
		fireballSprite.rotation = (float) Math.toDegrees(((EntityProjectile)e).rotation);
		if(e.facingRight && fireballSprite.getFlipped())
			fireballSprite.setPivot(fireballSprite.getTextureWidth() * 3 / 4, fireballSprite.getTextureHeight() / 2);
		else if(!e.facingRight && !fireballSprite.getFlipped())
			fireballSprite.setPivot(fireballSprite.getTextureWidth() * 1 / 4, fireballSprite.getTextureHeight() / 2);
		fireballSprite.setFlipped(!e.facingRight);
		fireballSprite.draw();
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
		font1.drawLine(p.text, (int)(p.x * scale) + offsetX, (int)(p.y * scale) + offsetY, p.getFontStyle());
	}
	
	/**
	 * Renders an explosion particle.
	 * 
	 * @param p The explosion particle.
	 */
	public void renderExplosion(ParticleExplosion p) {
		explosionSprite.tint(p.colour);
		explosionSprite.setAlpha(p.alpha);
		int size = (int)(p.radius * scale);
		explosionSprite.setScaledDimensions(size, size);
		explosionSprite.drawSprite((int)(p.x * scale + offsetX), (int)(p.y * scale + offsetY));
	}
	
	/**
	 * Renders a flame particle.
	 * 
	 * @param p The flame particle.
	 */
	public void renderFlame(ParticleFlame p) {
		particleSprites.setAlpha(p.opacity);
		particleSprites.drawSprite(2, 0, (int)(p.x * scale + offsetX - particleSprites.getScaledSpriteWidth() / 2), (int)(p.y * scale + offsetY));
		particleSprites.setAlpha(1.0f);
	}
	
	/**
	 * Renders a smoke particle.
	 * 
	 * @param p The smoke particle.
	 */
	public void renderSmoke(ParticleSmoke p) {
		particleSprites.drawSprite(0, 0, (int)(p.x * scale + offsetX - particleSprites.getScaledSpriteWidth() / 2), (int)(p.y * scale + offsetY));
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
		mobSprite.setScale(scale / mobSprite.getTextureWidth());
		//arrowSprite.setScale(scale / arrowSprite.getTextureWidth());
		fireballSprite.setScale(scale / fireballSprite.getTextureWidth());
		itemSprites.setScale(scale / itemSprites.getSpriteWidth());
		particleSprites.setScale(scale * 0.25f / particleSprites.getSpriteWidth());
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

	@Override
	public void loadResources() {
		font1 = new Font("sheets/font1");
		
		personModel = new ModelPlayer();
		
		mobSprite = new Sprite("enemy");
		mobSprite.setPivot(mobSprite.getTextureWidth() / 2, 0);
		mobSprite.filter(Texture.NEAREST);
		
		//arrowSprite = new Sprite("arrow");
		//arrowSprite.setPivot(arrowSprite.getTextureWidth() * 3 / 4, arrowSprite.getTextureHeight() / 2);
		//arrowSprite.filter(Texture.NEAREST);
		
		fireballSprite = new Sprite("fireball");
		fireballSprite.setPivot(fireballSprite.getTextureWidth(), fireballSprite.getTextureHeight() / 2);
		fireballSprite.filter(Texture.NEAREST);
		
		explosionSprite = new Sprite("explosion");
		explosionSprite.setPivot(explosionSprite.getTextureWidth()/2, explosionSprite.getTextureHeight()/2);
		explosionSprite.filter(Texture.LINEAR);
		
		itemSprites = new SpriteSheet("sheets/items", 8, 8);
		itemSprites.filter(Texture.NEAREST);
		
		particleSprites = new SpriteSheet("sheets/particles", 8, 8);
		particleSprites.filter(Texture.NEAREST);
		
		//background = new Rectangle(screen.getWidth(), screen.getHeight());
		background.fill(new Colour(0x92D1E4));
		//background.colourVertices(new Colour(0xFFFFFF), new Colour(0xFF00FF), new Colour(0x00FFFF), new Colour(0xFFFF00));
		//background.gradientTopToBottom(new Colour(0x000000), new Colour(0xC9300E));
		
		hudRenderer.loadResources();
		
		setScale(scale);
	}

	@Override
	public void unloadResources() {
		font1.destroy();
		personModel.destroy();
		mobSprite.destroy();
		//arrowSprite.destroy();
		fireballSprite.destroy();
		explosionSprite.destroy();
		itemSprites.destroy();
		particleSprites.destroy();
		background.destroy();
		tileRenderer.unloadResources();
		hudRenderer.unloadResources();
	}

}
