/*
 * File: Face.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This class organizes the vertices into a face
 */

public class Face extends Renderable
{
	private static int numFace = 0;				// The total number of faces created
	
	private BodyGroup root;						// The body group this face is part of
	private Vertex[] vertices = new Vertex[3];	// The three vertices that make up the face
	private int[] face;							// The parameters of the face
	private int id;								// The face's id
	private byte numVertex = 0;					// The number of vertices added to the face
	
	public Face(BodyGroup r, int[] f)
	{
		root = r;
		id = numFace;
		face = f;
		numFace++;
	}
	
	public Face(BodyGroup r)
	{
		this(r, new int[9]);
	}
	
	// Adds a vertex to the face
	public void addVertex(Vertex ... v)
	{
		for(int i = 0; i < v.length; i++)
		{
			if(numVertex > 2)
				Application.throwError("FATAL ERROR - TOO MANY VERTICES", this);
			
			vertices[numVertex] = v[i];
			vertices[numVertex].addFace(this);
			numVertex++;
		}
	}
	
	// Updates the transformations of the face before rendering
	public void updateTransformation(final float[] ref, final float[] rot, final float[] scale) 
	{
		vertices[0].updateProjection(ref, rot, scale);
		vertices[1].updateProjection(ref, rot, scale);
		vertices[2].updateProjection(ref, rot, scale);
	}

	// Renders the face to the display
	public void render(Camera camera) 
	{
		camera.renderFace(this);
	}
	
	// Finalizes the face before rendering, it checks to make sure face is valid
	public void finalize()
	{
		if(numVertex < 3)
			Application.throwError("FATAL ERROR - NOT ENOUGH VERTICES", this);
	}
	
	public Vertex[] getVertecies() {return vertices;}
	public Vertex getVertex(int i) {return vertices[i];}
	public int[] getObjFace() {return face;}
	public int getID() {return id;}
	public Material getMaterial() {return root.material;}
}
