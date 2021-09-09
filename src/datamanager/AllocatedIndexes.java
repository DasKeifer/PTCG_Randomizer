package datamanager;


import java.util.HashMap;

import compiler.CompilerUtils;

public class AllocatedIndexes extends HashMap<String, Integer>
{
	private static final long serialVersionUID = 42L;

	public AllocatedIndexes() {}
	
	public AllocatedIndexes(AllocatedIndexes allocIndexes) 
	{
		super(allocIndexes);
	}

	public int getThrow(String segmentId)
	{
		return get(segmentId);
	}
	
	public int getTry(String segmentId)
	{
		Integer val = get(segmentId);
		if (val == null)
		{
			return CompilerUtils.UNASSIGNED_ADDRESS;
		}
		return val;
	}
}
