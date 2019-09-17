/*
 * File: RenderTriangle.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Class used to prepare rendering process of triangles
 */

public class RenderTriangle 
{
	private RenderBuffer root;	// Reference to buffer

	private float[][] clipEq = new float[4][4]; // |s0|t0|s|t|
	private int numClips = 0;
	
	private float[] refPoint;				// Anchor point of rendering		
	private float[] sVector = new float[5]; // S-component vector: |x|y|depth|tx|ty|
	private float[] tVector = new float[5]; // T-component vector: |x|y|depth|tx|ty|
	float sInc;				// Increment extent of s
	float tInc;				// Increment extent of t
	private float textureX; // Texture vector x-comp
	private float textureY; // Texture vector y-comp
	private float xmin;		// Min bounds of x
	private float xmax;		// Max bounds of x
	private float ymin;		// Min bounds of y
	private float ymax;		// Max bounds of y
	private float width;	// Boundary width of triangle
	private float height;	// Boundary height of triangle
	boolean remove;			// Flag used to destroy this object
	
	public RenderTriangle(RenderBuffer r)
	{
		root = r;
	}
	
	//Used to reset parameters without reinitializing the object
	public void reset(float[] p1, float[] p2, float[] p3, Face face)
	{
		//Parameters for vector rasterization
		sVector[0] = p2[0] - p1[0];
		sVector[1] = p2[1] - p1[1];
		sVector[2] = p2[2] - p1[2];
		tVector[0] = p3[0] - p1[0];
		tVector[1] = p3[1] - p1[1];
		tVector[2] = p3[2] - p1[2];
		
		if((sVector[0] == 0 && sVector[1] == 0) || (tVector[0] == 0 && tVector[1] == 0))
		{
			remove = true;
			return;
		}
		
		//ref = r;
		refPoint = p1;
		textureX = face.getVertex(0).texture[0];
		textureY = face.getVertex(0).texture[1];
		sVector[3] = face.getVertex(1).texture[0] - textureX;
		sVector[4] = face.getVertex(1).texture[1] - textureY;
		tVector[3] = face.getVertex(2).texture[0] - textureX;
		tVector[4] = face.getVertex(2).texture[1] - textureY;
		
		xmin = xmax = refPoint[0];
		ymin = ymax = refPoint[1];
		
		if(p2[0] < xmin)
			xmin = p2[0];
		else if(p2[0] > xmax)
			xmax = p2[0];
		
		if(p2[1] < ymin)
			ymin = p2[1];
		else if(p2[1] > ymax)
			ymax = p2[1];
		
		if(p3[0] < xmin)
			xmin = p3[0];
		else if(p3[0] > xmax)
			xmax = p3[0];
		
		if(p3[1] < ymin)
			ymin = p3[1];
		else if(p3[1] > ymax)
			ymax = p3[1];
		
		width = xmax - xmin;
		height = ymax - ymin;
		
		// Safe Increment value
		sInc = width > height ? 1/(width) : 1/(height);
		
		/*
		float s_width = Math.abs(sVector[0]) + 1;
		float s_height = Math.abs(sVector[1]) + 1;
		float t_width = Math.abs(tVector[0]) + 1;
		float t_height = Math.abs(tVector[1]) + 1;
		sInc = s_width > s_height ? 1 / s_width : 1 / s_height;
		tInc = t_width > t_height ? 1 / t_width : 1 / t_height;
		if(sInc < tInc) tInc = sInc;
		else sInc = tInc;
		*/

		//TEMP UNTIL CLIPPING WORKS
		if(xmax < 0) remove = true;
		else if(xmin > root.getWidth()) remove = true;
		else if(ymax < 0) remove = true;
		else if(ymin > root.getHeight()) remove = true;
		else remove = false;
	}
	
