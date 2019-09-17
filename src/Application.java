import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Application
{
	private Rectangle bounds;			//Boundaries of the screen
	private GraphicsEnvironment ge; 	//Graphics environment
	private GraphicsDevice[] gs;		//Graphics hardware
	private GraphicsConfiguration gc;	//Graphics configuration
	private BufferStrategy strategy;	//Buffer strategy
	private BufferedImage buffer;		//Frame Buffer
	private Graphics graphics;			//Graphic component
	private Window w;					//Display window
	private Frame frame;				//Display frame
	private KeyBinder keys;				//Control binder
	
	private float rate = 6.0f;			//Rate of rotation
	private float scale = 1.00f;			//Scale of external to internal
	private int internalWidth;			//Internal Resolution
	private int internalHeight;
	private int externalWidth;			//Display Resolution
	private int externalHeight;
	private Environment env;			//Engine environment
	private Camera camera;				//Default Camera
	private Model model;				//Default Model
	
	public static void main(String[] arg) {new Application();}
	
	public Application()
	{
		// Initialize components
		keys = new KeyBinder();
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gs = ge.getScreenDevices();
		gc = gs[0].getConfigurations()[0];
		
		// Resolution Configuration
		bounds = gc.getBounds();
		externalWidth = gc.getBounds().width;
		externalHeight = gc.getBounds().height;
		internalWidth = (int)(gc.getBounds().width * Math.sqrt(scale));
		internalHeight = (int)(gc.getBounds().height * Math.sqrt(scale));
		
		// Frame Configuration
		frame = new Frame(gc);
		frame.setSize(externalWidth, externalHeight);
		frame.setTitle("IP2K Engine");
		frame.addKeyListener(keys);
		frame.setVisible(true);
		
		// Window Configuration
		w = new Window(frame, gc);
		w.setSize(externalWidth, externalHeight);
		w.setLocation(bounds.x, bounds.y);
		w.setVisible(true);
		w.createBufferStrategy(2);
		strategy = w.getBufferStrategy();
		
		// Engine Configuration
		env = new Environment(this);
		
		// Manually adding model and camera
		camera = new Camera(1.5f, 1.0f, 10.0f, internalWidth, internalHeight);
		model = new Model("models\\HyruleCastle\\", "hyrule_castle.obj");
		//model = new Model("models\\The Legend of Zelda - Twilight Princess\\Spinner\\", "spinner.obj");
		//model = new Model("models\\The Legend of Zelda - Twilight Princess\\King Bulblin Test Area\\", "king bulblin test area.obj");

		env.addStructure(model);
		env.addCamera(camera);
		env.finalize();
		model.position[0] = 1000;
		model.position[1] = 0;
		model.position[2] = 0;
		
		graphics = strategy.getDrawGraphics();
		
		// Game Loop
		while(!keys.inputs[KeyBinder.KEY_QUIT])
		{
			Time.frame();
			controller();
			refresh();
		}
		
		// Remove window
		w.setVisible(false);
		w.dispose();
		
		System.out.println("CLOSING APPLICATION");
		System.exit(1);
	}
	
	// Refreshes the display
	public void refresh()
	{
		buffer = env.drawEnvironment();
		
		do 
		{
			// Create Graphic Context
        	graphics = strategy.getDrawGraphics();
        	
        	// Graphic Rendering
        	graphics.drawImage(buffer, 0, 0, externalWidth, externalHeight, w);
        	
        	graphics.setColor(Color.WHITE);
        	graphics.drawString("FPS: " + Time.frameRate, 5, 20);

            // Graphics Disposal
            graphics.dispose();
             
        } while (strategy.contentsRestored());
		
		// Display
		strategy.show();
	}
	
	// Used to respond to control inputs
	public void controller()
	{
		if(keys.inputs[KeyBinder.KEY_X_UP]) camera.position[0] += rate * Time.deltaTime;
		if(keys.inputs[KeyBinder.KEY_X_DOWN]) camera.position[0] -= rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_Y_UP]) camera.position[1] += rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_Y_DOWN]) camera.position[1] -= rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_Z_UP]) camera.position[2] += rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_Z_DOWN]) camera.position[2] -= rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_ROT_L]) model.rotation[2] += rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_ROT_R]) model.rotation[2] -= rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_ROT_U]) model.rotation[0] += rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_ROT_D]) model.rotation[0] -= rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_ROT_O]) model.rotation[1] += rate * Time.deltaTime; 
		if(keys.inputs[KeyBinder.KEY_ROT_P]) model.rotation[1] -= rate * Time.deltaTime; 
		
		for(int i = 0; i < 10; i++)
		{
			if(keys.inputs[KeyBinder.KEY_NUM + i])
			{
				model.getBodyGroup(i).toggleVisibility();
				keys.inputs[KeyBinder.KEY_NUM + i] = false;
			}
		}
	}
	
	// Highlights a vertex / vector in its rendered location
	public void debugVertex(Graphics g, Camera c, Line proj)
	{
		float scale = 2;
		float[] vertex = c.getVertexPosition(proj);
		
		g.setColor(Color.WHITE);
		g.fillRect((int)(vertex[0]*scale), (int)(vertex[1]*scale), (int) scale, (int) scale);
		g.drawString(proj.vector[0] + ", " + proj.vector[1] + ", " + proj.vector[2], (int)(vertex[0]*scale), (int)(vertex[1]*scale));
	}
	
	// Prints the current computer's hardware details to the console
	public void outputGraphicDevices()
	{
		Rectangle virtualBounds = new Rectangle();
		
		for (int j = 0; j < gs.length; j++) 
		{
        	GraphicsConfiguration[] temp = gs[j].getConfigurations();
          
        	System.out.println("-----Device " + j + "-----");
        	System.out.println("ID: " + gs[j].getIDstring());
        	System.out.println("Type: " + gs[j].getType() + " (" + GraphicsDevice.TYPE_RASTER_SCREEN + " = RASTER SCREEN)");
        	System.out.println("Available Accelerated Memory: " + gs[j].getAvailableAcceleratedMemory() + " bytes");
        	System.out.println("Fullscreen Support: " + gs[j].isFullScreenSupported());
        	System.out.println("Display Change Support: " + gs[j].isDisplayChangeSupported());
        	System.out.println("Current Bit Depth: " + gs[j].getDisplayMode().getBitDepth() + " bits");
        	System.out.println("Current Refresh Rate: " + gs[j].getDisplayMode().getRefreshRate() + " FPS");
        	System.out.println("Resolution: " + gs[j].getDisplayMode().getWidth() + "x" + gs[j].getDisplayMode().getHeight());
        	
        	for (int i=0; i < temp.length; i++) 
        	{
        		System.out.println("     -----Configuration " + i + "-----");
        		System.out.println("     Translucency Capability: " + temp[i].isTranslucencyCapable());
        		virtualBounds = virtualBounds.union(temp[i].getBounds());
        		gc = gs[j].getConfigurations()[i];
        	}
	    } 
	}
	
	public int getInternalWidth() {return internalWidth;}
	public int getInternalHeight() {return internalHeight;}
	
	// Custom error handler, use to display object's and their parameters
	public static void throwError(String message, Object o)
	{
		System.err.println(message);
		System.err.println("Source: ");
		System.err.println(o);
		System.exit(-1);
	}
	
	// Debug only! Prints an array of floats to the console (use for printing vectors)
	public static void printFloatArr(float[] arr)
	{
		System.out.print(arr[0]);
		for(int i = 1; i < arr.length; i++)
			System.out.print(", " + arr[i]);
		System.out.print("\n");
	}
}
