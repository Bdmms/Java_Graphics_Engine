
public class Transformation 
{
	// 16-bit sine depth
	private static final float SIN_CONVERT = (float) (32668 / Math.PI);
	private static final float[] SINE = generateSineLookup();
	private static final float[] COSINE = generateCosineLookup();
	
	// Transformation matrix indecies
	public final static byte POS_X = 0;
	public final static byte POS_Y = 1;
	public final static byte POS_Z = 2;
	public final static byte ROT_X = 3;
	public final static byte ROT_Y = 4;
	public final static byte ROT_Z = 5;
	public final static byte SCA_X = 6;
	public final static byte SCA_Y = 7;
	public final static byte SCA_Z = 8;
	
	// Cached variables
	private float sinx;
	private float cosx;
	private float siny;
	private float cosy;
	private float sinz;
	private float cosz;
	private float[] transform;	   // Transformation matrix
	private float[] finalTransform; // Calculated rendering transformation matrix
	
	public Transformation(float[] trans, float[] fTrans)
	{
		transform = trans;
		finalTransform = fTrans;
	}
	
	public void setReference(float[] ref)
	{
		finalTransform = ref;
		int lookup_x = (int)(finalTransform[Structure.ROT_X] * SIN_CONVERT) & 0xFFFF;
		int lookup_y = (int)(finalTransform[Structure.ROT_Y] * SIN_CONVERT) & 0xFFFF;
		int lookup_z = (int)(finalTransform[Structure.ROT_Z] * SIN_CONVERT) & 0xFFFF;
		sinx = SINE[lookup_x];
		cosx = COSINE[lookup_x];
		siny = SINE[lookup_y];
		cosy = COSINE[lookup_y];
		sinz = SINE[lookup_z];
		cosz = COSINE[lookup_z];
	}
	
	public float[] propagatePosition(Transformation prev)
	{
		// Rotations
		float rot_y = transform[POS_Y]*prev.cosx - transform[POS_Z]*prev.sinx;
		float rot_z = transform[POS_Y]*prev.sinx + transform[POS_Z]*prev.cosx;
		float rot_x = transform[POS_X]*prev.cosy - rot_z*prev.siny;
		
		// Final position from rotations
		finalTransform[POS_X] = (rot_x*prev.cosz - rot_y*prev.sinz)*prev.finalTransform[SCA_X] + prev.finalTransform[POS_X];
		finalTransform[POS_Y] = (rot_x*prev.sinz + rot_y*prev.cosz)*prev.finalTransform[SCA_Y] + prev.finalTransform[POS_Y];
		finalTransform[POS_Z] = (transform[POS_X]*prev.siny + rot_z*prev.cosy)*prev.finalTransform[SCA_Z] + prev.finalTransform[POS_Z];
		
		return finalTransform;
	}
	
	public void propagateTransformation(Transformation prev)
	{
		// Rotations
		float rot_y = transform[POS_Y]*prev.cosx - transform[POS_Z]*prev.sinx;
		float rot_z = transform[POS_Y]*prev.sinx + transform[POS_Z]*prev.cosx;
		float rot_x = transform[POS_X]*prev.cosy - rot_z*prev.siny;
		
		// Final position from rotations
		finalTransform[POS_X] = (rot_x*prev.cosz - rot_y*prev.sinz)*prev.finalTransform[SCA_X] + prev.finalTransform[POS_X];
		finalTransform[POS_Y] = (rot_x*prev.sinz + rot_y*prev.cosz)*prev.finalTransform[SCA_Y] + prev.finalTransform[POS_Y];
		finalTransform[POS_Z] = (transform[POS_X]*prev.siny + rot_z*prev.cosy)*prev.finalTransform[SCA_Z] + prev.finalTransform[POS_Z];
		
		// Other transformations
		finalTransform[SCA_X] = transform[SCA_X] + prev.finalTransform[SCA_X];
		finalTransform[SCA_Y] = transform[SCA_Y] + prev.finalTransform[SCA_Y];
		finalTransform[SCA_Z] = transform[SCA_Z] + prev.finalTransform[SCA_Z];
		finalTransform[ROT_X] = transform[ROT_X] + prev.finalTransform[ROT_X];
		finalTransform[ROT_Y] = transform[ROT_Y] + prev.finalTransform[ROT_Y];
		finalTransform[ROT_Z] = transform[ROT_Z] + prev.finalTransform[ROT_Z];
		
		int lookup_x = (int)(finalTransform[Structure.ROT_X] * SIN_CONVERT) & 0xFFFF;
		int lookup_y = (int)(finalTransform[Structure.ROT_Y] * SIN_CONVERT) & 0xFFFF;
		int lookup_z = (int)(finalTransform[Structure.ROT_Z] * SIN_CONVERT) & 0xFFFF;
		sinx = SINE[lookup_x];
		cosx = COSINE[lookup_x];
		siny = SINE[lookup_y];
		cosy = COSINE[lookup_y];
		sinz = SINE[lookup_z];
		cosz = COSINE[lookup_z];
	}
	
	private static float[] generateSineLookup()
	{
		float[] sine = new float[65536];
		
		for(int i = 0; i < sine.length; i++)
			sine[i] = (float) Math.sin(((float)i / 32668) * Math.PI);
		
		return sine;
	}
	
	private static float[] generateCosineLookup()
	{
		float[] cosine = new float[65536];
		
		for(int i = 0; i < cosine.length; i++)
			cosine[i] = (float) Math.cos(((float)i / 32668) * Math.PI);
		
		return cosine;
	}
}
