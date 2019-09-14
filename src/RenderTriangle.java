/*
 * File: RenderTriangle.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Class used to prepare rendering process of triangles
 */

public class RenderTriangle 
{
	public enum Orientation {TOP_L, TOP_R, BOT_L, BOT_R}	// TODO: Orientation of clipping
	
	public static final float[] HORIZONTAL = {1, 0};	// Horizontal 2D unit vector
	public static final float[] VERTICAL = {0, 1};		// Vertical 2D unit vector
	
	private RenderBuffer root;	// Reference to buffer
	private Face ref;			// Reference to face being rendered
	private Orientation orientation; // Clipping Orientation
	
	private float[][] boundVector = new float[4][4]; // TODO: Boundary clipping vector
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
		
		smin = 0;
		smax = 1;
		xmin = xmax = refPoint[0];
		ymin = ymax = refPoint[1];
		
		if(refPoint[0] < root.getWidth()/2)
			if(refPoint[1] < root.getHeight()/2)
				orientation = Orientation.BOT_L;
			else
				orientation = Orientation.BOT_R;
		else
			if(refPoint[1] < root.getHeight()/2)
				orientation = Orientation.TOP_L;
			else
				orientation = Orientation.TOP_R;
		
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
		sInc = tInc = width > height ? tInc = 1/width : 1/height;
		
		clipBounds(0, 0, HORIZONTAL, 0);
		clipBounds(0, 0, VERTICAL, 1);
		clipBounds(0, root.getHeight(), HORIZONTAL, 2);
		clipBounds(root.getWidth(), 0, VERTICAL, 3);
		
		if(height > 400) remove = true;
		else if(xmax < 0) remove = true;
		else if(xmin > root.getWidth()) remove = true;
		else if(ymax < 0) remove = true;
		else if(ymin > root.getHeight()) remove = true;
		else remove = false;
	}
	
	// Clips the triangle based on the boundaries of the screen
	public void clipBounds(int x, int y, float[] vect, int bound)
	{
		float s, t;
		int target = 0;

		//Order: TOP, LEFT, BOTTOM, RIGHT
		switch(orientation)
		{
		case TOP_L: target = bound; break;
		case TOP_R: target = (bound + 1) % 4; break;
		case BOT_R: target = (bound + 2) % 4; break;
		case BOT_L: target = (bound + 3) % 4; break;
		default: Application.throwError("ERROR - UNKNOWN TRIANGLE ORIENTATION", this);
		}
		
		//(s, 0)
		if(sVector[1] == 0)
		{
			s = (x + vect[0] - refPoint[0])/sVector[0]; //(s, ?)
			boundVector[target][0] = s;
		}
		else
		{
			s = (y + vect[1] - refPoint[1])/sVector[1]; //(?, s)
			boundVector[target][1] = s;
		}
		
		//(0, t)
		if(tVector[1] != 0)
		{
			t = (x + vect[0] - refPoint[0])/tVector[0]; //(t, ?)
			boundVector[target][0] = t;
		}
		else
		{
			t = (y + vect[1] - refPoint[1])/tVector[1]; //(?, t)
			boundVector[target][1] = t;
		}
		
		if(s < 0) s = 0;
		if(s > 1) s = 1;
		if(s < smin) smin = s;
		if(s > smax) smax = s;
		
		boundVector[target][2] = -s;
		boundVector[target][3] = t;
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
		//float a = boundVector[2][1] + boundVector[2][3]*s;
		//float b = boundVector[3][1] + boundVector[3][3]*s;
		return 1 - s;
	}
	
	// Returns maximum t
	public float getMinT(float s)
	{
		//float a = boundVector[0][1] + boundVector[0][3]*s;
		//float b = boundVector[1][1] + boundVector[1][3]*s;
		return 0;
	}
	
	public float[] getReferencePoint() {return refPoint;}
	public float[] getSVector() {return sVector;}
	public float[] getTVector() {return tVector;}
	public float getTextureStartX() {return textureX;}
	public float getTextureStartY() {return textureY;}
}
