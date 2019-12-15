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
	public void updateProjection(float[] refTransform)
	{
		// TODO: Fix projection pull to eliminate duplicate vertices
		if(projected) 
			return;
		
		float sinx = (float) Math.sin(refTransform[Structure.ROT_X]);
		float cosx = (float) Math.cos(refTransform[Structure.ROT_X]);
		float siny = (float) Math.sin(refTransform[Structure.ROT_Y]);
		float cosy = (float) Math.cos(refTransform[Structure.ROT_Y]);
		float sinz = (float) Math.sin(refTransform[Structure.ROT_Z]);
		float cosz = (float) Math.cos(refTransform[Structure.ROT_Z]);
		
		//-----POSITION-----
		
		//Rotation Along x-axis
		float rot_y = vertex[1]*cosx - vertex[2]*sinx;
		float rot_z = vertex[1]*sinx + vertex[2]*cosx;

		//Rotation Along y-axis
		float rot_x = vertex[0]*cosy - rot_z*siny;
		rot_z = vertex[0]*siny + rot_z*cosy;
		
		//Rotation Along z-axis
		finalPosition[0] = (rot_x*cosz - rot_y*sinz)*refTransform[Structure.SCA_X] + refTransform[Structure.POS_X];
		finalPosition[1] = (rot_x*sinz + rot_y*cosz)*refTransform[Structure.SCA_Y] + refTransform[Structure.POS_Y];
		finalPosition[2] = rot_z*refTransform[Structure.SCA_Z] + refTransform[Structure.POS_Z];
		
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
