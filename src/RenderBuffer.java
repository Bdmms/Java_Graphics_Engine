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
	//private float[] tri_id;			
	//private float[] tri_s;			
	//private float[] tri_t;			
	private int width;				// Width of frame in pixels
	private int height;				// Height of frame in pixels
	private int size;				// Total size of frame
	
	private float[] origin = new float[5];
	private float[] x_corner = new float[5];
	private float[] y_corner = new float[5];
	private float[] x_vector = new float[5]; 
	private float[] y_vector = new float[5]; 
	
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
	public void fillTriangleXOR(RenderTriangle tri, Material mat)
	{
		if(tri.remove)
			return;
		
		int[] texture = mat.getTexture();
		boolean drawing;
		
		/*
		Face f = tri.ref;
		int[] tex_anchor = f.textureData;
		int[] s_tex_vect = f.textVect1;
		int[] t_tex_vect = f.textVect2;*/
		
		float dep, s, t, int_dep, int_s, int_t, int_tx, int_ty;
		float xmin = tri.getMinX();
		float xmax = tri.getMaxX();
		float ymin = tri.getMinY();
		float ymax = tri.getMaxY();
		float tri_width = tri.getWidth();
		float tri_height = tri.getHeight();
		
		int index, color, tx, ty;
		float ystart = ymin >= 0 ? 0 : -ymin;
		float xstart = xmin >= 0 ? 0 : -xmin;
		float xend = xmax >= width ? tri_width - xmax + width - 1: tri_width;
		float yend = ymax >= height ? tri_height - ymax + height - 1: tri_height;
		
		tri.solveForST(xmin, ymin, origin);
		tri.solveForST(xmax, ymin, x_corner);
		tri.solveForST(xmin, ymax, y_corner);
		
		x_vector[0] = (x_corner[0] - origin[0]) / tri_width;
		x_vector[1] = (x_corner[1] - origin[1]) / tri_width;
		x_vector[2] = (x_corner[2] - origin[2]) / tri_width;
		x_vector[3] = (x_corner[3] - origin[3]) / tri_width;
		x_vector[4] = (x_corner[4] - origin[4]) / tri_width;
		y_vector[0] = (y_corner[0] - origin[0]) / tri_height;
		y_vector[1] = (y_corner[1] - origin[1]) / tri_height;
		y_vector[2] = (y_corner[2] - origin[2]) / tri_height;
		y_vector[3] = (y_corner[3] - origin[3]) / tri_height;
		y_vector[4] = (y_corner[4] - origin[4]) / tri_height;
		
		for(float y = ystart; y < yend; y++)
		{
			int_s = origin[0] + y_vector[0]*y;
			int_t = origin[1] + y_vector[1]*y;
			int_dep = origin[2] + y_vector[2]*y;
			int_tx = origin[3] + y_vector[3]*y;
			int_ty = origin[4] + y_vector[4]*y;
			drawing = false;
			
			for(float x = xstart; x < xend; x++)
			{
				//index = (int)(xmin + x + (ymin + y) * width);
				index = (int)(xmin + x) + (int)(ymin + y) * width;
				
				if(index > size)
				{
					//TEMP
					System.out.println(x + xmin);
					System.out.println(y + ymin);
					System.out.println(x);
					System.out.println(y);
					System.out.println(index);
					break;
				}
				
				s = (int_s + x_vector[0]*x);
				t = (int_t + x_vector[1]*x);
				
				if(s >= 0.00f && t >= 0.00f && s + t <= 1.00f)
				{
					drawing = true;
					dep = int_dep + (x_vector[2]*x);
					
					if(depth[index] > dep)
					{
						tx = (int)((int_tx + x_vector[3]*x) % 1) * mat.width;
						ty = (int)((int_ty + x_vector[4]*x) % 1) * mat.height;
						
						if(tx < 0) tx = mat.width + tx;
						if(ty < 0) ty = mat.height + ty;
						
						color = texture[tx + ty * mat.width];
						
						if((color & 0xFF000000) < 0)
						{
							frame[index] = color;
							depth[index] = dep;
						}
					}
				}
				else if(drawing)
					break;
			}
		}
	}

	// Renders a triangle in the buffer
	public void fillTriangleSTV(RenderTriangle tri, Material mat)
	{
		// Return if triangle is invalid
		if(tri.remove)
			return;
		
		// Get data from the face
		Face f = tri.ref;
		int[] tex_anchor = f.textureData;
		int[] s_tex_vect = f.textVect1;
		int[] t_tex_vect = f.textVect2;
		
		// Get data from the texture
		int[] texture = mat.getTexture();
		BinarySize text_width = mat.bin_width;
		BinarySize text_height = mat.bin_height;
		int text_w_max = text_width.max;
		int text_h_max = text_height.max;
		int text_scan_bits = text_width.bits;
		
		// Get data from the render triangle
		float[] ref = tri.getReferencePoint();
		float[] svect = tri.getSVector();
		float[] tvect = tri.getTVector();
		int i, color;
		
		// Generate initial data
		float s_inc = tri.sInc;
		float x, y, tx, ty, dep, t_max;
		float i_x = ref[0];
		float i_y = ref[1];
		float i_x_s_inc = svect[0] * s_inc;
		float i_y_s_inc = svect[1] * s_inc;
		float i_x_t_inc = tvect[0] * s_inc;
		float i_y_t_inc = tvect[1] * s_inc;
		float i_dep = ref[2];
		float i_dep_s_inc = svect[2] * s_inc;
		float i_dep_t_inc = tvect[2] * s_inc;
		float i_tex_x = tex_anchor[0];
		float i_tex_y = tex_anchor[1];
		float i_tex_x_s_inc = s_tex_vect[0] * s_inc;
		float i_tex_y_s_inc = s_tex_vect[1] * s_inc;
		float i_tex_x_t_inc = t_tex_vect[0] * s_inc;
		float i_tex_y_t_inc = t_tex_vect[1] * s_inc;
		
		for(float s = 0; s < 1; s += s_inc)
		{
			// Increment along s axis
			i_x += i_x_s_inc;
			i_y += i_y_s_inc;
			i_dep += i_dep_s_inc;
			i_tex_x += i_tex_x_s_inc;
			i_tex_y += i_tex_y_s_inc;
			
			// Set the max t along the t axis
			t_max = 1 - s;
			
			// Set initial value for t axis
			x = i_x;
			y = i_y;
			dep = i_dep;
			tx = i_tex_x;
			ty = i_tex_y;
			
			for(float t = 0; t <= t_max; t += s_inc)
			{
				// Increment along t axis
				x += i_x_t_inc;
				y += i_y_t_inc;
				dep += i_dep_t_inc;
				tx += i_tex_x_t_inc;
				ty += i_tex_y_t_inc;
				
				// Calculate the index value of the buffer
				i = (int)(x) + (int)(y) * width;
				//i = (int)(i_x + tvect[0]*t) + (int)(y) * width;
				//i = (int)(x) + (int)(i_y + tvect[1]*t) * width;
				//i = (int)(i_x + tvect[0]*t) + (int)(i_y + tvect[1]*t) * width;
				
				// If screen pixel reference is within bounds and if depth of face is not behind anything
				if(i >= 0 && i < size && x >= 0 && x < width && depth[i] > dep)
				{
					// Color is found by texture coordinates
					color = texture[
					                ((int)(tx) & text_w_max) +
					                (((int)(ty) & text_h_max) << text_scan_bits)];
					
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
	
	public int getWidth() {return width;}
	public int getHeight() {return height;}
}
