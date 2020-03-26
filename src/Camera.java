import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

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
	
	private RenderPackage packet;
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
	
	@Override
	public void finalizeRender()
	{
		buffer = new RenderBuffer(width, height);
		packet = buffer.getPackage();
		packet.camera = this;
	}
	
	@Override
	// Nullifies rendering process
	public void render() 
	{
		return;
	}
	
	// Projects the vertices present in the environment to the camera and renders the structures
	public void project(List<Structure> list)
	{
		buffer.refresh();
		
		binded = packet; // bind package
		
		// Note: transformation is treated as negative
		for(Renderable r : list)
		{
			transformation.setReference(transform);
			binded.transform = transformation;
			r.render();
		}
	}
	
	// Renders the final image
	public BufferedImage generateRender()
	{
		return buffer.render();
	}
	
	public RenderBuffer getBuffer() { return buffer; }
}
