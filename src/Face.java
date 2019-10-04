/*
 * File: Face.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This class organizes the vertices into a face
 */

public class Face extends Renderable implements Runnable
{
	private static int numFace = 0;				// The total number of faces created
	
	private static float[][] pixelData = new float[3][3];	// Intermediate pixel data (do not set)
	
	private Thread renderExecution;
	private RenderBuffer currentBuffer;			// Reference to currently rendered buffer
	private RenderTriangle triangle;			// Currently rendered triangle (do not set)
	private BodyGroup root;						// The body group this face is part of
	private Vertex[] vertices;					// The three vertices that make up the face
	private float[] normal;						// The normal of the face
	private int[] face;							// The parameters of the face
	private int id;								// The face's id
	private byte numVertex = 0;					// The number of vertices added to the face
	
	public Face(BodyGroup r, int[] f)
	{
		root = r;
		id = numFace;
		face = f;
		numFace++;
		triangle = new RenderTriangle();
		vertices = new Vertex[3];
	}
	
	public Face(BodyGroup r)
	{
		this(r, new int[9]);
	}
	
	// Used to run rendering process on face
	public void run()
	{
		//Draw renderable triangle in the buffer
		currentBuffer.fillTriangleSTV(triangle, root.material);
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
		//Find where each projection vector intersects view plane
		if(!camera.isFaceVisible(vertices, pixelData)) return;

		//Reset the renderable triangle to the intersecting points
		triangle.reset(pixelData[0], pixelData[1], pixelData[2], this);
		
		// Render Triangle (Single Thread)
		camera.getBuffer().fillTriangleSTV(triangle, root.material);
		
		// Render Triangle (Multi-Thread)
		//currentBuffer = camera.getBuffer();
		//renderExecution.run();
	}
	
	// Finalizes the face before rendering, it checks to make sure face is valid
	public void finalize()
	{
		if(numVertex < 3)
			Application.throwError("FATAL ERROR - NOT ENOUGH VERTICES", this);
		else
			normal = Plane.getNormal(vertices[0].vertex, vertices[1].vertex, vertices[2].vertex);
		
		triangle = new RenderTriangle();
		renderExecution = new Thread(this);
		
		System.out.println("Preparing Face Thread: " + renderExecution.getName());
	}
	
	public Vertex[] getVertecies() {return vertices;}
	public Vertex getVertex(int i) {return vertices[i];}
	public float[] getNormal() {return normal;}
	public int[] getObjFace() {return face;}
	public int getID() {return id;}
	public Material getMaterial() {return root.material;}
}
