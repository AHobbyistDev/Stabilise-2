package com.stabilise.opengl;

import com.stabilise.util.annotation.LWJGLReliant;

/**
 * An Animation is essentially a sprite-sheet, with each sprite constituting an
 * individual frame of the animation.
 */
@LWJGLReliant
public class Animation extends SpriteSheet {
	
	/** The number of frames in the animation. */
	private int frames;
	/** The current frame. */
	private int currFrame = 0;
	/** The number of ticks to elapse before switching to the next frame. */
	private int currFrameDuration = 2;
	/** The duration of each frame, in ticks. */
	private int frameDurations = 2;
	
	
	/**
	 * Creates a new Animation.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * animation's spritesheet texture.
	 * @param cols The number of columns in the animation's spritesheet.
	 * @param rows The number of rows in the animation's spritesheet.
	 * @param frames The number of frames in the animation.
	 */
	public Animation(String textureFile, int cols, int rows, int frames) {
		super(textureFile, cols, rows);
		init(frames);
	}
	
	/**
	 * Creates a new Animation.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * animation's spritesheet texture.
	 * @param cols The number of columns in the animation's spritesheet.
	 * @param rows The number of rows in the animation's spritesheet.
	 * @param frames The number of frames in the animation.
	 * @param marker The object which to mark the animation's spritesheet's
	 * texture.
	 */
	public Animation(String textureFile, int cols, int rows, int frames, Object marker) {
		super(textureFile, cols, rows, marker);
		init(frames);
	}
	
	/**
	 * Sets up the Animation.
	 * 
	 * @param frames The number of frames in the animation.
	 */
	private void init(int frames) {
		if(frames < 2 || frames > rows * cols)
			throw new IllegalArgumentException("Invalid number of frames (" + frames + ")!");
		
		this.frames = frames;
		
		setFrame(0);
	}
	
	/**
	 * Updates the animation.
	 * This should be invoked every tick to ensure the animation plays
	 * correctly.
	 */
	public void update() {
		currFrameDuration--;
		
		if(currFrameDuration == 0)
			setFrame((currFrame + 1) % frames);
	}
	
	/**
	 * Sets the animation's frame.
	 * 
	 * @param frame The frame to set as the current frame.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code frame < 0} or
	 * {@code frame} is greater than the number of frames in the animation.
	 */
	public void setFrame(int frame) {
		if(frame < 0 || frame > frames)
			throw new IllegalArgumentException("Attempted to set the animation to an invalid frame (" + frame + "/" + frames + ")!");
		
		currFrame = frame;
		currFrameDuration = frameDurations;
		
		setSprite(frame % cols, frame / cols);
	}
	
	/**
	 * Sets the duration of each frame, in ticks.
	 * 
	 * @param durations The duration of each frame, in ticks.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code durations < 1}.
	 */
	public void setFrameDurations(int durations) {
		if(durations < 1)
			throw new IllegalArgumentException("Illegal frame duration (" + durations + ")");
		
		frameDurations = durations;
	}
	
}
