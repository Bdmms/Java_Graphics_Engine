import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * File: Model.java
 * Author: Sean Rannie
 * Last Edited: September/12/2019
 * 
 * This class stores the data of a 3D model
 */

public class Model extends Structure
{
	public enum ObjCommand { NONE, VERTEX_COORDINATE, TEXTURE_COORDINATE, NORMAL, FACE, MTLLIB, USEMTL, BODYGROUP, G, COMMENT}
	
	String filename;			// Filename of model object
	String folder;				// Directory the model object is located in
	String materialFile;		// Material file (MTL) associated with model
	private int numFaces = 0;	// Total number of faces in model
	private LinkedList<Material> materials = new LinkedList<Material>();	// List of materials in model
	private boolean hasTextures;	// Whether the model is using a texture (in obj)
	private boolean hasNormals;		// Whether the model is using normals (in obj)
	
	public Model(String folder, String file)
	{
		super(file);
		this.folder = folder;
		
		hasTextures = false;
		hasNormals = false;
		
		readObjFile(name);
	}
	
	// Loads an .obj model file
	private void readObjFile(String filein)
	{
		try {
			System.out.println("OPENING: " + filein);
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(folder + filein)));
			LinkedList<Vertex> vertices = new LinkedList<Vertex>();
			LinkedList<float[]> textures = new LinkedList<float[]>();
			LinkedList<float[]> normals = new LinkedList<float[]>();
			BodyGroup group = null;
			filename = filein;
			String line; 
			ObjCommand header;
			int nullCount = 0;
			
			while(nullCount < 10)
			{
				line = reader.readLine();
				
				if(line != null && line.length() > 0)
				{
					nullCount = 0;
					header = objReturnLineHead(line);
					line = line.substring(line.indexOf(' ') + 1);
					
					while(line.charAt(0) == ' ')
						line = line.substring(1);
					
					switch(header)
					{
					case VERTEX_COORDINATE:
						vertices.add(new Vertex(readValues(line, 3)));
						break;
						
					case TEXTURE_COORDINATE:
						textures.add(readValues(line, 2));
						hasTextures = true;
						break;
						
					case NORMAL:
						normals.add(readValues(line, 3));
						hasNormals = true;
						break;
						
					case BODYGROUP:
						if(group != null)
						{
							numFaces += group.size();
							children.add(group);
						}
						
						group = new BodyGroup(line);
						break;
						
					case FACE:
						int[] face = readFaces(line);
						Face f = new Face(group, face);
						byte i = 0;
						
						for(byte v = 0; v < 3; v++)
						{
							f.addVertex(vertices.get(face[i++] - 1));
							if(hasTextures) f.getVertex(v).setTexture(textures.get(face[i++] - 1));
							if(hasNormals) f.getVertex(v).setNormal(normals.get(face[i++] - 1));
						}
						
						group.addChild(f);
						break;
						
					case MTLLIB: 
						materialFile = line;
						readMTLFile(materialFile);
						break;
						
					case USEMTL: 
						Material mat = searchForMaterial(line);
						
						if(group == null)
							group = new BodyGroup(mat.name);
						else
						{
							if(group.material != Material.DEFAULT_MAT)
							{
								children.add(group);
								group = new BodyGroup(mat.name);
							}
						}
						
						group.material = mat;
						break;
						
					case COMMENT:
						System.out.println("# " + line);
						break;
						
					default: break;
					}
				
				}
				else
					nullCount++;
			}
			
			System.out.println("EXITING: " + filein);
			
			if(group != null)
			{
				numFaces += group.size();
				children.add(group);
			}
			
			reader.close();
			
