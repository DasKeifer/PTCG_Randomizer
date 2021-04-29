package datamanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

public class AllocatableBank 
{
	List<AllocatableSpace> spaces;
	TreeMap<Byte, List<AllocData>> allocationsByPriority;
	
	public AllocatableBank()
	{
		spaces = new LinkedList<>();
	}
	
	public void addSpace(int startAddress, int stopAddress)
	{
		spaces.add(new AllocatableSpace(startAddress, stopAddress));
	}
	
	public void addSpace(AddressRange space)
	{
		spaces.add(new AllocatableSpace(space));
	}
	
	public List<AllocData> add(AllocData alloc)
	{
		List<AllocData> displacedAllocs = new LinkedList<>();
		if (!doesHaveSpaceLeft(alloc.getCurrentSize()))
		{
			displacedAllocs = shrinkToMakeSpace(alloc.getCurrentSize());
		}
		
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		return displacedAllocs;
	}

	private int largestSpaceLeft()
	{
		// Clear the spaces
		for (AllocatableSpace space : spaces)
		{
			space.clear();
		}
		
		// Go through an allocate each to a space
		for (List<AllocData> allocWithPriority : allocationsByPriority.values())
		{
			for (AllocData alloc : allocWithPriority)
			{
				for (AllocatableSpace space : spaces)
				{
					if (space.addIfSpaceLeft(alloc))
					{
						break;
					}
				}
			}
		}
		
		// Now go through each space and find the largest
		int largest = 0;
		int openSpace;
		for (AllocatableSpace space : spaces)
		{
			openSpace = space.spaceLeft();
			if (openSpace > largest)
			{
				largest = openSpace;
			}
		}
		
		return largest;
	}
	
	public boolean doesHaveSpaceLeft(int spaceToFind)
	{
		return largestSpaceLeft() >= spaceToFind;
	}
	
	public byte canShrinkingToMakeSpace(int space)
	{
		List<AllocData> shrunkenAllocs = new LinkedList<>();
		byte priorityValue = shrinkToMakeSpace(space, shrunkenAllocs);
		
		// Always revert the changes
		unshrinkAllAllocs(shrunkenAllocs);
		return priorityValue;
	}
	
	private List<AllocData> shrinkToMakeSpace(int space)
	{
		List<AllocData> shrunkenAllocs = new LinkedList<>();
		
		// If we failed, revert the changes
		if (shrinkToMakeSpace(space, shrunkenAllocs) < 0)
		{
			throw new RuntimeException("Failed to make space!");
		}
		
		// Otherwise, try to unshrink as much as possible
		unshrinkAsMuchAsPossible(space, shrunkenAllocs);
		
		// Remove any that were shrunken but still present so only the ones
		// shrunken out of existence are here
		ListIterator<AllocData> iter = shrunkenAllocs.listIterator();
		while (iter.hasNext())
		{
			if (iter.next().getCurrentSize() != 0)
			{
				iter.remove();
			}
		}
		
		return shrunkenAllocs;
	}
	
	private byte shrinkToMakeSpace(int space, List<AllocData> shrunkenAllocsByReversePriority)
	{
		List<List<AllocData>> allocByPriority = new ArrayList<>(allocationsByPriority.descendingMap().values());
		ListIterator<AllocData> revIterator;
		AllocData alloc;
		
		for (List<AllocData> allocWithPriority : allocByPriority)
		{
			revIterator = allocWithPriority.listIterator(allocWithPriority.size());
			while (revIterator.hasPrevious())
			{
				alloc = revIterator.previous();
				if (alloc.canBeShrunk())
				{
					alloc.shrunk = true;
					shrunkenAllocsByReversePriority.add(alloc);
					
					if (doesHaveSpaceLeft(space))
					{						
						return alloc.getPriority();
					}
				}				
			}
		}
		
		return -1;
	}
	
	private void unshrinkAsMuchAsPossible(int space, List<AllocData> shrunkenAllocsByReversePriority)
	{
		AllocData toProcess;
		ListIterator<AllocData> revIterator = shrunkenAllocsByReversePriority.listIterator(shrunkenAllocsByReversePriority.size());
		while (revIterator.hasPrevious())
		{
			toProcess = revIterator.previous();
			toProcess.shrunk = false;
			
			// If there is no longer space, then we can't unshrink this one
			if (!doesHaveSpaceLeft(space))
			{
				toProcess.shrunk = true;
			}
			else
			{
				revIterator.remove();
			}
		}
	}

	private void unshrinkAllAllocs(List<AllocData> shrunkenAllocs)
	{
		for (AllocData alloc : shrunkenAllocs)
		{
			alloc.shrunk = false;
		}
	}
}
