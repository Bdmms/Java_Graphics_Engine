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
	
	protected Transformation transformation;
	protected LinkedList<Renderable> children;	// The dynamic list of children stored within the structure
	
	public Structure(String name)
	{
		float[] temp = {0,0,0,0,0,0,1,1,1};
		transformation = new Transformation(transform, temp);
		children = new LinkedList<Renderable>();
		this.name = name;
		numStructures++;
	}
	
	// Adds a child to the structure
	public void addChild(Renderable child){children.add(child);}
		
	// Returns a specified child from the list
	public Renderable getChild(int index){return children.get(index);}
	
	@Override
	// Default Finalization, Finalizes the object and its components before rendering
	public void finalizeRender()
	{
		for(Renderable r : children)
			r.finalizeRender();
	}
	
	@Override
	// Default Rendering process, can be overwritten
	public void render() 
	{
		transformation.propagateTransformation(binded.transform);
		
		if(visible)
			for(Renderable r : children)
			{
				binded.transform = transformation;
				r.render();
			}
	}
	
	// Returns number of existing structures created
	public static int getNumStructures() {return numStructures;}
	
	public int size() {return children.size();}
	public boolean isEmpty() {return children.isEmpty();}
}
