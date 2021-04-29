package datamanager;

import java.util.List;
import java.util.TreeMap;

class AllocatableSpace extends AddressRange
{
	TreeMap<Byte, List<AllocData>> allocationsByPriority;
	
	public AllocatableSpace(int start, int stopExclusive)
	{
		super(start, stopExclusive);
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
	
	public void assignAddresses()
	{
		int nextStart = start;
		for (List<AllocData> allocWithPriority : allocationsByPriority.values())
		{
			for (AllocData alloc : allocWithPriority)
			{
				alloc.setAddress(nextStart);
				nextStart += alloc.getCurrentSize();
				
				if (nextStart > stopExclusive)
				{
					throw new RuntimeException("Error - misaccounted for allocatable space! ran out of space!");
				}
			}
		}
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
