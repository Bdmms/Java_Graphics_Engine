/*
 * File: RenderTriangle.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Class used to prepare rendering process of triangles
 */

public class RenderTriangle 
{
	private static float[] HOR_VECTOR = {1,0};
	private static float[] VER_VECTOR = {0,1};
	private static float[] ORIGIN = {0,0};
	private static float[] W_ORIGIN = new float[2];
	private static float[] H_ORIGIN = new float[2];
	
	private RenderBuffer root;	// Reference to buffer
	private Face ref;			// Reference to face being rendered
	
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
	private float smin;		// Min bounds of s
	private float smax;		// Max bounds of s
	private float s_width;	// Width of s vector
	private float s_height;	// Height of s vector
	private float t_width;  // Width of t vector
	private float t_height;	// Height of t vector
	private float width;	// Boundary width of triangle
	private float height;	// Boundary height of triangle
	boolean remove;			// Flag used to destroy this object
	
	public RenderTriangle(RenderBuffer r)
	{
		root = r;
	}
	
	//Used to reset parameters without reinitializing the object
	public void reset(float[] p1, float[] p2, float[] p3, Face r)
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
		
		ref = r;
		refPoint = p1;
		textureX = ref.getVertex(0).texture[0];
		textureY = ref.getVertex(0).texture[1];
		
		sVector[3] = ref.getVertex(1).texture[0] - textureX;
		sVector[4] = ref.getVertex(1).texture[1] - textureY;
		tVector[3] = ref.getVertex(2).texture[0] - textureX;
		tVector[4] = ref.getVertex(2).texture[1] - textureY;
		
		//smin = 0;
		//smax = 1;
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
		s_width = Math.abs(sVector[0]) + 1;
		s_height = Math.abs(sVector[1]) + 1;
		t_width = Math.abs(tVector[0]) + 1;
		t_height = Math.abs(tVector[1]) + 1;
		sInc = s_width > s_height ? 1 / s_width : 1 / s_height;
		tInc = t_width > t_height ? 1 / t_width : 1 / t_height;
		if(sInc < tInc) tInc = sInc;
		else sInc = tInc;
		*/

		//TEMP UNTIL CLIPPING WORKS
		if(height > root.getHeight() / 2) remove = true;
		else if(width > root.getWidth() / 2) remove = true;
		else if(xmax < 0) remove = true;
		else if(xmin > root.getWidth()) remove = true;
		else if(ymax < 0) remove = true;
		else if(ymin > root.getHeight()) remove = true;
		else remove = false;
		
