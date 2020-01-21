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
	private int id;											// Unique id of vertex

	public Transformation transformation;
	public float[] vertex = new float[3];	// Position vector
	public float[] normal = new float[3];	// Normal vector
	public float[] texture = new float[2];  // Texture vector
	
	public Vertex(float ... loc) 
	{
		vertex[0] = loc[0];
		vertex[1] = loc[1];
		vertex[2] = loc[2];
		id = numVertex;
		numVertex++;
		transformation = new Transformation(vertex, new float[3]);
	}
	
	public void setTexture(float[] text){ texture = text; }
	public void setNormal(float[] norm){normal = norm;}
	
	// Adds a connected face to the list
	public void addFace(Face f) {faces.add(f);}
	
	// Print function
	public void print()
	{
		System.out.println("Vertex: " + id);
		Application.printArray(vertex);
		Application.printArray(normal);
		Application.printArray(texture);
	}
}
