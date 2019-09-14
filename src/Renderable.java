/*
 * File: Renderable.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Abstract class used by all objects rendered in environment
 */

public abstract class Renderable 
{
	protected boolean visible = true; // sets if object will show during rendering
	
	// Processes object when rendering process begins
	public abstract void finalize();
	
	// Rendering transformation, which are updated per frame
	public abstract void updateTransformation(final float[] ref, final float[] rot, final float[] scale);
	
	// Renders object to camera
	public abstract void render(Camera camera);
}
