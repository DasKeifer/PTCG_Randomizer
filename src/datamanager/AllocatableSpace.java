package datamanager;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import util.RomUtils;

class AllocatableSpace extends AddressRange
{
	TreeMap<Byte, List<MoveableBlock>> allocationsByPriority;
	
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
	
	public boolean addIfSpaceLeft(MoveableBlock alloc)
	{
		if (alloc.getCurrentSizeOnBank(RomUtils.determineBank(start)) > spaceLeft())
		{
			return false;
		}
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		return true;
	}
	
	public void assignAddresses(Map<String, Integer> blockIdsToAddresses)
	{
		int nextStart = start;
		for (List<MoveableBlock> allocWithPriority : allocationsByPriority.values())
		{
			for (MoveableBlock alloc : allocWithPriority)
			{
				// Assign the location
				blockIdsToAddresses.put(alloc.getId(), nextStart);
				
				// TODO: assign label addresses - pass in a set to the block for it to add to
				
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
		for (List<MoveableBlock> allocWithPriority : allocationsByPriority.values())
		{
			for (MoveableBlock alloc : allocWithPriority)
			{
				spaceLeft -= alloc.getCurrentSizeOnBank(RomUtils.determineBank(start));
			}
		}
		
		return spaceLeft;
	}
}
