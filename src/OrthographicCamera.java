/*
 * File: Camera.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This object is used to create a viewport in the environment
 */

public class OrthographicCamera extends Camera
{
	private Plane viewPlane;		// Plane that represents the view of the camera
	private float viewWidth;		// Width of view plane
	private float viewHeight;		// Height of view plane
	private float viewDepth;		// Depth of view plane from source
	
	public OrthographicCamera(float vW, float vH, float vD, int w, int h)
	{
		super(w, h);
		viewWidth = vW;
		viewHeight = vH;
		viewDepth = vD;
		setResolution(w, h);
	}
	
	public boolean isFaceVisible(Vertex[] vertices, float[][] pixelData)
	{
		return 	viewPlane.intersectionAlongPlane(vertices[0].pullProjection(), pixelData[0]) &&
				viewPlane.intersectionAlongPlane(vertices[1].pullProjection(), pixelData[1]) &&
				viewPlane.intersectionAlongPlane(vertices[2].pullProjection(), pixelData[2]);
	}
	
	// Returns intersection point of a line to the camera's view plane (Used in debug)
	public float[] getVertexPosition(Line pos)
	{
		float[] finalPos = new float[3];
		viewPlane.intersectionAlongPlane(pos, finalPos);
		return finalPos;
	}
	
	// Finalizes camera resources before rendering process begins
	public void finalize()
	{
		viewPlane = new Plane(new Vertex(viewDepth, -viewWidth/2, -viewHeight/2), new Vertex(viewDepth, viewWidth/2, -viewHeight/2), new Vertex(viewDepth, -viewWidth/2, viewHeight/2), width, height);
		buffer = new RenderBuffer(width, height);
	}
	
	// Updates the transformations of the view plane before rendering
	public void updateTransformation(final float[] ref, final float[] rot, final float[] scale) 
	{
		viewPlane.update(this);
	}
	
	public Plane getViewPlane() { return viewPlane; }
}