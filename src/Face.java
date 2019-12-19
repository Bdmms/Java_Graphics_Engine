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
	
	private RenderTriangle triangle;			// Currently rendered triangle (do not set)
	private float[][] pixelData;				// Intermediate pixel data (do not set)
	private BodyGroup root;						// The body group this face is part of
	private Vertex[] vertices;					// The three vertices that make up the face
	private float[] normal;						// The normal of the face
	private int[] face;							// The parameters of the face
	private int id;								// The face's id
	private byte numVertex = 0;					// The number of vertices added to the face
	
	// Cached Data
	int[] textureData = new int[6];
	int[] textVect1 = new int[2];
	int[] textVect2 = new int[2];
	
	public Face(BodyGroup r, int[] f)
	{
		root = r;
		id = numFace;
		face = f;
		numFace++;
		triangle = new RenderTriangle(this);
		vertices = new Vertex[3];
		pixelData = new float[3][5];
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

	// Renders the face to the display
	public void render(final float[] refTransform, Camera camera) 
	{
		// Updates vertices
		vertices[0].updateProjection(refTransform);
		vertices[1].updateProjection(refTransform);
		vertices[2].updateProjection(refTransform);
		
		// Render Triangle (Single Thread)
		if(!camera.getIntersection(vertices, pixelData)) return;
		
		//triangle.reset(pixelData[0], pixelData[1], pixelData[2]);
		//camera.getBuffer().fillTriangleSTV(triangle, root.material);
		
		//PPR
		int w = root.material.getWidth();
		int h = root.material.getHeight();
		pixelData[0][3] = vertices[0].texture[0] * w;
		pixelData[0][4] = vertices[0].texture[1] * h;
		pixelData[1][3] = vertices[1].texture[0] * w;
		pixelData[1][4] = vertices[1].texture[1] * h;
		pixelData[2][3] = vertices[2].texture[0] * w;
		pixelData[2][4] = vertices[2].texture[1] * h;
		camera.getBuffer().fillTrianglePPR(pixelData[0], pixelData[1], pixelData[2], root.material);
	}
	
	// Finalizes the face before rendering, it checks to make sure face is valid
	public void finalizeRender()
	{
		if(numVertex < 3)
			Application.throwError("FATAL ERROR - NOT ENOUGH VERTICES", this);
		else
			normal = Plane.getNormal(vertices[0].vertex, vertices[1].vertex, vertices[2].vertex);
		
		// Set texture data
		for(int i = 0; i < 6; i++)
			textureData[i] = (int)(vertices[i/2].texture[i%2] * root.material.getWidth());
		
		textVect1[0] = textureData[2] - textureData[0];
		textVect1[1] = textureData[3] - textureData[1];
		textVect2[0] = textureData[4] - textureData[0];
		textVect2[1] = textureData[5] - textureData[1];
		
		triangle.update();
	}
	
	public Vertex[] getVertecies() {return vertices;}
	public Vertex getVertex(int i) {return vertices[i];}
	public float[] getNormal() {return normal;}
	public int[] getObjFace() {return face;}
	public int getID() {return id;}
	public Material getMaterial() {return root.material;}
	
	public static int getNumFaces() { return numFace; }
}