	public boolean lineIntersection(float[] src0, float[] vect0, float[] src1, float[] vect1, float[] intersection)
	{
		if(vect0[0] / vect0[1] == vect1[0] / vect1[1])
			return false;
		
		if(vect1[1] == 0)
		{
			intersection[3] = (src0[0] - src1[0] + vect0[0] / vect0[1] * (src1[1] - src0[0])) / (vect1[0] - vect0[0] * vect1[1] / vect0[1]);
			intersection[2] = (src1[1] + vect1[1]*intersection[3] - src0[1]) / vect0[1];
		}
		else
		{
			// --- Math ---
			//a = x constant
			//b = y constant
			
			//a0 + x0*s = a1 + x1*t
			//b0 + y0*s = b1 + y1*t
			
			//s = (a1 + x1*t - a0) / x0
			//t = (a0 + x0*s - a1) / x1
			//t = (b0 + y0*s - b1) / y1
			
			//s = (a1 + x1/y1*(b0 + y0*s - b1) - a0) / x0
			//s - x1/x0*y0/y1*s = (a1 + x1/y1*(b0 - b1) - a0) / x0
			//s = (a1 - a0 + x1/y1*(b0 - b1)) / (x0 - x1*y0/y1)
			
			intersection[2] = (src1[0] - src0[0] + vect1[0] / vect1[1] * (src0[1] - src1[0])) / (vect0[0] - vect1[0] * vect0[1] / vect1[1]);
			intersection[3] = (src0[1] + vect0[1]*intersection[2] - src1[1]) / vect1[1];
		}
		
		intersection[0] = src0[0] + vect0[0]*intersection[2];
		intersection[1] = src0[1] + vect0[1]*intersection[2];
		
		return true;
	}

	public boolean isLocationWithinTriangle(float x, float y)
	{
		if(tVector[1] != 0)
		{
			float bound = (sVector[0] - tVector[0]/tVector[1]*sVector[1]);
			float s = (x - refPoint[0] - tVector[0] / tVector[1] * (y - refPoint[1])) / bound;
			float t = (y - refPoint[1] - sVector[1] * s) / tVector[1];

			return (s > 0 && t > 0 && s + t < 1);
		}
		else
			return false;
	}
	
	// Solves for s and t based on (x, y) position
	public void solveForST(float x, float y, float[] ST)
	{
		//x = x0 + v1x*s + v2x*t 
		//y = y0 + v1y*s + v2y*t 
		
		//s = (x - x0 - v2x*t) / v1x
		//t = (y - y0 - v1y*s) / v2y
		
		//s = (x - x0 - v2x*(y - y0 - v1y*s) / v2y) / v1x
		//s = (x - x0 - v2x/v2y*(y - y0)) / v1x + v2x/v2y*v1y/v1x*s
		//s * (1 - v2x/v2y*v1y/v1x) = (x - x0 - v2x/v2y*(y - y0)) / v1x 
		//s = (x - x0 - v2x/v2y*(y - y0)) / (v1x - v2x/v2y*v1y)
		
		ST[0] = (x - refPoint[0] - tVector[0] / tVector[1] * (y - refPoint[1])) / (sVector[0] - tVector[0]/tVector[1]*sVector[1]);
		ST[1] = (y - refPoint[1] - sVector[1] * ST[0]) / tVector[1];
		ST[2] = refPoint[2] + ST[0]*sVector[2] + ST[1]*tVector[2];
		ST[3] = textureX + ST[0]*sVector[3] + ST[1]*tVector[3];
		ST[4] = textureY + ST[0]*sVector[4] + ST[1]*tVector[4];
	}
	
	public float getMinX() {return xmin;}
	public float getMaxX() {return xmax;}
	public float getMinY() {return ymin;}
	public float getMaxY() {return ymax;}
	
	public float[] getReferencePoint() {return refPoint;}
	public float[] getSVector() {return sVector;}
	public float[] getTVector() {return tVector;}
	public float getTextureStartX() {return textureX;}
	public float getTextureStartY() {return textureY;}
	
	public void print()
	{
		System.out.println("RenderTriangle: N/A");
		for(int i = 0; i < numClips; i++)
		{
			System.out.print("Clip Eq. " + i + ": ");
			Application.printFloatArr(clipEq[i]);
		}
	}
}
