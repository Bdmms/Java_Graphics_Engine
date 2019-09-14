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
		int textureWidth = mat.getWidth();
		
		float[] ref = tri.getReferencePoint();
		float[] svect = tri.getSVector();
		float[] tvect = tri.getTVector();
		float dep, iX, iY, iDep, iTx, iTy;
		int i, tx, ty, color;
		
		int w = mat.width - 1;
		int h = mat.height - 1;
		
		for(float s = tri.getMinS(); s < tri.getMaxS(); s += tri.sInc)
		{
			iX = ref[0] + svect[0]*s;
			iY = ref[1] + svect[1]*s;
			iDep = ref[2] + svect[2]*s;
			iTx = tri.getTextureStartX() + svect[3]*s;
			iTy = tri.getTextureStartY() + svect[4]*s;
			
			for(float t = tri.getMinT(s); t < tri.getMaxT(s); t += tri.tInc)
			{
				i = (int)(iX + tvect[0]*t) + (int)(iY + tvect[1]*t) * width;
				
				// If screen pixel reference is not within bounds
				if(i >= 0 && i < size)
				{
					dep = iDep + tvect[2]*t;
					
					// If depth of face is behind something
					if(depth[i] > dep)
					{
						tx = (int)((iTx + tvect[3]*t) * w) % mat.width;
						ty = (int)((iTy + tvect[4]*t) * h) % mat.height;
		
						if(tx < 0) tx = (mat.width + tx);
						if(ty < 0) ty = (mat.height + ty);
						
						color = texture[tx + ty * textureWidth];
						
						// If color is not transparent
						if((color & 0xFF000000) < 0)
						{
							frame[i] = color;
							depth[i] = dep;
						}
					}
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
}
