import java.util.Iterator;
import java.util.LinkedList;

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
	
	public final static byte POS_X = 0;
	public final static byte POS_Y = 1;
	public final static byte POS_Z = 2;
	public final static byte ROT_X = 3;
	public final static byte ROT_Y = 4;
	public final static byte ROT_Z = 5;
	public final static byte SCA_X = 6;
	public final static byte SCA_Y = 7;
	public final static byte SCA_Z = 8;
	
	public String name;						// Name of structure
	public float[] transform = {0,0,0,0,0,0,1,1,1}; //Transformation matrix
	private float[] finalTransform = {0,0,0,0,0,0,1,1,1};	// Calculated rendering transformation matrix
	
	protected LinkedList<Renderable> children;	// The dynamic list of children stored within the structure
	protected Renderable[] finalizedList;		// Finalized list used to automate rendering process
	
	public Structure(String name)
	{
		children = new LinkedList<Renderable>();
		this.name = name;
		numStructures++;
	}
	
	// Adds a child to the structure
	public void addChild(Renderable child){children.add(child);}
		
	// Returns a specified child from the list
	public Renderable getChild(int index){return children.get(index);}
	
	// Default Finalization, Finalizes the object and its components before rendering
	public void finalizeRender()
	{
		Iterator<Renderable> iterator = children.iterator();
		finalizedList = new Renderable[children.size()];
		
		for(int i = 0; i < finalizedList.length; i++)
		{
			finalizedList[i] = iterator.next();
			finalizedList[i].finalizeRender();
			iterator.remove();
		}
	}
	
	// Default Rendering process, can be overwritten
	public void render(final float[] refTransform, Camera camera) 
	{
		calculateTransform(refTransform);
		
		if(visible)
			for(int i = 0; i < finalizedList.length; i++)
				finalizedList[i].render(finalTransform, camera);
	}
	
	// Calculates all transformations during rendering
	public void calculateTransform(float[] refTransform)
	{
		// OLD METHOD
		//float sinx = (float) Math.sin(refTransform[ROT_X]);
		//float cosx = (float) Math.cos(refTransform[ROT_X]);
		//float siny = (float) Math.sin(refTransform[ROT_Y]);
		//float cosy = (float) Math.cos(refTransform[ROT_Y]);
		//float sinz = (float) Math.sin(refTransform[ROT_Z]);
		//float cosz = (float) Math.cos(refTransform[ROT_Z]);
		
		// NEW METHOD
		int lookup_x = (int)(refTransform[Structure.ROT_X] * SIN_CONVERT) & 0xFFFF;
		int lookup_y = (int)(refTransform[Structure.ROT_Y] * SIN_CONVERT) & 0xFFFF;
		int lookup_z = (int)(refTransform[Structure.ROT_Z] * SIN_CONVERT) & 0xFFFF;
		float sinx = SINE[lookup_x];
		float cosx = COSINE[lookup_x];
		float siny = SINE[lookup_y];
		float cosy = COSINE[lookup_y];
		float sinz = SINE[lookup_z];
		float cosz = COSINE[lookup_z];
		
		// Rotation Along x-axis
		float rot_y = transform[POS_Y]*cosx - transform[POS_Z]*sinx;
		float rot_z = transform[POS_Y]*sinx + transform[POS_Z]*cosx;

		// Rotation Along y-axis
		float rot_x = transform[POS_X]*cosy - rot_z*siny;
		rot_z = transform[POS_X]*siny + rot_z*cosy;
		
		// Rotation Along z-axis
		finalTransform[POS_X] = (rot_x*cosz - rot_y*sinz)*refTransform[SCA_X] + refTransform[POS_X];
		finalTransform[POS_Y] = (rot_x*sinz + rot_y*cosz)*refTransform[SCA_Y] + refTransform[POS_Y];
		finalTransform[POS_Z] = rot_z*refTransform[SCA_Z] + refTransform[POS_Z];
		
		// Other transformations
		finalTransform[SCA_X] = transform[SCA_X] + refTransform[SCA_X];
		finalTransform[SCA_Y] = transform[SCA_Y] + refTransform[SCA_Y];
		finalTransform[SCA_Z] = transform[SCA_Z] + refTransform[SCA_Z];
		finalTransform[ROT_X] = transform[ROT_X] + refTransform[ROT_X];
		finalTransform[ROT_Y] = transform[ROT_Y] + refTransform[ROT_Y];
		finalTransform[ROT_Z] = transform[ROT_Z] + refTransform[ROT_Z];
	}
	
	// Returns a renderable component of this object
	public Renderable getRenderable(int i){return finalizedList[i];}
	
	// Returns number of existing structures created
	public static int getNumStructures() {return numStructures;}
	
	public int size() {return children.size();}
	public boolean isEmpty() {return children.isEmpty();}
}
