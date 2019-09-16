import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/*
 * File: RenderBuffer.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * This is the buffer for the display
 */

public class RenderBuffer 
{
	private BufferedImage render;	// Buffered image of rendering frame
	private WritableRaster raster;	// Writable raster
	private DataBufferInt buffer;	// Data buffer which bridges data and render frame
	private ColorModel cm;			// Color model of data buffer
	private SampleModel sm;			// Sample model of data buffer
	private int[] frame;			// Data array of the frame
	private float[] depth;			// Depth buffer of the frame
	private int width;				// Width of frame in pixels
	private int height;				// Height of frame in pixels
	private int size;				// Total size of frame
	
	public RenderBuffer(int w, int h)
	{
		width = w;
		height = h;
		size = w * h;
		frame = new int[size];
		depth = new float[size];
		
		buffer = new DataBufferInt(frame, frame.length);
		cm = new DirectColorModel(24, 0xFF0000, 0x00FF00, 0x0000FF);
		sm = cm.createCompatibleSampleModel(w,h);
		raster = Raster.createWritableRaster(sm, buffer, null);
		render = new BufferedImage(cm, raster, false, null);
		
		refresh();
	}

	// Renders a triangle in the buffer
	public void drawTriangle(RenderTriangle tri, Material mat)
	{
		if(tri.remove)
			return;
		
		int[] texture = mat.getTexture();
		int textureWidth = mat.width;
		int textureHeight = mat.height;
		
		boolean start_drawing = false;
		float[] ref = tri.getReferencePoint();
		float[] svect = tri.getSVector();
		float[] tvect = tri.getTVector();
		float dep, i_x, i_y, i_depth, i_texture_x, i_texture_y, t_max;
		int x, i, tx, ty, color;
		
		for(float s = 0; s < 1; s += tri.sInc)
		{
			i_x = ref[0] + svect[0]*s;
			i_y = ref[1] + svect[1]*s;
			i_depth = ref[2] + svect[2]*s;
			i_texture_x = tri.getTextureStartX() + svect[3]*s;
			i_texture_y = tri.getTextureStartY() + svect[4]*s;
			
			t_max = 1 - s;
			//t_max = (width - i_x) / tvect[1];
			//if(t_max > 1- s)
			//	t_max = 1 - s;
			
			start_drawing = false;
			for(float t = 0; t < t_max; t += tri.sInc)
			{
				x = (int)(i_x + tvect[0]*t);
				i = x + (int)(i_y + tvect[1]*t) * width;
				
				// If screen pixel reference is not within bounds
				if(i >= 0 && i < size && x > 0 && x < width)
				{
					start_drawing = true;
					dep = i_depth + tvect[2]*t;
					
					// If depth of face is behind something
					if(depth[i] > dep)
					{
						tx = (int)(((i_texture_x + tvect[3]*t) % 1) * textureWidth);
						ty = (int)(((i_texture_y + tvect[4]*t) % 1) * textureHeight);
		
						if(tx < 0) tx = textureWidth + tx;
						if(ty < 0) ty = textureHeight + ty;
						
						color = texture[tx + ty * textureWidth];
						
						// If color is not transparent
						if((color & 0xFF000000) < 0)
						{
							frame[i] = color;
							depth[i] = dep;
						}
					}
				}
				else
				{
					if(start_drawing)
						break;
				}
			}
		}
	}
	
	// Returns the render image
	public BufferedImage render()
	{
		return render;
	}
	
	// Resets the buffer
	public void refresh()
	{
		for(int i = 0; i < size; i++) 
		{
			frame[i] = 0x000000;
			depth[i] = Float.POSITIVE_INFINITY;
		}
	}

	// Modifies x value to be in bounds
	public int convertXPosition(float x)
	{
		if(x < 0)
			return 0;
		else if(x >= width)
			return width - 1;
		else
			return (int) x;
	}
	
	// Modifies y value to be in bounds
	public int convertYPosition(float y)
	{
		if(y < 0)
			return 0;
		else if(y >= height)
			return height - 1;
		else
			return (int) y;
	}
	
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
	
	public void UnitTest1()
	{
		System.out.println("--- Unit Test 1 ---");
		
		RenderTriangle test = new RenderTriangle(this);
		float[] p1 = {width * 0.25f, -height / 3,0};
		float[] p2 = {width * 0.25f, height * 4 / 3,0};
		float[] p3 = {width * 1.25f, height / 2,0};
		Face f = new Face(null);
		f.addVertex(new Vertex(p1), new Vertex(p2), new Vertex(p3));
		test.reset(p1, p2, p3, f);
		test.print();
		
		if(test.remove == true)
			System.out.println("WARNING - Unit Test Rejected");
		
		System.out.println("Recommendation = " + test.getMinT(0.0f));
		System.out.println("Recommendation = " + test.getMaxT(0.0f));
		System.out.println("Recommendation = " + test.getMinT(0.5f));
		System.out.println("Recommendation = " + test.getMaxT(0.5f));
		System.out.println("Recommendation = " + test.getMinT(1.0f));
		System.out.println("Recommendation = " + test.getMaxT(1.0f));
	}
	
	public void UnitTest2()
	{
		System.out.println(" ");
		System.out.println("--- Unit Test 2 ---");
		
		RenderTriangle test = new RenderTriangle(this);
		float[] p1 = {width * 0.25f, height * 0.25f, 0};
		float[] p2 = {width * 0.25f, height * 0.75f, 0};
		float[] p3 = {width * 0.75f, height * 0.50f, 0};
		Face f = new Face(null);
		f.addVertex(new Vertex(p1), new Vertex(p2), new Vertex(p3));
		test.reset(p1, p2, p3, f);
		test.print();
		
		if(test.remove == true)
			System.out.println("WARNING - Unit Test Rejected");
		
		System.out.println("Recommendation = " + test.getMinT(0.0f));
		System.out.println("Recommendation = " + test.getMaxT(0.0f));
		System.out.println("Recommendation = " + test.getMinT(0.5f));
		System.out.println("Recommendation = " + test.getMaxT(0.5f));
		System.out.println("Recommendation = " + test.getMinT(1.0f));
		System.out.println("Recommendation = " + test.getMaxT(1.0f));
	}
	
	public void UnitTest3()
	{
		System.out.println(" ");
		System.out.println("--- Unit Test 3 ---");
		
		RenderTriangle test = new RenderTriangle(this);
		float[] p1 = {width * 0.25f, height * 1.25f, 0};
		float[] p2 = {width * 0.25f, height * 0.25f, 0};
		float[] p3 = {width * 0.75f, height * 0.50f, 0};
		Face f = new Face(null);
		f.addVertex(new Vertex(p1), new Vertex(p2), new Vertex(p3));
		test.reset(p1, p2, p3, f);
		test.print();
		
		if(test.remove == true)
			System.out.println("WARNING - Unit Test Rejected");
		
		System.out.println("Recommendation = " + test.getMinT(0.0f));
		System.out.println("Recommendation = " + test.getMaxT(0.0f));
		System.out.println("Recommendation = " + test.getMinT(0.5f));
		System.out.println("Recommendation = " + test.getMaxT(0.5f));
		System.out.println("Recommendation = " + test.getMinT(1.0f));
		System.out.println("Recommendation = " + test.getMaxT(1.0f));
	}
}
