/*
 * File: Camera.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This object is used to create a viewport in the environment
 */

public class Camera extends Structure
{
	public static final float PI = (float) Math.PI;
	public static final float PI2 = (float) (2*Math.PI);
	
	private static int numCameras = 0;	// Total number of created cameras
	
	private RenderBuffer buffer;	// Buffer of the display
	private Plane viewPlane;		// Plane that represents the view of the camera
	private int width;				// Width of display
	private int height;				// Height of display
	private float viewWidth;		// Width of view plane
	private float viewHeight;		// Height of view plane
	private float viewDepth;		// Depth of view plane from source
	private float[][] pixelData = new float[3][3];	// Intermediate pixel data (do not set)
	private RenderTriangle triangle;	// Currently rendered triangle (do not set)
	
	public Camera(float vW, float vH, float vD, int w, int h)
	{
		super("Camera " + numCameras);
		viewWidth = vW;
		viewHeight = vH;
		viewDepth = vD;
		setResolution(w, h);
		numCameras++;
	}
	
	// Resizes the display resolution
	public void setResolution(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	// Returns the display buffer
	public RenderBuffer getBuffer() {return buffer;}
	
	// Renders a face after its vertices have been projected
	public void renderFace(Face f)
	{
		//Find where each projection vector intersects view plane
		if(!viewPlane.intersectionAlongPlane(f.getVertex(0).getProjection(), pixelData[0], width, height)) return;
		if(!viewPlane.intersectionAlongPlane(f.getVertex(1).getProjection(), pixelData[1], width, height)) return;
		if(!viewPlane.intersectionAlongPlane(f.getVertex(2).getProjection(), pixelData[2], width, height)) return;
		
		//Reset the renderable triangle to the intersecting points
		triangle.reset(pixelData[0], pixelData[1], pixelData[2], f);
		
		//Draw renderable triangle in the buffer
		buffer.drawTriangle(triangle, f.getMaterial());
	}
	
	// Returns intersection point of a line to the camera's view plane
	public float[] getVertexPosition(Line pos)
	{
		float[] finalPos = new float[3];
		viewPlane.intersectionAlongPlane(pos, finalPos, width, height);
		return finalPos;
	}
	
	// Finalizes camera resources before rendering process begins
	public void finalize()
	{
		viewPlane = new Plane(new Vertex(viewDepth, -viewWidth/2, -viewHeight/2), new Vertex(viewDepth, viewWidth/2, -viewHeight/2), new Vertex(viewDepth, -viewWidth/2, viewHeight/2));
		buffer = new RenderBuffer(width, height);
		triangle = new RenderTriangle(buffer);
	}
	
	// Updates the transformations of the view plane before rendering
	public void updateTransformation(final float[] ref, final float[] rot, final float[] scale) 
	{
		viewPlane.update(this);
	}
	
	// Renders the camera (does nothing)
	public void render(Camera camera) {}
}
