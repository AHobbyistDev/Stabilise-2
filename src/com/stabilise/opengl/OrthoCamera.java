package com.stabilise.opengl;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * This class extends OrthographicCamera to move (0,0) from the centre of the
 * screen to the bottom-left.
 */
public class OrthoCamera extends OrthographicCamera {
	
	public OrthoCamera() {
		super();
	}
	
	/**
	 * Constructs a new OrthoCamera, using the given viewport width and height.
	 * For pixel perfect 2D rendering just supply the screen size, for other
	 * unit scales (e.g. meters for box2d) proceed accordingly. The camera will
	 * show the region [0, 0] - [viewportWidth, viewportHeight].
	 * 
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height
	 */
	public OrthoCamera(float viewportWidth, float viewportHeight) {
		super(viewportWidth, viewportHeight);
	}
	
	@Override
	public void update(boolean updateFrustum) {
		projection.setToOrtho(0, zoom * viewportWidth, 0, zoom * viewportHeight, near, far);
		view.setToLookAt(position, new Vector3().set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if(updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}
	
	/** Sets this camera to an orthographic projection, centered at (viewportWidth/2, viewportHeight/2), with the y-axis pointing up
	 * or down.
	 * @param yDown whether y should be pointing down.
	 * @param viewportWidth
	 * @param viewportHeight */
	public void setToOrtho (boolean yDown, float viewportWidth, float viewportHeight) {
		if (yDown) {
			up.set(0, -1, 0);
			direction.set(0, 0, 1);
		} else {
			up.set(0, 1, 0);
			direction.set(0, 0, -1);
		}
		position.set(zoom * viewportWidth / 2.0f, zoom * viewportHeight / 2.0f, 0);
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}
	
}
