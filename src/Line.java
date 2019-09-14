/*
 * File: Line.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * This is a line object in 3d space
 */

public class Line 
{
	float[] vector;			// Direction vector of line
	float[] point;			// Anchor position vector of line
	private float mag = 0;	// Magnitude of vector
	
	public Line(float[] source, float[] pos)
	{
		vector = new float[3];
		reset(source, pos);
	}
	
	public Line(Vertex v1, Vertex v2)
	{
		vector = new float[3];
		reset(v1, v2);
	}
	
	// Used to modify the line with recreating the object
	public void reset(Vertex v1, Vertex v2)
	{
		point = v1.vertex;
		vector[0] = v2.vertex[0] - v1.vertex[0];
		vector[1] = v2.vertex[1] - v1.vertex[1];
		vector[2] = v2.vertex[2] - v1.vertex[2];
		mag = (float) Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2]);
	}
	
	// Used to modify the line with recreating the object
	public void reset(float[] v1, float[] v2)
	{
		point = v1;
		vector[0] = v2[0] - v1[0];
		vector[1] = v2[1] - v1[1];
		vector[2] = v2[2] - v1[2];
		mag = (float) Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2]);
	}
	
	// Used to modify the line with recreating the object
	public void reset(float[] vect)
	{
		point = Vertex.ORIGIN;
		vector[0] = vect[0];
		vector[1] = vect[1];
		vector[2] = vect[2];
		mag = (float) Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2]);
	}
	
	// Returns the dot product between the line and another vector
	public float dot(float[] vect)
	{
		return vector[0]*vect[0] + vector[1]*vect[1] + vector[2]*vect[2];
	}
	
	// Returns the magnitude of the direction vector 
	public float mag()
	{
		return mag;
	}
	
	// Multiplies a vector by a scalar value
	public static float[] multiply(float[] vect, float mult)
	{
		for(byte a = 0; a < vect.length; a++)
			vect[a] *= mult;
		return vect;
	}
	
	// Divides a vector by a scalar value
	public static float[] divide(float[] vect, float mult)
	{
		for(byte a = 0; a < vect.length; a++)
			vect[a] /= mult;
		return vect;
	}
	
	// Returns the unit vector
	public static float[] unit(float[] vect)
	{
		return divide(vect, mag(vect));
	}
	
	// Returns the magnitude of the vector
	public static float mag(float[] vect)
	{
		return (float) Math.sqrt(vect[0]*vect[0] + vect[1]*vect[1] + vect[2]*vect[2]);
	}
	
	// Returns the dot product of two vectors
	public static float dot(float[] vect1, float[] vect2)
	{
		return vect1[0]*vect2[0] + vect1[1]*vect2[1] + vect1[2]*vect2[2];
	}
}
