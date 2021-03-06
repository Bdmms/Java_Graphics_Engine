import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/*
 * File: Material.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * Material object which paints faces of a model
 */

public class Material 
{
	public static final Material DEFAULT_MAT = new Material("default");
	
	public enum MtlCommand { NONE, Kd, Ka, Ks, Ke, Ns, Ni, Tr, TYPE, FILE }; // Commands that can be read in MTL file
	public enum MaterialType { NONE, ILLUM2 };						 // Material types that can be read
	
	private short[] r_image;
	private short[] g_image;
	private short[] b_image;
	private int[] image;					// Image data of texture
	private String file = "...";			// File name
	
	MaterialType type = MaterialType.NONE;	// Material type
	float[] Kd = new float[3];				// Ambient texture vector
	float[] Ka = new float[3];				// Diffuse texture vector
	float[] Ks = new float[3];				// Specular color texture vector
	float[] Ke = new float[3];				// Emissive texture vector
	float Ns = 0;							// Specular highlights		
	float Tr = 0;							// Transparency
	int width;								// Image width
	int height;								// Image height
	BinarySize bin_width;					// Binary Image width
	BinarySize bin_height;					// Binary Image height
	String name;							// Material name
	
	public Material(String nm)
	{
		name = nm;
		width = 1;
		height = 1;
		bin_width = BinarySize.x1;
		bin_height = BinarySize.x1;
		image = new int[width * height];
		r_image = new short[width * height];
		g_image = new short[width * height];
		b_image = new short[width * height];
		image[0] = 0x0000FF;
		r_image[0] = 0xFF;
	}
	
	public Material(BufferedReader reader, String folder, String nm) throws IOException
	{
		System.out.println("CREATED MATERIAL: " + nm);
		
		name = nm;
		readMTLFile(reader, folder);
	}
	
	public short[] getRedChannel(){return r_image;}
	public short[] getGreenChannel(){return g_image;}
	public short[] getBlueChannel(){return b_image;}
	public int[] getTexture(){return image;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
	// Reads image from filename and returns it as an array
	private void readImage(String file)
	{
		try {
			BufferedImage src_image = ImageIO.read(new File(file));
			image = new int[src_image.getWidth() *  src_image.getHeight()];
			r_image = new short[src_image.getWidth() *  src_image.getHeight()];
			g_image = new short[src_image.getWidth() *  src_image.getHeight()];
			b_image = new short[src_image.getWidth() *  src_image.getHeight()];
			
			for(int x = 0; x <  src_image.getWidth(); x++)
				for(int y = 0; y <  src_image.getHeight(); y++)
				{
					int index = x + y *  src_image.getWidth();
					image[index] =  src_image.getRGB(x, y);
					r_image[index] = (short) ((0xFF0000 & src_image.getRGB(x, y)) >> 16);
					g_image[index] = (short) ((0xFF00 & src_image.getRGB(x, y)) >> 8);
					b_image[index] = (short) (0xFF & src_image.getRGB(x, y));
				}
			
			width =  src_image.getWidth();
			height =  src_image.getHeight();
			bin_width = BinarySize.match(width);
			bin_height = BinarySize.match(height);
		} catch (IOException e) { Application.throwError("ERROR - FAILED TO LOAD TEXTURE: " + file, this);}
	}
	
	// Reads an exert from a .mtl file (TODO: reader must already be opened in instance)
	private void readMTLFile(BufferedReader reader, String folder) throws IOException
	{
		String line = "";
		MtlCommand command = MtlCommand.NONE;
		
		while(command != MtlCommand.FILE)
		{
			line = reader.readLine();
			
			if(line != null && line.length() > 1)
			{
				// Remove any beginning whitespace characters
				while(line.charAt(0) <= ' ')
					line = line.substring(1);
				
				command = objReturnLineHead(line);
				
				// Interpret command
				switch(command)
				{
				case Ka: Ka = Model.readValues(line.substring(3), 3);break;
				case Kd: Kd = Model.readValues(line.substring(3), 3);break;	
				case Ks: Ks = Model.readValues(line.substring(3), 3);break;
				case Ke: Ke = Model.readValues(line.substring(3), 3);break;
				case Ns: Ns = Float.parseFloat(line.substring(3));break;
				case Ni: System.out.println("Warning - Ni material parameter detected, this program does not support Ni");
				case Tr: Tr = Float.parseFloat(line.substring(2));break;
				case TYPE: type = readType(line); break;
				case FILE: 
					file = folder + line.substring(7);
					readImage(file);
					break;
				default: break;
				}
			}
		}
	}
	
	// Converts material type string to enumerator
	public static MaterialType readType(String line)
	{
		switch(line)
		{
		case "illum 2": return MaterialType.ILLUM2;
		default: return MaterialType.NONE;
		}
	}
	
	// Returns the command type of file line
	public static MtlCommand objReturnLineHead(String line)
	{
		if(readType(line) != MaterialType.NONE)
			return MtlCommand.TYPE;
		
		//if(line.indexOf(' ') == -1)
		//	return MtlCommand.TYPE;
		
		String set = line.substring(0, line.indexOf(' '));
		
		switch(set)
		{
		case "Ka": 
			return MtlCommand.Ka;
		case "Kd": 
			return MtlCommand.Kd;
		case "Ks": 
			return MtlCommand.Ks;
		case "Ke": 
			return MtlCommand.Ke;
		case "Ns": 
			return MtlCommand.Ns;
		case "Ni":
			return MtlCommand.Ni;
		case "map_Kd":
			return MtlCommand.FILE;
		case "d":
			return MtlCommand.Tr;
		case "newmtl":
			Application.throwError("ERROR - MTL FILE IS INVALID", line);
		default: 
			System.out.println("WARNING - UNABLE TO INTERPRET: " + line);
			return MtlCommand.NONE;
		}
	}
	
	// Print function
	public void print()
	{
		System.out.println("Material: " + name + " (" + file +')');
		System.out.println((width) + "x" + (height));
		System.out.print("Ka: ");
		Application.printArray(Ka);
		System.out.print("Kd: ");
		Application.printArray(Kd);
		System.out.print("Ks: ");
		Application.printArray(Ks);
		System.out.print("Ke: ");
		Application.printArray(Ke);
		System.out.println("Ns: " + Ns);
	}
}
