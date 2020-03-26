
// Struct for passing variables
public class RenderableTriangle 
{
	private static final byte X = 0;
	private static final byte Y = 1;
	private static final byte DEPTH = 2;
	private static final byte TX = 3;
	private static final byte TY = 4;
	private static final byte LT = 5;
	
	public Material material;
	public Vertex[] vertices;
	public float[][] pixelData;
	public float[][] projections;
	public float[] v1;
	public float[] v2;
	public float[] v3;
	
	private int[] texture;
	private short[] r_image;
	private short[] g_image;
	private short[] b_image;
	private float s_vector_tx_comp;
	private float s_vector_ty_comp;
	private float t_vector_tx_comp;
	private float t_vector_ty_comp;
	
	public RenderableTriangle(Material m, Vertex[] v, float[][] data)
	{
		material = m;
		vertices = v;
		pixelData = data;
		projections = new float[v.length][];
		v1 = pixelData[0];
		v2 = pixelData[1];
		v3 = pixelData[2];
	}
	
	public void update()
	{
		s_vector_tx_comp = (v2[TX] - v1[TX]);
		s_vector_ty_comp = (v2[TY] - v1[TY]);
		t_vector_tx_comp = (v3[TX] - v1[TX]);
		t_vector_ty_comp = (v3[TY] - v1[TY]);
		texture = material.getTexture();
		r_image = material.getRedChannel();
		g_image = material.getGreenChannel();
		b_image = material.getBlueChannel();
	}
	
	// Dynamic Lighting (simplified)
	public void render(RenderPackage packet)
	{
		// Order the vertices
		float[] x_min, x_max, y_min, y_max;
		
		// X-points
		if(v1[0] > v2[0])
		{
			if(v1[0] > v3[0])
			{
				x_max = v1;
				x_min = v2[0] > v3[0] ? v3 : v2;
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
				x_max = v2[0] < v3[0] ? v3 : v2;
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
				y_min = v2[1] > v3[1] ? v3 : v2;
			}
			else
			{
				y_max = v3;
				y_min = v2;
			}
		}
		else
		{
			if(v1[1] < v3[1])
			{
				y_min = v1;
				y_max = v2[1] < v3[1] ? v3 : v2;
			}
			else
			{
				y_max = v2;
				y_min = v3;
			}
		}
		
		// Constants for solving S & T
		float int_vector_denomonator = 1 / ((v2[X] - v1[X]) - (v3[X] - v1[X]) / (v3[Y] - v1[Y]) * (v2[Y] - v1[Y]));
		float int_vector_t_ratio = (v3[X] - v1[X]) / (v3[Y] - v1[Y]);
		
		// Origin (0,0)
		float s_origin = (x_min[0] - v1[0] + int_vector_t_ratio * v1[1] - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		float t_origin = (y_min[1] - v1[1] - (v2[Y] - v1[Y]) * s_origin) / (v3[Y] - v1[Y]);
		// X-axis (1,0)
		float s_x_axis = (x_max[0] - v1[0] + int_vector_t_ratio * v1[1] - int_vector_t_ratio * y_min[1]) * int_vector_denomonator;
		// Y-axis (0,1)
		float s_y_axis = (x_min[0] - v1[0] + int_vector_t_ratio * v1[1] - int_vector_t_ratio * y_max[1]) * int_vector_denomonator;

		float dsx = (s_x_axis - s_origin) / (x_max[0] - x_min[0]);
		float dtx = ((y_min[1] - v1[1] - (v2[Y] - v1[Y]) * s_x_axis) / (v3[Y] - v1[Y]) - t_origin) / (x_max[0] - x_min[0]);
		float dsy = (s_y_axis - s_origin) / (y_max[1] - y_min[1]);
		float dty = ((y_max[1] - v1[1] - (v2[Y] - v1[Y]) * s_y_axis) / (v3[Y] - v1[Y]) - t_origin) / (y_max[1] - y_min[1]);

		int xmin = x_min[0] < 0 ? 0 : (int) x_min[0];
		int ymin = y_min[1] < 0 ? 0 : (int) y_min[1];
		int xmax = x_max[0] >= packet.width ? packet.width - 1 : (int) x_max[0];
		int ymax = y_max[1] >= packet.height ? packet.height - 1 : (int) y_max[1];
		
		float s_y = s_origin + (y_min[1] < 0 ? -y_min[1] * dsy : 0);
		float t_y = t_origin + (y_min[1] < 0 ? -y_min[1] * dty : 0);
		
		// Draw triangle
		for(int y = ymin, pi = ymin * packet.width; y < ymax; y++, pi += packet.width, s_y += dsy, t_y += dty)
		{
			float s = s_y;
			float t = t_y;
					
			for(int x = xmin; x < xmax; x++, s += dsx, t += dtx)
			{
				if(s >= 0 && t >= 0 && s + t <= 1)
				{
					float d = v1[DEPTH] + (v2[DEPTH] - v1[DEPTH]) * s + (v3[DEPTH] - v1[DEPTH]) * t;
					int index = pi + x;
					
					//s = s_origin + dsy * (y - ymin) + dsx * (x - xmin);
					//t = t_origin + dty * (y - ymin) + dtx * (x - xmin);
					
					if(packet.depth[index] > d)
					{
						float lt = v1[LT] + (v2[LT] - v1[LT]) * s + (v3[LT] - v1[LT]) * t;
						int t_index = ((int)(v1[TX] + s_vector_tx_comp * s + t_vector_tx_comp * t) & material.bin_width.max) | 
								(((int)(v1[TY] + s_vector_ty_comp * s + t_vector_ty_comp * t) & material.bin_height.max) << material.bin_width.bits);
						
						if((texture[t_index] & 0xFF000000) < 0)
						{
							packet.frame[index] = 
									((int)(r_image[t_index] * lt) << 16) |
									((int)(g_image[t_index] * lt) << 8) | 
									 (int)(b_image[t_index] * lt);
							packet.depth[index] = d;
						}
					}
				}
			}
		}
	}
}
