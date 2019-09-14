/**
 * @deprecated
 */

public class Matrix 
{
	float[][] matrix;
	int width;
	int height;
	
	public Matrix(int w, int h)
	{
		int width = w;
		int height = h;
		matrix = new float[width][height];
	}
	
	public Matrix dot(Matrix m)
	{
		Matrix temp = new Matrix(m.width, height);
		float acc;
		
		for(int x = 0; x < m.width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				acc = 0;
				
				for(int i = 0; i < width; i++)
					acc += matrix[i][y]*m.matrix[x][i];
				
				temp.matrix[x][y] = acc;
			}
		}
		
		return temp;
	}
}	
