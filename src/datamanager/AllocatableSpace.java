package datamanager;

import java.util.List;
import java.util.TreeMap;

class AllocatableSpace extends AddressRange
{
	TreeMap<Byte, List<AllocData>> allocationsByPriority;
	
	public AllocatableSpace(int start, int stop)
	{
		super(start, stop);
		allocationsByPriority = new TreeMap<>();
	}
	
	public AllocatableSpace(AddressRange range)
	{
		super(range);
		allocationsByPriority = new TreeMap<>();
	}

	public void clear()
	{
		allocationsByPriority.clear();
	}
	
	public boolean addIfSpaceLeft(AllocData alloc)
	{
		if (alloc.getCurrentSize() > spaceLeft())
		{
			return false;
		}
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		return true;
	}
	
	public int spaceLeft()
	{ 
		int spaceLeft = size();
		for (List<AllocData> allocWithPriority : allocationsByPriority.values())
		{
			for (AllocData alloc : allocWithPriority)
			{
				spaceLeft -= alloc.getCurrentSize();
			}
		}
		
		return spaceLeft;
	}
}
