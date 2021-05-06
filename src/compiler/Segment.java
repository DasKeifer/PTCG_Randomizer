package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Segment 
{
	String label;
	List<Data> data;
	
	public Segment(String label)
	{
		this.label = label;
		data = new LinkedList<>();
	}
	
	// TODO AppendInstruction
	
	public int getWorstCaseSizeOnBank(byte bank)
	{
		return 0; // TODO
	}
	
	// TODO write
}
