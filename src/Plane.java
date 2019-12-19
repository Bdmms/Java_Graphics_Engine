/*
 * File: Plane.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * This class stores the data of a 3D plane
 */

public class Plane
{
	Vertex[] vertices = new Vertex[3];			// Reference of verticies which make up plane
	float[] intersectionPoint = new float[3];	// Intersection position vector
	float[] normal = new float[3];				// Normal vector
	float[] anchor;								// Anchor vector
	float[] vectorS = new float[3];				// S-comp vector
	float[] vectorT = new float[3];				// T-comp vector
	float[] vectorT_inv = new float[3];			// Reciprocal of t-comp vector
	float constant = 0;							// Plane constant
	int virtualWidth;
	int virtualHeight;
	
	// Intermediates
	float int_vector_t;
	float int_vector_numerator;
	float int_vector_anchor;
	float int_vector_inv_t_height;
	
	//Other
	float intersectionNumerator;	// Intermediate calculation
	
	public Plane(Vertex p1, Vertex p2, Vertex p3, int width, int height)
	{
		virtualWidth = width;
		virtualHeight = height;
		vertices[0] = p1;
		vertices[1] = p2;
		vertices[2] = p3;
		anchor = vertices[0].vertex;
	}
	
	public Plane(Face f)
	{
		vertices = f.getVertecies();
		anchor = vertices[0].vertex;
		virtualWidth = 1;
		virtualHeight = 1;
	}
	
	// Updates plane data based on camera position
	public void update(Camera cam) 
	{
		// Note: transform also contains rotation and scale
		intersectionNumerator = Line.dot(cam.transform, normal) + constant;
		normal = getNormal(vertices[0].vertex, vertices[1].vertex, vertices[2].vertex);
		constant = Line.dot(normal, vertices[0].vertex);
		
		for(byte a = 0; a < 3; a++)
		{
			vectorS[a] = vertices[1].vertex[a] - vertices[0].vertex[a];
			vectorT[a] = vertices[2].vertex[a] - vertices[0].vertex[a];
			vectorT_inv[a] = 1 / vectorT[a];
		}
		
		// Update constant intermediate values
		int_vector_t = vectorT[1] * vectorT_inv[2];
		int_vector_numerator = 1/(vectorS[1] - vectorT[1] * vectorT_inv[2] * vectorS[2]);
		int_vector_anchor = int_vector_t * anchor[2] - anchor[1];
		int_vector_inv_t_height = vectorT_inv[2] * virtualHeight;
	}
	
	// Calculates intersection of line to plane
	public boolean intersectionAlongPlane(Line line, float[] p2D)
	{
		//Parameters for the t value
		float parallel = line.dot(normal);
		
		if(parallel > 0) //If parallel -> line does not intersect
		{
			float tValue = intersectionNumerator / parallel;
			
			intersectionPoint[0] = line.point[0] + line.vector[0] * tValue; //X
			intersectionPoint[1] = line.point[1] + line.vector[1] * tValue; //Y
			intersectionPoint[2] = line.point[2] + line.vector[2] * tValue; //Z
			
			p2D[0] = (intersectionPoint[1] + int_vector_anchor - int_vector_t * intersectionPoint[2]) * int_vector_numerator;
			p2D[1] = (intersectionPoint[2] - anchor[2] - vectorS[2] * p2D[0]) * int_vector_inv_t_height;
			p2D[0] *= virtualWidth;
			p2D[2] = line.vector[0]*line.vector[0] + line.vector[1]*line.vector[1] + line.vector[2]*line.vector[2];
			
			return true;
		}

		return false;
	}
	
	// Solve for s and t from intersection point
	public void solveForST(float[] intersect, float[] p2D, int w, int h)
	{
		//x = x0 + v1x*s + v2x*t 
		//y = y0 + v1y*s + v2y*t 
		//z = z0 + v1z*s + v2z*t 
		
		//s = (x - x0 - v2x*t) / v1x
		//s = (y - y0 - v2y*t) / v1y
		//t = (z - z0 - v1z*s) / v2z
		
		//s = ((y - y0 + v2y/v2z*(z - z0) )/v1y) / (1 - v2y/v2z*v1z/v1y)
		
		p2D[0] = (intersect[1] - anchor[1] - int_vector_t * (intersect[2] - anchor[2])) * int_vector_numerator;
		p2D[1] = ((intersect[2] - anchor[2] - vectorS[2] * p2D[0]) * vectorT_inv[2]) * h;
		p2D[0] *= w;
		
		/*
		p2D[0] = (intersect[1] - anchor[1] - vectorT[1] * (intersect[2] - anchor[2]))/(1 + vectorT[1]*vectorS[2]);
		p2D[1] = (intersect[2] - anchor[2] - vectorS[2] * p2D[0]) * h;
		p2D[0] *= w;*/
	}
	
	// Checks if line intersects plane once
	public boolean doesLineIntersectPlane(Line line)
	{
		return line.dot(normal) != 0;
	}
	
	// Checks if point is on plane
	public boolean isPointOnPlane(float[] p)
	{
		return constant == Line.dot(normal, p);
	}
	
	// Print function
	public void print()
	{
		System.out.println("Plane: N/A");
		System.out.println(normal[0] + "x " + normal[1] + "y " + normal[2] + "z = " + constant);
	}
	
	// Returns normal from three points
	public static float[] getNormal(float[] p1, float[] p2, float[] p3)
	{
		float[] v1 = new float[3];
		float[] v2 = new float[3];
		float[] normal = new float[3];
		
		for(byte a = 0; a < 3; a++)
		{
			v1[a] = p2[a] - p1[a];
			v2[a] = p3[a] - p2[a];
		}
		
		normal[0] = v1[1]*v2[2] - v1[2]*v2[1];
		normal[1] = v1[2]*v2[0] - v1[0]*v2[2];
		normal[2] = v1[0]*v2[1] - v1[1]*v2[0];
		
		return normal;
	}
}
