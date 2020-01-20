
// Struct for passing variables
public class RenderableTriangle 
{
	public Material material;
	public Vertex[] vertices;
	public float[][] pixelData;
	public float[][] projections;
	
	public RenderableTriangle(Material m, Vertex[] v, float[][] data)
	{
		material = m;
		vertices = v;
		pixelData = data;
		projections = new float[v.length][];
	}
}
