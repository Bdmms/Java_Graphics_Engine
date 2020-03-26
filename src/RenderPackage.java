
public class RenderPackage 
{
	Camera camera = null;
	Transformation transform = null;
	final float[] depth;
	final int[] frame;
	final int width;
	final int height;
	
	public RenderPackage(int[] frm, float[] dp, int w, int h)
	{
		depth = dp;
		frame = frm;
		width = w;
		height = h;
	}
}
