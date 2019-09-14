/*
 * File: Time.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * This class manages the passing of time between frames
 */

public class Time 
{
	private static long nextSecond = System.currentTimeMillis();//Time of next second interval
	private static int frameCount = 0;							//The current frame count of the second
	private static long lastFrame = 0;							//The time of the last frame
	private static long currentFrame = 0;						//The time of the current frame
	private static long nextFrame = System.nanoTime();			//Time of next frame
	
	public static int frameRate = 0;							//The current frame rate
	public static long globalCount = 0;							//The number of frames counted
	public static float deltaTime = 1;							//Difference in time from last frame
	
	// Used to pause until next 60 Hz tick
	public static void vSync()
	{
		while(nextFrame > currentFrame)
			currentFrame = System.nanoTime();
		nextFrame += 16666666; //16.6 ms
	}
	
	// Calculates time change between frames
	public static void frame()
	{
		// GPU Thread frame count
     	frameCount++;
 		if(System.currentTimeMillis() >= nextSecond)
 		{
 			frameRate = frameCount;
 			frameCount = 0;
 			nextSecond += 1000; //1.0 s
 		}
 		
 		lastFrame = currentFrame;
 		currentFrame = System.nanoTime();
 		deltaTime = (float) (currentFrame - lastFrame) / 1000000000;
		
		globalCount++;
		
		//vSync();
	}
}
