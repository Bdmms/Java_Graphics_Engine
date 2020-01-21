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
	
	protected Transformation transformation;
	protected LinkedList<Renderable> children;	// The dynamic list of children stored within the structure
	protected Renderable[] finalizedList;		// Finalized list used to automate rendering process
	
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
	public void render(Transformation ref, Camera camera) 
	{
		transformation.propagateTransformation(ref);
		
		if(visible)
			for(int i = 0; i < finalizedList.length; i++)
				finalizedList[i].render(transformation, camera);
	}
	
	// Returns a renderable component of this object
	public Renderable getRenderable(int i){return finalizedList[i];}
	
	// Returns number of existing structures created
	public static int getNumStructures() {return numStructures;}
	
	public int size() {return children.size();}
	public boolean isEmpty() {return children.isEmpty();}
}
