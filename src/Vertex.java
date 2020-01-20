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
	private float[] finalPosition = new float[3];			// Calculated rendered final position (do not set)
	private int id;											// Unique id of vertex

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
	public float[] updateProjection(TransformLookup lookup)
	{
		// Rotations
		float rot_y = vertex[1]*lookup.cosx - vertex[2]*lookup.sinx;
		float rot_z = vertex[1]*lookup.sinx + vertex[2]*lookup.cosx;
		float rot_x = vertex[0]*lookup.cosy - rot_z*lookup.siny;
		
		//Rotation Along z-axis
		finalPosition[0] = (rot_x*lookup.cosz - rot_y*lookup.sinz)*lookup.refTransform[Structure.SCA_X] + lookup.refTransform[Structure.POS_X];
		finalPosition[1] = (rot_x*lookup.sinz + rot_y*lookup.cosz)*lookup.refTransform[Structure.SCA_Y] + lookup.refTransform[Structure.POS_Y];
		finalPosition[2] = (vertex[0]*lookup.siny + rot_z*lookup.cosy)*lookup.refTransform[Structure.SCA_Z] + lookup.refTransform[Structure.POS_Z];
		
		return finalPosition;
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
	
	// Returns id of vertex
	public int getID() {return id;}
	
	// Returns base face of vertex
	public Face getFirstFace() {return faces.getFirst();}
	
	// Print function
	public void print()
	{
		System.out.println("Vertex: " + id);
		Application.printArray(vertex);
		Application.printArray(normal);
		Application.printArray(texture);
	}
}
