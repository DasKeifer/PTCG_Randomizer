package datamanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

public class AllocatableBank 
{
	byte bank;
	List<AllocatableSpace> spaces;
	TreeMap<Byte, List<AllocData>> allocationsByPriority;
	
	public AllocatableBank(byte bank)
	{
		this.bank = bank;
		spaces = new LinkedList<>();
	}
	
	public void addSpace(int startAddress, int stopAddress)
	{
		// TODO check in bank?
		spaces.add(new AllocatableSpace(startAddress, stopAddress));
	}
	
	public void addSpace(AddressRange space)
	{
		// TODO check in bank?
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
	
	public void assignBanks()
	{
		// Set the bank in the code snippet. Then we determine jump/call vs farjump/call
		for (List<AllocData> allocsWithPriority : allocationsByPriority.values())
		{
			for (AllocData alloc : allocsWithPriority)
			{
				alloc.setBank(bank);
			}
		}
	}
	
	public byte adjustAllocs()
	{
		byte earliestBankImpacted = Byte.MAX_VALUE;
		
		// Try to unpack allocs. If we can, then we remove the remote call which then
		// may trigger more unpacks
		List<Byte> priorityKeys = new ArrayList<>(allocationsByPriority.keySet());
		for (int priorityIdx = 0; priorityIdx < priorityKeys.size(); priorityIdx++)
		{
			List<AllocData> allocsWithPriority = allocationsByPriority.get(priorityKeys.get(priorityIdx));
			for (int allocIdx = 0; allocIdx < allocsWithPriority.size(); allocIdx++)
			{
				AllocData alloc = allocsWithPriority.get(allocIdx);
				
				alloc.shrunk = false;
				if (reassignToSpaces())
				{
					byte remoteAllocPriority = alloc.getRemoteAllocPriority();
					byte hostBank = alloc.removeRemoteAlloc();
					
					// If it has a higher priority in this bank, we need to reset our iterator
					// and see if it can be decompressed too
					if (hostBank == bank)
					{
						if (remoteAllocPriority >= 0 && remoteAllocPriority <= priorityKeys.get(priorityIdx))
						{
							priorityKeys = new ArrayList<>(allocationsByPriority.keySet());
							priorityIdx = 0;
						}
					}
					if (hostBank < earliestBankImpacted)
					{
						earliestBankImpacted = bank;
					}
				}
				// Couldn't unshrink - revert change and continue
				else
				{
					alloc.shrunk = true;
				}

			}
		}
		
		if (earliestBankImpacted < bank)
		{
			return earliestBankImpacted;
		}
		return -1;
	}
	
	public void assignAddresses()
	{
		// Now we do the final space assignment and give out actual addresses to the bank
		reassignToSpaces();

		for (AllocatableSpace space : spaces)
		{
			space.assignAddresses();
		}
	}
	
	private boolean reassignToSpaces()
	{
		// Clear the spaces
		for (AllocatableSpace space : spaces)
		{
			space.clear();
		}
		
		boolean placed;
		for (List<AllocData> allocWithPriority : allocationsByPriority.values())
		{
			for (AllocData alloc : allocWithPriority)
			{
				placed = false;
				for (AllocatableSpace space : spaces)
				{
					if (space.addIfSpaceLeft(alloc))
					{
						placed = true;
						break;
					}
				}
				
				if (!placed)
				{
					return false;
				}
			}
		}
		
		return true;
	}

	private int largestSpaceLeft()
	{
		// Go through an allocate each to a space
		if (!reassignToSpaces())
		{
			return -1;
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
		
		// Remove any that were shrunken and replace them with their dependencies
		// So the shrunken out of existence ones and the new blocks are
		// returned
		List<AllocData> allocsToPlace = new LinkedList<>();
		for (AllocData alloc : shrunkenAllocs)
		{
			if (alloc instanceof NoConstraintBlock)
			{
				alloc.shrunk = false;
				allocsToPlace.add(alloc);
			}
			else if (alloc instanceof ConstrainedBlock)
			{
				allocsToPlace.add(alloc.createRemoteAlloc());
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
