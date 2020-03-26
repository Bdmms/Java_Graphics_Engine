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
	public abstract void finalizeRender();
	
	// Updates transformation and renders object to camera
	public abstract void render(RenderPackage packet);
	
	// Toggles visibility of structure
	public void toggleVisibility() { visible = !visible;}
}
