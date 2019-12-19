/*
 * File: Renderable.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Abstract class used by all objects rendered in environment
 */

public abstract class Renderable
{
	// 16-bit sine depth
	public static final float SIN_CONVERT = (float) (32668 / Math.PI);
	public static final float[] SINE = generateSineLookup();
	public static final float[] COSINE = generateCosineLookup();
	
	protected boolean visible = true; // sets if object will show during rendering
	
	// Processes object when rendering process begins
	public abstract void finalizeRender();
	
	// Updates transformation and renders object to camera
	public abstract void render(final float[] refTransform, Camera camera);
	
	// Toggles visibility of structure
	public void toggleVisibility() { visible = !visible;}
	
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
