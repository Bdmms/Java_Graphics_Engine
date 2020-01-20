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
	private static final byte LT = 5;
	
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
	
	// Resets the buffer
	public void refresh()
	{
		for(int i = 0; i < size; i++) 
		{
			frame[i] = 0x000000;
			depth[i] = Float.POSITIVE_INFINITY;
		}
	}
	
	// Returns the render image
	public BufferedImage render() { return render; }
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
	// Dynamic Lighting
	public void fillTrianglePPR_DL(float[] v1, float[] v2, float[] v3, Material mat)
	{
		// Texture
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
		float s_vector_y_comp = (v2[Y] - v1[Y]);
		float s_vector_d_comp = (v2[DEPTH] - v1[DEPTH]);
		float s_vector_tx_comp = (v2[TX] - v1[TX]);
		float s_vector_ty_comp = (v2[TY] - v1[TY]);
		float s_vector_lt_comp = (v2[LT] - v1[LT]);
		float t_vector_x_comp = (v3[X] - v1[X]);
		float t_vector_y_comp = (v3[Y] - v1[Y]);
		float t_vector_d_comp = (v3[DEPTH] - v1[DEPTH]);
		float t_vector_tx_comp = (v3[TX] - v1[TX]);
		float t_vector_ty_comp = (v3[TY] - v1[TY]);
		float t_vector_lt_comp = (v3[LT] - v1[LT]);
		
		// Constants for solving S & T
		float int_vector_denomonator = 1/((v2[X] - v1[X]) - t_vector_x_comp / t_vector_y_comp * s_vector_y_comp);
		float int_vector_t_ratio = (t_vector_x_comp / t_vector_y_comp);
		float int_vector_t_constant = (v1[0] - int_vector_t_ratio * v1[1]);
		float int_vector_inv_t = 1 / t_vector_y_comp;
		
		// Origin (0,0)
		float s_origin = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		float t_origin = (y_min[1] - v1[1] - s_vector_y_comp * s_origin) * int_vector_inv_t;
		
		// X-axis (1,0)
		float s_x_axis = (x_max[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;

		// Y-axis (0,1)
		float s_y_axis = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_max[1]) * int_vector_denomonator;

		// Calculate vectors
		float dxLeft, dxRight;
		
		float dx0 = (y_mid[0] - y_min[0]) / (y_mid[1] - y_min[1]);
		float dx1 = (y_max[0] - y_min[0]) / (y_max[1] - y_min[1]);
		float dx2 = (y_max[0] - y_mid[0]) / (y_max[1] - y_mid[1]);
		
		float dsx = (s_x_axis - s_origin) * inv_tri_width;
		float dtx = ((y_min[1] - v1[1] - s_vector_y_comp * s_x_axis) * int_vector_inv_t - t_origin) * inv_tri_width;
		float dsy = (s_y_axis - s_origin) * inv_tri_height;
		float dty = ((y_max[1] - v1[1] - s_vector_y_comp * s_y_axis) * int_vector_inv_t - t_origin) * inv_tri_height;
		
		// Figure out which edge vector is left or right
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
		float int_lt = v1[LT] + s_origin*s_vector_lt_comp + t_origin*t_vector_lt_comp;
		
		// Intermediate increments
		float add_d_x = dsx*s_vector_d_comp + dtx*t_vector_d_comp;
		float add_d_y = dsy*s_vector_d_comp + dty*t_vector_d_comp;
		float add_tx_x = dsx*s_vector_tx_comp + dtx*t_vector_tx_comp;
		float add_tx_y = dsy*s_vector_tx_comp + dty*t_vector_tx_comp;
		float add_ty_x = dsx*s_vector_ty_comp + dtx*t_vector_ty_comp;
		float add_ty_y = dsy*s_vector_ty_comp + dty*t_vector_ty_comp;
		float add_lt_x = dsx*s_vector_lt_comp + dtx*t_vector_lt_comp;
		float add_lt_y = dsy*s_vector_lt_comp + dty*t_vector_lt_comp;
		
		// Determine default (rectangular) boundaries of the triangle
		int y_start = (int)y_min[1];
		if(y_start < 0)
		{
			y_start = 0;
			int y_diff = (int)-y_min[1];
			int_d += add_d_y * y_diff;
			int_tx += add_tx_y * y_diff;
			int_ty += add_ty_y * y_diff;
			int_lt += add_lt_y * y_diff;
		}
		
		int switch_point = (int) y_mid[1];
		int y_end = y_max[1] >= height ? height - 1 : (int)y_max[1];
		
		int default_x_start = x_min[0] > 0 ? (int)x_min[0] : 0;
		int default_x_end = x_max[0] >= width ? width - 1 : (int)x_max[0];
		float left = y_min[0] + dxLeft*(y_start - y_min[1]);
		float right = y_min[0] + dxRight*(y_start - y_min[1]);
		
		int x_start, x_end, index, color;
		float dep, tx, ty, lt, x_diff;
		
		// Draw triangle
		for(int y = y_start, py = y_start*width; y < y_end; y++, py += width)
		{
			if(y == switch_point)
			{
				if(dx0 > dx1)
				{
					dxRight = dx2;
					right = y_mid[0] + dxRight*(y - y_mid[1]);
				}
				else
				{
					dxLeft = dx2;
					left = y_mid[0] + dxLeft*(y - y_mid[1]);
				}
			}
			
			// Determine left and right boundaries of the triangle
			x_start = left < default_x_start ? default_x_start : (int)left;
			x_end = py + (right < default_x_end ? (int)right : default_x_end);
			x_diff = x_start - x_min[0];
			dep = int_d + add_d_x * x_diff;
			tx = int_tx + add_tx_x * x_diff;
			ty = int_ty + add_ty_x * x_diff;
			lt = int_lt + add_lt_x * x_diff;
			
			for(index = py + x_start; index < x_end; index++)
			{
				if(depth[index] > dep)
				{
					color = texture[((int)(tx) & text_w_max) + (((int)(ty) & text_h_max) << text_scan_bits)];
					
					if((color & 0xFF000000) < 0)
					{
						int r = (int)(((color & 0xFF0000) >> 16) * lt);
						int g = (int)(((color & 0xFF00) >> 8) * lt);
						int b = (int)((color & 0xFF) * lt);
						frame[index] = (r << 16) | (g << 8) | b;
						
						//frame[index] = color;
						depth[index] = dep;
					}
				}
				
				// Increment along x axis
				dep += add_d_x;
				tx += add_tx_x;
				ty += add_ty_x;
				lt += add_lt_x;
			}
			
			// Increment along y axis
			int_d += add_d_y;
			int_tx += add_tx_y;
			int_ty += add_ty_y;
			int_lt += add_lt_y;
			left += dxLeft;
			right += dxRight;
		}
	}
	
	// ----- FLAT LIGHTING ------
	public void fillTrianglePPR_FL(float[] v1, float[] v2, float[] v3, Material mat, float brightness)
	{
		// Texture
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
		float int_vector_denomonator = 1/((v2[X] - v1[X]) - t_vector_x_comp / t_vector_y_comp * s_vector_y_comp);
		float int_vector_t_ratio = (t_vector_x_comp / t_vector_y_comp);
		float int_vector_t_constant = (v1[0] - int_vector_t_ratio * v1[1]);
		float int_vector_inv_t = 1 / t_vector_y_comp;
		
		// Origin (0,0)
		float s_origin = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		float t_origin = (y_min[1] - v1[1] - s_vector_y_comp * s_origin) * int_vector_inv_t;
		
		// X-axis (1,0)
		float s_x_axis = (x_max[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;

		// Y-axis (0,1)
		float s_y_axis = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_max[1]) * int_vector_denomonator;

		// Calculate vectors
		float dxLeft, dxRight;
		
		float dx0 = (y_mid[0] - y_min[0]) / (y_mid[1] - y_min[1]);
		float dx1 = (y_max[0] - y_min[0]) / (y_max[1] - y_min[1]);
		float dx2 = (y_max[0] - y_mid[0]) / (y_max[1] - y_mid[1]);
		
		float dsx = (s_x_axis - s_origin) * inv_tri_width;
		float dtx = ((y_min[1] - v1[1] - s_vector_y_comp * s_x_axis) * int_vector_inv_t - t_origin) * inv_tri_width;
		float dsy = (s_y_axis - s_origin) * inv_tri_height;
		float dty = ((y_max[1] - v1[1] - s_vector_y_comp * s_y_axis) * int_vector_inv_t - t_origin) * inv_tri_height;
		
		// Figure out which edge vector is left or right
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
		int y_start = (int)y_min[1];
		if(y_start < 0)
		{
			y_start = 0;
			int y_diff = (int)-y_min[1];
			int_d += add_d_y * y_diff;
			int_tx += add_tx_y * y_diff;
			int_ty += add_ty_y * y_diff;
		}
		
		int switch_point = (int) y_mid[1];
		int y_end = y_max[1] >= height ? height - 1 : (int)y_max[1];
		
		int default_x_start = x_min[0] > 0 ? (int)x_min[0] : 0;
		int default_x_end = x_max[0] >= width ? width - 1 : (int)x_max[0];
		float left = y_min[0] + dxLeft*(y_start - y_min[1]);
		float right = y_min[0] + dxRight*(y_start - y_min[1]);
		
		int x_start, x_end, index, color;
		float dep, tx, ty, x_diff;
		
		// Draw triangle
		for(int y = y_start, py = y_start*width; y < y_end; y++, py += width)
		{
			if(y == switch_point)
			{
				if(dx0 > dx1)
				{
					dxRight = dx2;
					right = y_mid[0] + dxRight*(y - y_mid[1]);
				}
				else
				{
					dxLeft = dx2;
					left = y_mid[0] + dxLeft*(y - y_mid[1]);
				}
			}
			
			// Determine left and right boundaries of the triangle
			x_start = left < default_x_start ? default_x_start : (int)left;
			x_end = py + (right < default_x_end ? (int)right : default_x_end);
			x_diff = x_start - x_min[0];
			dep = int_d + add_d_x * x_diff;
			tx = int_tx + add_tx_x * x_diff;
			ty = int_ty + add_ty_x * x_diff;
			
			for(index = py + x_start; index < x_end; index++)
			{
				//dep = v1[DEPTH] + s*s_vector_d_comp + t*t_vector_d_comp;
				//tx = (v1[TX] + s*s_vector_tx_comp + t*t_vector_tx_comp);
				//ty = (v1[TY] + s*s_vector_ty_comp + t*t_vector_ty_comp);
				
				if(depth[index] > dep)
				{
					color = texture[((int)(tx) & text_w_max) + (((int)(ty) & text_h_max) << text_scan_bits)];
					
					if((color & 0xFF000000) < 0)
					{
						int r = (int)(((color & 0xFF0000) >> 16) * brightness);
						int g = (int)(((color & 0xFF00) >> 8) * brightness);
						int b = (int)((color & 0xFF) * brightness);
						frame[index] = (r << 16) | (g << 8) | b;
						
						//frame[index] = color;
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
			left += dxLeft;
			right += dxRight;
		}
	}
	
	// ----- NO LIGHTING ------
	public void fillTrianglePPR_NL(float[] v1, float[] v2, float[] v3, Material mat)
	{
		// Texture
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
		float int_vector_denomonator = 1/((v2[X] - v1[X]) - t_vector_x_comp / t_vector_y_comp * s_vector_y_comp);
		float int_vector_t_ratio = (t_vector_x_comp / t_vector_y_comp);
		float int_vector_t_constant = (v1[0] - int_vector_t_ratio * v1[1]);
		float int_vector_inv_t = 1 / t_vector_y_comp;
		
		// Origin (0,0)
		float s_origin = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		float t_origin = (y_min[1] - v1[1] - s_vector_y_comp * s_origin) * int_vector_inv_t;
		
		// X-axis (1,0)
		float s_x_axis = (x_max[0] - int_vector_t_constant - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;

		// Y-axis (0,1)
		float s_y_axis = (x_min[0] - int_vector_t_constant - int_vector_t_ratio * y_max[1]) * int_vector_denomonator;

		// Calculate vectors
		float dxLeft, dxRight;
		
		float dx0 = (y_mid[0] - y_min[0]) / (y_mid[1] - y_min[1]);
		float dx1 = (y_max[0] - y_min[0]) / (y_max[1] - y_min[1]);
		float dx2 = (y_max[0] - y_mid[0]) / (y_max[1] - y_mid[1]);
		
		float dsx = (s_x_axis - s_origin) * inv_tri_width;
		float dtx = ((y_min[1] - v1[1] - s_vector_y_comp * s_x_axis) * int_vector_inv_t - t_origin) * inv_tri_width;
		float dsy = (s_y_axis - s_origin) * inv_tri_height;
		float dty = ((y_max[1] - v1[1] - s_vector_y_comp * s_y_axis) * int_vector_inv_t - t_origin) * inv_tri_height;
		
		// Figure out which edge vector is left or right
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
		int y_start = (int)y_min[1];
		if(y_start < 0)
		{
			y_start = 0;
			int y_diff = (int)-y_min[1];
			int_d += add_d_y * y_diff;
			int_tx += add_tx_y * y_diff;
			int_ty += add_ty_y * y_diff;
		}
		
		int switch_point = (int) y_mid[1];
		int y_end = y_max[1] >= height ? height - 1 : (int)y_max[1];
		
		int default_x_start = x_min[0] > 0 ? (int)x_min[0] : 0;
		int default_x_end = x_max[0] >= width ? width - 1 : (int)x_max[0];
		float left = y_min[0] + dxLeft*(y_start - y_min[1]);
		float right = y_min[0] + dxRight*(y_start - y_min[1]);
		
		int x_start, x_end, index, color;
		float dep, tx, ty, x_diff;
		
		// Draw triangle
		for(int y = y_start, py = y_start*width; y < y_end; y++, py += width)
		{
			if(y == switch_point)
			{
				if(dx0 > dx1)
				{
					dxRight = dx2;
					right = y_mid[0] + dxRight*(y - y_mid[1]);
				}
				else
				{
					dxLeft = dx2;
					left = y_mid[0] + dxLeft*(y - y_mid[1]);
				}
			}
			
			// Determine left and right boundaries of the triangle
			x_start = left < default_x_start ? default_x_start : (int)left;
			x_end = py + (right < default_x_end ? (int)right : default_x_end);
			x_diff = x_start - x_min[0];
			dep = int_d + add_d_x * x_diff;
			tx = int_tx + add_tx_x * x_diff;
			ty = int_ty + add_ty_x * x_diff;
			
			for(index = py + x_start; index < x_end; index++)
			{
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
			left += dxLeft;
			right += dxRight;
		}
	}
}
