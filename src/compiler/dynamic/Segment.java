package compiler.dynamic;

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
		rootFragment = new Fragment();
		labelledFragments = new LinkedList<>();
	}
	
	// TODO AppendInstruction
	
	public void assignLabelAddresses(int functionAddress, Map<String, Integer> labelsToAddress)
	{
		if (labelsToAddress.put(label, functionAddress) != null)
		{
			//collision
			throw new IllegalArgumentException("Label collision!");
		}
		
		// Go through and search for each label in the function and assign those addresses as well
	}

	public void startNewFragment(String fragmentName) 
	{
		// TODO Auto-generated method stub
		
	}
	
	// TODO write
}
