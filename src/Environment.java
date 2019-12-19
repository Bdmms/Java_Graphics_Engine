import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * File: Environment.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This is the environment that 3d objects can be added to
 */

public class Environment
{
	private LinkedList<Structure> structures = new LinkedList<Structure>();	// List of all structures in environment
	private LinkedList<Camera> cameras = new LinkedList<Camera>();			// List of available cameras
	private Structure[] finalizedList;				// Finalized list of renderable objects
	private Camera mainCamera;						// The main camera used for rendering
	private boolean finalized = false;				// Whether the environment has been finalized
	
	// Adds a structure to the environment
	public void addStructure(Structure m) {structures.add(m);}
	
	// Adds a camera (viewport) to the system
	public void addCamera(Camera cam)
	{
		cameras.add(cam);
		structures.add(cam);
	}
	
	// Finalizes all components so that they can be rendering
	public void finalizeRender()
	{
		Iterator<Structure> iterator = structures.iterator();
		finalizedList = new Structure[structures.size()];
		
		// Finalize Components
		for(int i = 0; i < finalizedList.length; i++)
		{
			finalizedList[i] = iterator.next();
			finalizedList[i].finalizeRender();
		}
		
		// Select rendering source
		mainCamera = cameras.getFirst();
		finalized = true;
	}
	
	// Renders the environment and its components
	public BufferedImage drawEnvironment(Graphics g)
	{
		if(!finalized)
			finalizeRender();
		
		mainCamera.graphics = g;
		mainCamera.project(finalizedList);
		
		return mainCamera.generateRender();
	}
}
