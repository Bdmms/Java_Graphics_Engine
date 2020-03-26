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
	
	private RenderableTriangle render_tri;
	private float[][] pixelData;				// Intermediate pixel data (do not set)
	private float[][] unit_proj;
	
	private BodyGroup root;						// The body group this face is part of
	private Vertex[] vertices;					// The three vertices that make up the face
	private float[] normal;						// The normal of the face
	private float[] unit_nrm;					// The unit vector of the normal
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
		vertices = new Vertex[3];
		unit_proj = new float[3][3];
		pixelData = new float[3][6];
		render_tri = new RenderableTriangle(root.material, vertices, pixelData);
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

	@Override
	// Renders the face to the display
	public void render() 
	{
		// Updates vertices
		render_tri.projections[0] = vertices[0].transformation.propagatePosition(binded.transform);
		render_tri.projections[1] = vertices[1].transformation.propagatePosition(binded.transform);
		render_tri.projections[2] = vertices[2].transformation.propagatePosition(binded.transform);
		
		Plane.setNormal(unit_nrm, vertices[0].vertex, vertices[1].vertex, vertices[2].vertex);
		Line.unit(unit_nrm, normal);
		Line.unit(unit_proj[0], render_tri.projections[0]);
		Line.unit(unit_proj[1], render_tri.projections[1]);
		Line.unit(unit_proj[2], render_tri.projections[2]);
		
		pixelData[0][5] = 0.5f - Line.dot(unit_proj[0], unit_nrm) * 0.5f;
		pixelData[1][5] = 0.5f - Line.dot(unit_proj[1], unit_nrm) * 0.5f;
		pixelData[2][5] = 0.5f - Line.dot(unit_proj[2], unit_nrm) * 0.5f;
		
		// Update material
		render_tri.material = root.material;
		binded.camera.render(render_tri);
	}
	
	@Override
	// Finalizes the face before rendering, it checks to make sure face is valid
	public void finalizeRender()
	{
		if(numVertex < 3)
			Application.throwError("FATAL ERROR - NOT ENOUGH VERTICES", this);
		
		normal = Plane.getNormal(vertices[0].vertex, vertices[1].vertex, vertices[2].vertex);
		unit_nrm = new float[3];
		
		// Set texture data
		for(int i = 0; i < 3; i++)
		{
			textureData[i*2] = (int)(vertices[i].texture[0] * root.material.getWidth());
			textureData[i*2 + 1] = (int)(vertices[i].texture[1] * root.material.getHeight());
		}
		
		textVect1[0] = textureData[2] - textureData[0];
		textVect1[1] = textureData[3] - textureData[1];
		textVect2[0] = textureData[4] - textureData[0];
		textVect2[1] = textureData[5] - textureData[1];
		
		pixelData[0][3] = textureData[0];
		pixelData[0][4] = textureData[1];
		pixelData[1][3] = textureData[2];
		pixelData[1][4] = textureData[3];
		pixelData[2][3] = textureData[4];
		pixelData[2][4] = textureData[5];
		
		render_tri.finalizeRender();
	}
	
	public Vertex[] getVertecies() {return vertices;}
	public Vertex getVertex(int i) {return vertices[i];}
	public float[] getNormal() {return normal;}
	public int[] getObjFace() {return face;}
	public int getID() {return id;}
	public Material getMaterial() {return root.material;}
	
	public static int getNumFaces() { return numFace; }
}
