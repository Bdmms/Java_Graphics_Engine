import java.awt.Graphics;
import java.awt.image.BufferedImage;

/*
 * File: Camera.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This object is used to create a viewport in the environment
 */

public abstract class Camera extends Structure
{
	public static final float PI = (float) Math.PI;
	public static final float PI2 = (float) (2*Math.PI);
	
	private static int numCameras = 0;	// Total number of created cameras
	
	protected RenderBuffer buffer;	// Buffer of the display
	protected int width;				// Width of display
	protected int height;				// Height of display
	protected int size;					// Size of the display array
	
	public Graphics graphics;
	
	public Camera(int w, int h)
	{
		super("Camera " + numCameras);
		setResolution(w, h);
		visible = false;
		numCameras++;
	}
	
	public abstract void render(RenderableTriangle tri);
	public abstract float[] getVertexPosition(Line pos);
	
	// Resizes the display resolution
	public void setResolution(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.size = width * height;
	}
	
	public void finalizeRender()
	{
		buffer = new RenderBuffer(width, height);
	}
	
	// Nullifies rendering process
	public void render(Transformation ref, final Camera camera) 
	{
		return;
	}
	
	// Projects the vertices present in the environment to the camera and renders the structures
	public void project(Renderable[] list)
	{
		buffer.refresh();
		
		// Default transformation cache
		transformation.setReference(transform);
		
		// Note: transformation is treated as negative
		for(int i = 0; i < list.length; i++)
			list[i].render(transformation, this);
	}
	
	// Renders the final image
	public BufferedImage generateRender()
	{
		return buffer.render();
	}
	
	// Renders the camera (does nothing)
	public void render(Camera camera) {}
	
	public RenderBuffer getBuffer() { return buffer; }
}