			System.out.println(vertices.size() + " vertices");
			System.out.println(textures.size() + " textures");
			System.out.println(normals.size() + " normals");
			System.out.println(numFaces + " faces");
			System.out.println(children.size() + " bodygroups");
			System.out.println(materials.size() + " materials");
		} 
		catch (FileNotFoundException e) {System.out.println("ERROR - FAILED TO LOAD OBJ FILE: " + filein);} 
		catch (IOException e) {System.out.println("ERROR - FAILED TO READ OBJ FILE: " + filein);}
	}
	
	// Reads a .mtl file from the .obj file
	public void readMTLFile(String filein)
	{
		try {
			System.out.println("OPENING: " + filein);
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(folder + filein)));
			String line;
			int nullCount = 0;
			
			while(nullCount < 10)
			{
				line = reader.readLine();
				
				if(line != null && line.length() > 2)
				{
					nullCount = 0;
					if(line.substring(0, line.indexOf(' ')).equals("newmtl"))
					{
						line = line.substring(line.indexOf(' ') + 1);
						materials.add(new Material(reader, folder, line));
					}
				}
				else 
					nullCount++;
			}
			
			System.out.println("EXITING: " + filein);
			
			reader.close();
		}
		catch (FileNotFoundException e) {System.out.println("ERROR - FAILED TO LOAD MTL FILE: " + filein);} 
		catch (IOException e) {System.out.println("ERROR - FAILED TO READ MTL FILE: " + filein);}
	}
	
	// Searches for a material with a given name in the model's material list
	public Material searchForMaterial(String name)
	{
		Iterator<Material> iterator = materials.iterator();
		
		while(iterator.hasNext())
		{
			Material mat = iterator.next();
			if(mat.name.equals(name))
				return mat;
		}
		
		System.out.println("FAILED TO FIND MATERIAL: " + name);
		
		return Material.DEFAULT_MAT;
	}
	
	// Whether the file line indicates a new material should be created
	public static boolean isNewMTL(String line)
	{
		return line.substring(0, line.indexOf(' ')).equals("newmtl");
	}
	
	// Returns the OBJ command from the file line
	public static ObjCommand objReturnLineHead(String line)
	{
		if(line == null)
			return ObjCommand.NONE;
		else if(line.length() <= 1)
		{
			if(line.equals("g"))
				return ObjCommand.G;
			else
				return ObjCommand.NONE;
		}
		
		String set = line.substring(0, line.indexOf(' '));
		
		switch(set)
		{
		case "v": 
			return ObjCommand.VERTEX_COORDINATE;
		case "vt": 
			return ObjCommand.TEXTURE_COORDINATE;
		case "vn": 
			return ObjCommand.NORMAL;
		case "f": 
			return ObjCommand.FACE;
		case "g": 
			return ObjCommand.BODYGROUP;
		case "mtllib":
			return ObjCommand.MTLLIB;
		case "usemtl":
			return ObjCommand.USEMTL;
		case "#":
			return ObjCommand.COMMENT;
		default: 
			return ObjCommand.NONE;
		}
	}
	
	// Reads multiple sequential parameters from a file line
	public static float[] readValues(String line, int num)
	{
		float[] values = new float[num];
		num--;
		
		for(int i = 0; i < num; i++)
		{
			values[i] = Float.parseFloat(line.substring(0, line.indexOf(' ')));
			line = line.substring(line.indexOf(' ') + 1);
		}
		
		if(line.indexOf(' ') == -1)
			values[num] = Float.parseFloat(line);
		else
			values[num] = Float.parseFloat(line.substring(0, line.indexOf(' ')));
		
		return values;
	}
	
	// Reads the format for faces from a file line in the .obj file (TODO: support faces with more than 3 vertices)
	public int[] readFaces(String line)
	{
		int size = 3;
		if(hasTextures) size += 3;
		if(hasNormals) size += 3;
		
		int[] face = new int[size];
		int nextEnd = 0;
		
		for(byte i = 0; nextEnd >= 0 && i < face.length; i++)
		{
			nextEnd = nextEndingFace(line);
			
			if(nextEnd == -1)
				face[i] = Integer.parseInt(line);
			else
			{
				face[i] = Integer.parseInt(line.substring(0, nextEnd));
				line = line.substring(nextEnd + 1);
			}
		}
		
		return face;
	}
	
	// Determines where the next face character is in file line
	public static int nextEndingFace(String line)
	{
		int a = line.indexOf(' ');
		int b = line.indexOf('/');
		
		if(a == -1 && b == -1)
			return -1;
		else if(a == -1)
			return b;
		else if(b == -1)
			return a;
		else
			return a < b ? a : b;
	}
}
