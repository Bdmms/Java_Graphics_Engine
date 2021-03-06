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
	}
	
	// Used to modify the line with recreating the object
	public void reset(float[] v1, float[] v2)
	{
		point = v1;
		vector[0] = v2[0] - v1[0];
		vector[1] = v2[1] - v1[1];
		vector[2] = v2[2] - v1[2];
	}
	
	// Used to modify the line with recreating the object
	public void reset(float[] vect)
	{
		point = Vertex.ORIGIN;
		vector = vect;
	}
	
	// Returns the dot product between the line and another vector
	public float dot(float[] vect)
	{
		return vector[0]*vect[0] + vector[1]*vect[1] + vector[2]*vect[2];
	}
	
	// Returns the magnitude of the direction vector 
	public float mag()
	{
		return (float)Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2]);
	}
	
	// Returns the squared magnitude of the direction vector 
	public float sqr_mag()
	{
		return vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2];
	}
	
	// Multiplies a vector by a scalar value
	public static float[] multiply(float[] vect, float mult)
	{
		for(byte a = 0; a < vect.length; a++)
			vect[a] *= mult;
		return vect;
	}
	
	// Divides a vector by a scalar value
	public static void divide(float[] result, float[] vect, float mult)
	{
		for(byte a = 0; a < vect.length; a++)
			result[a] = vect[a] / mult;
	}
	
	public static void divideBy(float[] vect, float mult)
	{
		for(byte a = 0; a < vect.length; a++)
			vect[a] /= mult;
	}
	
	// Returns the unit vector
	public static float[] unit(float[] vect)
	{
		divideBy(vect, mag(vect));
		return vect;
	}
	
	// Sets the unit vector
	public static void unit(float[] unit, float[] vect)
	{
		divide(unit, vect, mag(vect));
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
