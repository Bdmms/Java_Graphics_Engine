
/*
 * File: BodyGroup.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This class stores the sub group of a model. 
 * A body group can group faces with the same material.
 */

public class BodyGroup extends Structure
{
	Material material = Material.DEFAULT_MAT;				// The assigned material to the body group
	
	public BodyGroup(String name)
	{
		super(name);
		System.out.println("CREATED BODYGROUP: " + name);
	}
}
