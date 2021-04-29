package datamanager;

import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

class AllocatableSpace extends AddressRange
{
	TreeMap<Byte, List<AllocData>> allocationsByPriority;
	
	public AllocatableSpace(int start, int stop)
	{
		super(start, stop);
		allocationsByPriority = new TreeMap<>();
	}
	
	public List<AllocData> add(AllocData alloc)
	{
		if (alloc.getCurrentSize() > spaceLeft())
		{
			if (!shrinkToMakeSpace(alloc))
			{
				throw new RuntimeException("Internal error - fialed to make space for new alloc");
			}
		}
		
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
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
		
		// Sanity/tamper check
		if (spaceLeft < 0)
		{
			throw new RuntimeException("More space was used than was available in the space!");
		}
		
		return spaceLeft;
	}
	
	public byte canShrinkingToMakeSpace(int space)
	{
		int availSpace = spaceLeft();
		for (Entry<Byte, List<AllocData>> allocWithPriority : allocationsByPriority.descendingMap().entrySet())
		{
			for (AllocData alloc : allocWithPriority.getValue())
			{
				availSpace += alloc.getCurrentSize() - alloc.getMinimalSize();
				if (availSpace >= space)
				{
					return allocWithPriority.getKey();
				}
			}
		}
		
		return 0;
	}
	
	private boolean shrinkToMakeSpace(AllocData alloc)
	{
		// TODO
		return false;
	}
}
