/**
 * @deprecated
 */

public class RenderList 
{
	class Node
	{
		Node next;
		Node prev;
		Face value;
		float depth;
		
		Node(Node n, Node p, Face f, float d)
		{
			next = n;
			prev = p;
			value = f;
			depth = d;
		}
	}
	
	private Node start;
	private Node current;
	private int size;
	
	public RenderList()
	{
		reset();
	}
	
	public void reset()
	{
		current = start = null;
		size = 0;
	}
	
	public void addFace(Face face, float depth)
	{
		if(start == null) 
		{
			start = new Node(null, null, face, depth);
			size++;
			return;
		}
		
		Node n = start;
		
		while(n.next != null)
		{
			if(n.depth > depth)
			{
				if(n.prev == null) //start of list
					start = new Node(n, null, face, depth);
				else
				{
					Node i = new Node(n, n.prev, face, depth);
					n.prev = n.prev.next = i;
				}

				size++;
				return;
			}
			
			n = n.next;
		}
		
		n.next = new Node(null, n, face, depth);
		size++;
	}
	
	public void print()
	{
		int i = 0;
		
		System.out.println("Size: " + size);
		
		current = start;
		while(hasNext())
		{
			i++;
			System.out.println(i + ": " + current.depth);
			current = current.next;
		}
	}
	
	public void iterator()
	{
		current = start;
	}
	
	public Face next()
	{
		Face temp = current.value;
		current = current.next;
		return temp;
	}
	
	public boolean hasNext()
	{
		return current != null;
	}
	
	public int size()
	{
		return size;
	}
}
