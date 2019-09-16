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
	private Application root;
	private LinkedList<Structure> structures = new LinkedList<Structure>();	// List of all structures in environment
	private LinkedList<Camera> cameras = new LinkedList<Camera>();			// List of available cameras
	private Renderable[] finalizedList;				// Finalized list of renderable objects
	private Camera mainCamera;						// The main camera used for rendering
	private boolean finalized = false;				// Whether the environment has been finalized
	
	private float[] anchorPosition = new float[3];	// The anchor position of the environment
	private float[] anchorRotation = new float[3];	// The anchor rotation of the environment
	private float[] anchorScale = {1,1,1};			// The anchor scale of the environment
	
	public Environment(Application r)
	{
		root = r;
	}
	
	// Adds a structure to the environment
	public void addStructure(Structure m) {structures.add(m);}
	
	// Adds a camera (viewport) to the system
	public void addCamera(Camera cam)
	{
		cameras.add(cam);
		structures.add(cam);
	}
	
	// Projects the vertices present in the environment to the camera
	public void project(Camera camera, final float[] ref, final float[] rot, final float[] scale)
	{
		camera.getBuffer().refresh();
		
		ref[0] = -camera.position[0];
		ref[1] = -camera.position[1];
		ref[2] = -camera.position[2];
		rot[0] = -camera.rotation[0];
		rot[1] = -camera.rotation[1];
		rot[2] = -camera.rotation[2];
		
		for(int i = 0; i < finalizedList.length; i++)
			finalizedList[i].updateTransformation(ref, rot, scale);
	}
	
	// Renders the environment
	public BufferedImage render(Camera camera)
	{
		for(int i = 0; i < finalizedList.length; i++)
			finalizedList[i].render(camera);
		
		return camera.getBuffer().render();
	}
	
	// Finalizes all components so that they can be rendering
	public void finalize()
	{
		Iterator<Structure> iterator = structures.iterator();
		finalizedList = new Structure[structures.size()];
		
		// Finalize Components
		for(int i = 0; i < finalizedList.length; i++)
		{
			finalizedList[i] = iterator.next();
			finalizedList[i].finalize();
		}
		
		// Select rendering source
		mainCamera = cameras.getFirst();
		finalized = true;
	}
	
	// Renders the environment and its components
	public BufferedImage drawEnvironment()
	{
		if(!finalized)
			finalize();
		
		project(mainCamera, anchorPosition, anchorRotation, anchorScale);
		return render(mainCamera);
	}
}
