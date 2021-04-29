package datamanager;

import java.util.List;
import java.util.TreeMap;

import util.RomUtils;

class AllocatableSpace extends AddressRange
{
	TreeMap<Byte, List<FlexibleBlock>> allocationsByPriority;
	
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
	
	public boolean addIfSpaceLeft(FlexibleBlock alloc)
	{
		if (alloc.getCurrentSizeOnBank(RomUtils.determineBank(start)) > spaceLeft())
		{
			return false;
		}
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		return true;
	}
	
	public void assignAddresses()
	{
		int nextStart = start;
		for (List<FlexibleBlock> allocWithPriority : allocationsByPriority.values())
		{
			for (FlexibleBlock alloc : allocWithPriority)
			{
				alloc.setAssignedAddress(nextStart);
				nextStart += alloc.getCurrentSizeOnBank(RomUtils.determineBank(start));
				
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
		for (List<FlexibleBlock> allocWithPriority : allocationsByPriority.values())
		{
			for (FlexibleBlock alloc : allocWithPriority)
			{
				spaceLeft -= alloc.getCurrentSizeOnBank(RomUtils.determineBank(start));
			}
		}
		
		return spaceLeft;
	}
}
