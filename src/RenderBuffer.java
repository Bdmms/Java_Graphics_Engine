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
	
	private static final byte X = 0;
	private static final byte Y = 1;
	private static final byte DEPTH = 2;
	private static final byte TX = 3;
	private static final byte TY = 4;
	
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
	
	// Parallel Polygon Rasterization
	public void fillTrianglePPR(float[] v1, float[] v2, float[] v3, Material mat)
	{
		// Texture
		int index, color;
		int[] texture = mat.getTexture();
		int text_w_max = mat.bin_width.max;
		int text_h_max = mat.bin_height.max;
		int text_scan_bits = mat.bin_width.bits;
		
		// Order the vertices
		float[] x_min, x_max, y_min, y_mid, y_max;
		
		// X-points
		if(v1[0] > v2[0])
		{
			if(v1[0] > v3[0])
			{
				x_max = v1;
				
				if(v2[0] > v3[0])
					x_min = v3;
				else
					x_min = v2;
			}
			else
			{
				x_max = v3;
				x_min = v2;
			}
		}
		else
		{
			if(v1[0] < v3[0])
			{
				x_min = v1;
				
				if(v2[0] < v3[0])
					x_max = v3;
				else
					x_max = v2;
			}
			else
			{
				x_max = v2;
				x_min = v3;
			}
		}
		
		// Y-points
		if(v1[1] > v2[1])
		{
			if(v1[1] > v3[1])
			{
				y_max = v1;
				
				if(v2[1] > v3[1])
				{
					y_mid = v2;
					y_min = v3;
				}
				else
				{
					y_mid = v3;
					y_min = v2;
				}
			}
			else
			{
				y_max = v3;
				y_mid = v1;
				y_min = v2;
			}
		}
		else
		{
			if(v1[1] < v3[1])
			{
				y_min = v1;
				
				if(v2[1] < v3[1])
				{
					y_mid = v2;
					y_max = v3;
				}
				else
				{
					y_mid = v3;
					y_max = v2;
				}
			}
			else
			{
				y_max = v2;
				y_mid = v1;
				y_min = v3;
			}
		}
		
		float inv_tri_width = 1/(x_max[0] - x_min[0]);
		float inv_tri_height = 1/(y_max[1] - y_min[1]);
		
		// Solve for S and T vectors
		float s_vector_x_comp = (v2[X] - v1[X]);
		float s_vector_y_comp = (v2[Y] - v1[Y]);
		float s_vector_d_comp = (v2[DEPTH] - v1[DEPTH]);
		float s_vector_tx_comp = (v2[TX] - v1[TX]);
		float s_vector_ty_comp = (v2[TY] - v1[TY]);
		float t_vector_x_comp = (v3[X] - v1[X]);
		float t_vector_y_comp = (v3[Y] - v1[Y]);
		float t_vector_d_comp = (v3[DEPTH] - v1[DEPTH]);
		float t_vector_tx_comp = (v3[TX] - v1[TX]);
		float t_vector_ty_comp = (v3[TY] - v1[TY]);
		
		// Constants for solving S & T
		float int_vector_denomonator = 1/(s_vector_x_comp - t_vector_x_comp / t_vector_y_comp * s_vector_y_comp);
		float int_vector_t_ratio = (t_vector_x_comp / t_vector_y_comp);
		float int_vector_t_constant = (v1[0] - int_vector_t_ratio * v1[1]);
		float int_vector_inv_t = 1 / t_vector_y_comp;
		
		// Origin (0,0)
		float s_origin = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		float t_origin = (y_min[1] - v1[1] - s_vector_y_comp * s_origin) * int_vector_inv_t;
		
		// X-axis (1,0)
		float s_x_axis = (x_max[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		float t_x_axis = (y_min[1] - v1[1] - s_vector_y_comp * s_x_axis) * int_vector_inv_t;
		
		// Y-axis (0,1)
		float s_y_axis = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_max[1]) * int_vector_denomonator;
		float t_y_axis = (y_max[1] - v1[1] - s_vector_y_comp * s_y_axis) * int_vector_inv_t;
		
		
		// Calculate vectors
		float dxLeft, dxRight;
		
		float dx0 = (y_mid[0] - y_min[0]) / (y_mid[1] - y_min[1]);
		float dx1 = (y_max[0] - y_min[0]) / (y_max[1] - y_min[1]);
		float dx2 = (y_max[0] - y_mid[0]) / (y_max[1] - y_mid[1]);
		
		float dsx = (s_x_axis - s_origin) * inv_tri_width;
		float dtx = (t_x_axis - t_origin) * inv_tri_width;
		float dsy = (s_y_axis - s_origin) * inv_tri_height;
		float dty = (t_y_axis - t_origin) * inv_tri_height;
		
		if(dx0 > dx1)
		{
			dxRight = dx0;
			dxLeft = dx1;
		}
		else
		{
			dxLeft = dx0;
			dxRight = dx1; 
		}
		
		// Intermediate Values
		float int_d = v1[DEPTH] + s_origin*s_vector_d_comp + t_origin*t_vector_d_comp;
		float int_tx = v1[TX] + s_origin*s_vector_tx_comp + t_origin*t_vector_tx_comp;
		float int_ty = v1[TY] + s_origin*s_vector_ty_comp + t_origin*t_vector_ty_comp;
		
		// Intermediate increments
		float add_d_x = dsx*s_vector_d_comp + dtx*t_vector_d_comp;
		float add_d_y = dsy*s_vector_d_comp + dty*t_vector_d_comp;
		float add_tx_x = dsx*s_vector_tx_comp + dtx*t_vector_tx_comp;
		float add_tx_y = dsy*s_vector_tx_comp + dty*t_vector_tx_comp;
		float add_ty_x = dsx*s_vector_ty_comp + dtx*t_vector_ty_comp;
		float add_ty_y = dsy*s_vector_ty_comp + dty*t_vector_ty_comp;
		
		// Determine default (rectangular) boundaries of the triangle
		// TODO: FIX y_start < 0
		int y_start = y_min[1] < 0 ? 0 : (int)y_min[1];
		int switch_point = (int) y_mid[1];
		float y_end = y_max[1] >= height ? height - 1 : y_max[1];
		
		int x_start, x_end;
		float dep, tx, ty, x_diff;
		
		int default_x_start = x_min[0] > 0 ? (int)x_min[0] : 0;
		int default_x_end = x_max[0] >= width ? width - 1 : (int)x_max[0];
		
		float left_y_min_y = y_min[1];
		float right_y_min_y = y_min[1];
		
		float left = y_min[0] + dxLeft*(y_start - y_min[1]);
		float right = y_min[0] + dxRight*(y_start - y_min[1]);
		
		// Draw triangle
		for(int y = y_start, py = y_start*width; y < y_end; y++, py += width)
		{
			if(y == switch_point)
			{
				if(dx0 > dx1)
				{
					dxRight = dx2;
					right_y_min_y = y_mid[1];
					right = y_mid[0] + dxRight*(y - y_mid[1]);
				}
				else
				{
					dxLeft = dx2;
					left_y_min_y = y_mid[1];
					left = y_mid[0] + dxLeft*(y - y_mid[1]);
				}
			}
			
			// Determine left and right boundaries of the triangle
			left += dxLeft;
			right += dxRight;
			
			x_start = default_x_start;
			dep = int_d;
			tx = int_tx;
			ty = int_ty;
			
			if(left >= default_x_start)
			{
				x_start = (int) left;
				x_diff = x_start - x_min[0];
				dep += add_d_x * x_diff;
				tx += add_tx_x * x_diff;
				ty += add_ty_x * x_diff;
			}
			
			x_end = right < default_x_end ? (int)right : default_x_end;
			
			for(int x = x_start; x < x_end; x++)
			{
				//dep = v1[DEPTH] + s*s_vector_d_comp + t*t_vector_d_comp;
				//tx = (v1[TX] + s*s_vector_tx_comp + t*t_vector_tx_comp);
				//ty = (v1[TY] + s*s_vector_ty_comp + t*t_vector_ty_comp);
				
				index = x + py;
				
				if(depth[index] > dep)
				{
					color = texture[((int)(tx) & text_w_max) + (((int)(ty) & text_h_max) << text_scan_bits)];
					
					if((color & 0xFF000000) < 0)
					{
						frame[index] = color;
						depth[index] = dep;
					}
				}
				
				// Increment along x axis
				dep += add_d_x;
				tx += add_tx_x;
				ty += add_ty_x;
			}
			
			// Increment along y axis
			int_d += add_d_y;
			int_tx += add_tx_y;
			int_ty += add_ty_y;
		}
	}

	// ST-component Texture Vectoring
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
		float x, y, tx, ty, dep, t_max, t_min;
		float i_x = ref[0];
		float i_y = ref[1];
		float i_dep = ref[2];
		float i_tex_x = tex_anchor[0];
		float i_tex_y = tex_anchor[1];
		
		final float s_inc = tri.sInc;
		final float i_x_s_inc = svect[0] * s_inc;
		final float i_x_t_inc = tvect[0] * s_inc;
		final float i_y_s_inc = svect[1] * s_inc;
		final float i_y_t_inc = tvect[1] * s_inc;
		final float i_dep_s_inc = svect[2] * s_inc;
		final float i_dep_t_inc = tvect[2] * s_inc;
		final float i_tex_x_s_inc = s_tex_vect[0] * s_inc;
		final float i_tex_y_s_inc = s_tex_vect[1] * s_inc;
		final float i_tex_x_t_inc = t_tex_vect[0] * s_inc;
		final float i_tex_y_t_inc = t_tex_vect[1] * s_inc;
		
		final float inv_i_x_t_inc = 1 / i_x_t_inc;

		/*
		final float inv_i_y_t_inc = 1 / i_y_t_inc;
		final float inv_i_x_s_inc = 1 / i_x_s_inc;
		final float inv_i_y_s_inc = 1 / i_y_s_inc;
		
		float min_t_min_s, max_t_min_s, min_t_max_s, max_t_max_s;
		
		//x = c + 0*mx + t*nx = 0; (t at min s, x = 0)
		//-c/nx = t
		float t_mins_minx = -i_x * inv_i_x_t_inc;	
		
		//x = c + 1*mx + t*nx = 0; (t at max s, x = 0)
		//(-c - mx)/nx = t
		float t_maxs_minx = (-i_x - i_x_s_inc) * inv_i_x_t_inc;	
		
		//y = c + 0*my + t*ny = 0; (s at min s, y = 0)
		//-c/ny = t	
		float t_mins_miny = -i_y * inv_i_y_t_inc;	
		
		//y = c + 1*my + t*ny = 0; (s at max s, y = 0)
		//(-c - my)/ny = t
		float t_maxs_miny = (-i_y - i_y_s_inc) * inv_i_y_t_inc;	

		// S-points
		float s_mint_minx = -i_x * inv_i_x_s_inc;	
		float s_mint_miny = -i_y * inv_i_y_s_inc;	
		if(s_mint_minx < s_mint_miny)
		{
			min_t_min_s = s_mint_minx;
			min_t_max_s = s_mint_miny;
		}
		else
		{
			min_t_min_s = s_mint_miny;
			min_t_max_s = s_mint_minx;
		}
		
		float s_maxt_minx = (-i_x - i_x_t_inc) * inv_i_x_s_inc;	
		float s_maxt_miny = (-i_y - i_y_t_inc) * inv_i_y_s_inc;	*/

		for(float s = 0; s < 1; s += s_inc)
		{
			// Set the max t along the t axis
			t_min = -i_x * inv_i_x_t_inc;		//m*t + c = 0	
			t_max = 1 - s;
			
			// Set initial value for t axis
			x = i_x;
			y = i_y;
			dep = i_dep;
			tx = i_tex_x;
			ty = i_tex_y;
			
			for(float t = t_min < 0 || t_min > t_max ? 0 : t_min; t < t_max; t += s_inc)
			{
				// Calculate the index value of the buffer
				i = (int)(x) + (int)(y) * width;
				
				// If screen pixel reference is within bounds and if depth of face is not behind anything
				if(i >= 0 && i < size && x >= 0 && x < width && depth[i] > dep)
				{
					// Color is found by texture coordinates
					color = texture[((int)(tx) & text_w_max) + (((int)(ty) & text_h_max) << text_scan_bits)];
					
					// If color is not transparent
					if((color & 0xFF000000) < 0)
					{
						frame[i] = color;
						depth[i] = dep;
					}
				}
				
				// Increment along t axis
				x += i_x_t_inc;
				y += i_y_t_inc;
				dep += i_dep_t_inc;
				tx += i_tex_x_t_inc;
				ty += i_tex_y_t_inc;
			}
			
			// Increment along s axis
			i_x += i_x_s_inc;
			i_y += i_y_s_inc;
			i_dep += i_dep_s_inc;
			i_tex_x += i_tex_x_s_inc;
			i_tex_y += i_tex_y_s_inc;
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
