/*
 * File: Structure.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Abstract class used by all physical components in environment
 */

public abstract class Structure extends Renderable
{
	private static int numStructures = 0;	// Number of total structures created
	
	String name;						// Name of structure
	float[] position = new float[3];	// Position vector
	float[] rotation = new float[3];	// Rotation vector
	float[] scale = {1,1,1};			// Scale vector
	
	protected Renderable[] finalizedList;		// Finalized list used to automate rendering process
	protected float[] finalPosition = {0,0,0};	// Calculated rendering position
	protected float[] finalRotation = {0,0,0};	// Calculated rendering rotation
	protected float[] finalScale = {1,1,1};		// Calculated rendering scale
	
	private float[] rotationalPosition = new float[3]; 	// Intermediate rendering position
	
	public Structure(String name)
	{
		this.name = name;
		numStructures++;
	}
	
	// Calculates position during rendering
	public void calculatePosition(float[] ref, float[] refRot, float[] refScale, float[] pos)
	{
		float sinx = (float) Math.sin(refRot[0]);
		float cosx = (float) Math.cos(refRot[0]);
		float siny = (float) Math.sin(refRot[1]);
		float cosy = (float) Math.cos(refRot[1]);
		float sinz = (float) Math.sin(refRot[2]);
		float cosz = (float) Math.cos(refRot[2]);
		
		//Rotation Along x-axis
		rotationalPosition[1] = pos[1]*cosx - pos[2]*sinx;
		rotationalPosition[2] = pos[1]*sinx + pos[2]*cosx;

		//Rotation Along y-axis
		rotationalPosition[0] = pos[0]*cosy - rotationalPosition[2]*siny;
		rotationalPosition[2] = pos[0]*siny + rotationalPosition[2]*cosy;
		
		//Rotation Along z-axis
		finalPosition[0] = (rotationalPosition[0]*cosz - rotationalPosition[1]*sinz)*refScale[0] + ref[0];
		finalPosition[1] = (rotationalPosition[0]*sinz + rotationalPosition[1]*cosz)*refScale[1] + ref[1];
		finalPosition[2] = (rotationalPosition[2])*refScale[2] + ref[2];
	}
	
	// Calculates all transformations during rendering
	public void calculatePosition(float[] ref, float[] refRot, float[] refScale)
	{
		calculatePosition(ref, refRot, refScale, position);
		finalScale[0] = scale[0] + refScale[0];
		finalScale[1] = scale[1] + refScale[1];
		finalScale[2] = scale[2] + refScale[2];
		finalRotation[0] = rotation[0] + refRot[0];
		finalRotation[1] = rotation[1] + refRot[1];
		finalRotation[2] = rotation[2] + refRot[2];
	}
	
	// Toggles visibility of structure
	public void toggleVisibility() { visible = !visible;}
	
	// Returns a renderable component of this object
	public Renderable getRenderable(int i){return finalizedList[i];}
	
	// Returns number of existing structures created
	public static int getNumStructures() {return numStructures;}
}
