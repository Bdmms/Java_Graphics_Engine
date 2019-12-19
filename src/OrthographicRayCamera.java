import java.awt.image.BufferedImage;

@Deprecated
public class OrthographicRayCamera extends Camera 
{
	float sigmaWidth;
	float deltaHeight;
	
	private Face[][] x_axis;
	private Face[][] y_axis;
	private int[] x_count;
	private int[] y_count;
	
	public OrthographicRayCamera(float angleW, float angleH, int w, int h)
	{
		super(w, h);
		sigmaWidth = angleW;
		deltaHeight = angleH;
		
		dummyProc();
	}
	
	private float dummyProc()
	{
		float parallel = 2 * 2 + 2 * 2 + 2 * 2;
		
		if(parallel > 0) //If parallel -> line does not intersect
		{
			float tValue = 2 / parallel;
			
			float x = 2 + 2 * 4; //X
			float y = 2 + 3 * 5; //Y
			float z = 2 + 4 * 6; //Z
			
			float s = (4243 + 234234 - 52354 * 2345) * x;
			float t= (54352 - 2354 - 2345 * 3452) * y;
			s *= z;
			return 1*s + 3*t + 5*tValue;
		}
		
		return 0;
	}
	
	private Face f = new Face(null, null);
	
	public boolean getIntersection(Vertex[] vertices, float[][] pixelData)
	{
		int xmin = (int)(Math.random() * width * 0.5);
		int xmax = (int)(Math.random() * width * 0.5 + width * 0.5);
		int ymin = (int)(Math.random() * height * 0.5);
		int ymax = (int)(Math.random() * height *0.5 + height * 0.5);
		
		for(; xmin < xmax; xmin++)
		{
			x_axis[xmin][x_count[xmin]] = f;
			x_count[xmin]++;
		}
		
		for(; ymin < ymax; ymin++)
		{
			y_axis[ymin][y_count[ymin]] = f;
			y_count[ymin]++;
		}
		
		return false;
	}
	
	// Renders the final image
	public BufferedImage generateRender()
	{
		return buffer.render();
	}
	
	public void finalizeRender()
	{
		buffer = new RenderBuffer(width, height);
		
		x_axis = new Face[width][Face.getNumFaces()];
		y_axis = new Face[height][Face.getNumFaces()];
		x_count = new int[width];
		y_count = new int[height];
	}
	
	public void render(final float[] refTransform, final Camera camera) 
	{
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				for(int i = 0; i < x_count[x]; i++)
					x_axis[x][i] = null;
				
				for(int i = 0; i < y_count[y]; i++)
					y_axis[y][i] = null;
				
				y_count[y] = 0;
			}
			
			x_count[x] = 0;
		}
	}
	
	public float[] getVertexPosition(Line pos)
	{
		return new float[3];
	}
}
