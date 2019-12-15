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
	
	public Camera(int w, int h)
	{
		super("Camera " + numCameras);
		setResolution(w, h);
		visible = false;
		numCameras++;
	}
	
	public abstract boolean isFaceVisible(Vertex[] vertices, float[][] pixelData);
	public abstract float[] getVertexPosition(Line pos);
	
	// Resizes the display resolution
	public void setResolution(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	// Nullifies rendering process
	public void render(final float[] refTransform, final Camera camera) 
	{
		
	}
	
	// Renders the camera (does nothing)
	public void render(Camera camera) {}
	
	public RenderBuffer getBuffer() { return buffer; }
}
