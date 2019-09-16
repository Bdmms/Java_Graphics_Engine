import java.util.LinkedList;

/*
 * File: Vertex.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * This class stores the data of a 3D vertex
 */

public class Vertex
{
	public static final float UNDEFINED = Float.POSITIVE_INFINITY;
	public static final float[] ORIGIN = new float[3];
	public static final Vertex ZERO = new Vertex(ORIGIN);
	
	private static int numVertex = 0;			//Total number of vertices created
	
	private LinkedList<Face> faces = new LinkedList<Face>();// List of faces the vertex forms
	private Line projection = new Line(ORIGIN, ORIGIN);		// Calculated rendered projection (do not set)
	private float[] rotationalPosition = new float[3];		// Calculated rendered rotation position (do not set)
	private float[] finalPosition = new float[3];			// Calculated rendered final position (do not set)
	private int id;											// Unique id of vertex
	private boolean projected = false;						// Whether vertex has been projected (used to prevent double projection)
	
	float[] vertex = new float[3];	// Position vector
	float[] normal = new float[3];	// Normal vector
	float[] texture = new float[2]; // Texture vector
	
	public Vertex(float[] loc) 
	{
		vertex[0] = loc[0];
		vertex[1] = loc[1];
		vertex[2] = loc[2];
		id = numVertex;
		numVertex++;
	}
	
	public Vertex(float x, float y, float z) 
	{
		vertex[0] = x;
		vertex[1] = y;
		vertex[2] = z;
		id = numVertex;
		numVertex++;
	}
	
	// Calculates final position and projection for rendering purposes
	public void updateProjection(float[] ref, float[] refRot, float[] refScale)
	{
		if(projected) 
			return;
		
		float sinx = (float) Math.sin(refRot[0]);
		float cosx = (float) Math.cos(refRot[0]);
		float siny = (float) Math.sin(refRot[1]);
		float cosy = (float) Math.cos(refRot[1]);
		float sinz = (float) Math.sin(refRot[2]);
		float cosz = (float) Math.cos(refRot[2]);
		
		//-----POSITION-----
		
		//Rotation Along x-axis
		rotationalPosition[1] = vertex[1]*cosx - vertex[2]*sinx;
		rotationalPosition[2] = vertex[1]*sinx + vertex[2]*cosx;

		//Rotation Along y-axis
		rotationalPosition[0] = vertex[0]*cosy - rotationalPosition[2]*siny;
		rotationalPosition[2] = vertex[0]*siny + rotationalPosition[2]*cosy;
		
		//Rotation Along z-axis
		finalPosition[0] = (rotationalPosition[0]*cosz - rotationalPosition[1]*sinz)*refScale[0] + ref[0];
		finalPosition[1] = (rotationalPosition[0]*sinz + rotationalPosition[1]*cosz)*refScale[1] + ref[1];
		finalPosition[2] = (rotationalPosition[2])*refScale[2] + ref[2];
		
		projection.reset(finalPosition);
		projected = true;
	}
	
	// Adds texture coordinates to vector
	public void setTexture(float[] text)
	{
		texture = text;
	}
	
	// Sets the normal vector
	public void setNormal(float[] norm){normal = norm;}
	
	// Adds a connected face to the list
	public void addFace(Face f) {faces.add(f);}
	
	// Returns the depth of the rendered projection (use only after projection)
	public float getDepth() {return projection.mag();}
	
	// Returns id of vertex
	public int getID() {return id;}
	
	// Returns base face of vertex
	public Face getFirstFace() {return faces.getFirst();}
	
	// Returns projection and resets the projected flag
	public Line pullProjection() 
	{
		projected = false;
		return projection;
	}
	
	// Print function
	public void print()
	{
		System.out.println("Vertex: " + id);
		Application.printFloatArr(vertex);
		Application.printFloatArr(normal);
		Application.printFloatArr(texture);
	}
}
