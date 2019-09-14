import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

/*
 * File: KeyBinder.java
 * Author: Sean Rannie
 * Last Edited: September/13/2019
 * 
 * This class is used to bind (pair) input codes to functions
 */

public class KeyBinder implements KeyListener
{
	public final static byte KEY_QUIT = 0;
	public final static byte KEY_X_UP = 1;
	public final static byte KEY_X_DOWN = 2;
	public final static byte KEY_Y_UP = 3;
	public final static byte KEY_Y_DOWN = 4;
	public final static byte KEY_Z_UP = 5;
	public final static byte KEY_Z_DOWN = 6;
	public final static byte KEY_ROT_L = 7;
	public final static byte KEY_ROT_R = 8;
	public final static byte KEY_ROT_U = 9;
	public final static byte KEY_ROT_D = 10;
	public final static byte KEY_ROT_O = 11;
	public final static byte KEY_ROT_P = 12;
	public final static byte KEY_SHIFT = 13;
	public final static byte KEY_NUM = 14;
	
	private HashMap<Integer, Byte> keybind = new HashMap<Integer, Byte>();	// Binded key map
	boolean[] inputs = new boolean[32];										// Input buffers
	boolean awake = false;													// if input system is awake
	
	public KeyBinder()
	{
		keybind.put(KeyEvent.VK_BACK_SLASH, KEY_QUIT);
		keybind.put(KeyEvent.VK_Q, KEY_X_UP);
		keybind.put(KeyEvent.VK_E, KEY_X_DOWN);
		keybind.put(KeyEvent.VK_D, KEY_Y_UP);
		keybind.put(KeyEvent.VK_A, KEY_Y_DOWN);
		keybind.put(KeyEvent.VK_W, KEY_Z_UP);
		keybind.put(KeyEvent.VK_S, KEY_Z_DOWN);
		keybind.put(KeyEvent.VK_LEFT, KEY_ROT_L);
		keybind.put(KeyEvent.VK_RIGHT, KEY_ROT_R);
		keybind.put(KeyEvent.VK_UP, KEY_ROT_U);
		keybind.put(KeyEvent.VK_DOWN, KEY_ROT_D);
		keybind.put(KeyEvent.VK_O, KEY_ROT_O);
		keybind.put(KeyEvent.VK_P, KEY_ROT_P);
		keybind.put(KeyEvent.VK_SHIFT, KEY_SHIFT);
		
		for(int i = 0; i < 9; i++)
			keybind.put(KeyEvent.VK_1 + i, (byte)(KEY_NUM + i));
	}
	
	public void keyPressed(KeyEvent e) 
	{
		if(keybind.containsKey(e.getKeyCode()))
			inputs[keybind.get(e.getKeyCode())] = true;
	}
	
	public void keyReleased(KeyEvent e) 
	{
		if(keybind.containsKey(e.getKeyCode()))
			inputs[keybind.get(e.getKeyCode())] = false;
	}

	public void keyTyped(KeyEvent arg0) {
	}
}