		/*
		W_ORIGIN[0] = root.getWidth();
		W_ORIGIN[1] = 0;
		H_ORIGIN[0] = 0;
		H_ORIGIN[1] = root.getHeight();
		numClips = 0;
		
		clipBounds(ORIGIN, HOR_VECTOR);
		clipBounds(ORIGIN, VER_VECTOR);
		clipBounds(H_ORIGIN, HOR_VECTOR);
		clipBounds(W_ORIGIN, VER_VECTOR);
		*/
	}
	
	// Clips the triangle based on the boundaries of the screen
	public void clipBounds(float[] boundSrc, float[] boundVect)
	{
		float[] itr_s = new float[4]; // |x|y|s|t|
		float[] itr_t = new float[4]; // |x|y|s|t|
		
		//System.out.println("-----");
		//System.out.print("Clip " + (numClips + 1));
		
		if(lineIntersection(refPoint, sVector, boundSrc, boundVect, itr_s))
		{
			if(lineIntersection(refPoint, tVector, boundSrc, boundVect, itr_t))
			{
				/*
				System.out.println(": S-T BOUNDARY");
				System.out.println("Format: |x|y|s|t|");
				System.out.print("S: ");
				Application.printFloatArr(itr_s);
				System.out.print("T: ");
				Application.printFloatArr(itr_t);
				System.out.print("U: ");
				Application.printFloatArr(itr_u);
				 */
				
				clipEq[numClips][0] = itr_s[2];
				clipEq[numClips][1] = 0;
				clipEq[numClips][2] = 0;
				clipEq[numClips][3] = itr_t[2];
				numClips++;
				return;
			}
			else
			{
				/*
				System.out.println(": S BOUNDARY");
				System.out.println("Format: |x|y|s|t|");
				System.out.print("S: ");
				Application.printFloatArr(itr_s);
				System.out.print("T: ");
				Application.printFloatArr(itr_t);
				System.out.print("U: ");
				Application.printFloatArr(itr_u);
				*/
				
				clipEq[numClips][0] = itr_s[2];	// S = s
				clipEq[numClips][1] = 0;		// T = 0
				clipEq[numClips][2] = 0;
				clipEq[numClips][3] = 0;
				numClips++;
				return;
			}
		}
		else if(lineIntersection(refPoint, tVector, boundSrc, boundVect, itr_t))
		{
			/*
			System.out.println(": T BOUNDARY");
			System.out.println("Format: |x|y|s|t|");
			System.out.print("S: ");
			Application.printFloatArr(itr_s);
			System.out.print("T: ");
			Application.printFloatArr(itr_t);
			System.out.print("U: ");
			Application.printFloatArr(itr_u);
			*/
			
			clipEq[numClips][0] = 0;		// S = 0
			clipEq[numClips][1] = itr_t[2]; // T = t
			clipEq[numClips][2] = 0;
			clipEq[numClips][3] = 0;
			numClips++;
			return;
		}
		
		remove = true;
		
		// Unit Test
		float[] src0 = {0, 0};
		float[] src1 = {0, 0};
		float[] test0 = {1, 0};
		float[] test1 = {0, 1};
		lineIntersection(src0, test0, src1, test1, itr_s);
		Application.printFloatArr(itr_s);
		System.exit(1);
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
	
	// Solves for s and t based on (x, y) position
	public boolean solveForST(int x, int y, float[] ST)
	{
		//x = x0 + v1x*s + v2x*t 
		//y = y0 + v1y*s + v2y*t 
		
		//s = x - x0 - v2x*t
		//t = y - y0 - v1y*s
		
		//s = x - x0 - v2x*(y - y0 - v1y*s)
		//s = (x - x0 - v2x*(y - y0))/(1 + v2x*v1y)
		
		float bound = 1 + tVector[1]*sVector[2];
		
		if(bound != 0)
		{
			ST[0] = x - refPoint[0] - tVector[0] * (y - refPoint[1]) / bound;
			ST[1] = y - refPoint[2] - sVector[2] * ST[0];
			return true;
		}
		else
			return false;
	}
	
	// Finds (x,y) position from s and t values
	public void findPoint(float s, float t, float[] p)
	{
		//x = x0 + v1x*s + v2x*t 
		//y = y0 + v1y*s + v2y*t 
		//z = z0 + v1z*s + v2z*t 
		
		p[0] = refPoint[0] + sVector[0]*s + tVector[0]*t;
		p[1] = refPoint[1] + sVector[1]*s + tVector[1]*t;
		p[2] = refPoint[2] + sVector[2]*s + tVector[2]*t;
		p[2] = textureX + sVector[3]*s + tVector[3]*t;
		p[3] = textureY + sVector[4]*s + tVector[4]*t;
	}
	
	// Returns minimum s
	public float getMinS(){return smin;}
	
	// Returns maximum s
	public float getMaxS(){return smax;}
	
	// Returns minimum t
	public float getMaxT(float s)
	{
		float lowestT1 = 1 - s;
		float lowestT2 = lowestT1;
		float lowestT3 = lowestT1;
		float tAtS = 0;
		
		System.out.println("---- Maximum T Value ----");
		System.out.println("Checking s = " + s);
		System.out.println("Possible Values:");
		System.out.println("Default: " + lowestT1);
		
		for(int i = 0; i < numClips; i++)
		{
			if(clipEq[i][0] > 0)
				tAtS = clipEq[i][3] - (clipEq[i][3] / clipEq[i][0] * s);
			else
				tAtS = clipEq[i][1];
			
			System.out.println("Clip " + i + ":" + tAtS);
			
			if(tAtS < lowestT1)
			{
				lowestT3 = lowestT2;
				lowestT2 = lowestT1;
				lowestT1 = tAtS;
			}
			else if(tAtS < lowestT2)
			{
				lowestT3 = lowestT2;
				lowestT2 = tAtS;
			}
			else if(tAtS < lowestT3)
				lowestT3 = tAtS;
		}
		
		if(lowestT3 < 0)
			return 1 - s;
		else
			return lowestT3;
	}
	
	// Returns maximum t
	public float getMinT(float s)
	{
		
		
		float highestT1 = 0;
		float highestT2 = 0;
		float highestT3 = 0;
		float tAtS = 0;
		
		System.out.println("---- Minimum T Value ----");
		System.out.println("Checking s = " + s);
		System.out.println("Possible Values:");
		System.out.println("Default: " + highestT1);
		
		for(int i = 0; i < numClips; i++)
		{
			if(clipEq[i][0] > 0)
				tAtS = clipEq[i][3] - (clipEq[i][3] / clipEq[i][0] * s);
			else
				tAtS = clipEq[i][1];
			
			System.out.println("Clip " + i + ": " + tAtS);
			
			if(tAtS > highestT1)
			{
				highestT3 = highestT2;
				highestT2 = highestT1;
				highestT1 = tAtS;
			}
			else if(tAtS > highestT2)
			{
				highestT3 = highestT2;
				highestT2 = tAtS;
			}
			else if(tAtS > highestT3)
				highestT3 = tAtS;
		}
		
		if(highestT3 > 1)
			return 0;
		else
			return highestT3;
	}
	
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
