import java.util.Iterator;
import java.util.LinkedList;

/*
 * File: BodyGroup.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This class stores the sub group of a model
 */

public class BodyGroup extends Structure
{
	Material material = Material.DEFAULT_MAT;				// The assigned material to the body group
	private LinkedList<Face> faces = new LinkedList<Face>();// The list of faces in the body group
	
	public BodyGroup(String name)
	{
		super(name);
		this.name = name;
		
		System.out.println("CREATED BODYGROUP: " + name);
	}
	
	// Adds a face to the body group
	public void addFace(Face f)
	{
		faces.add(f);
	}
	
	// Updates the transformations before rendering
	public void updateTransformation(final float[] ref, final float[] rot, final float[] scale)
	{
		calculatePosition(ref, rot, scale);
		
		if(visible)
			for(int i = 0; i < finalizedList.length; i++)
				finalizedList[i].updateTransformation(finalPosition, finalRotation, finalScale);
	}
	
	// Renders the bodygroup to the display
	public void render(Camera camera) 
	{
		if(visible)
			for(int i = 0; i < finalizedList.length; i++)
				finalizedList[i].render(camera);
	}
	
	// Finalizes the bodygroup before rendering process begins
	public void finalize()
	{
		finalizedList = new Renderable[faces.size()];
		Iterator<Face> iterator = faces.iterator();
		
		for(int i = 0; i < finalizedList.length; i++)
		{
			finalizedList[i] = iterator.next();
			finalizedList[i].finalize();
		}
	}
	
	public int size() {return faces.size();}
	public boolean isEmpty() {return faces.size() == 0;}
}
