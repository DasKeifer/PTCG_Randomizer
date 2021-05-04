package datamanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class AllocatableBank 
{
	byte bank;
	List<AllocatableSpace> spaces;
	TreeMap<Byte, List<MoveableBlock>> allocationsByPriority;
	
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
	
	public List<MoveableBlock> add(MoveableBlock alloc)
	{
		List<MoveableBlock> displacedAllocs = new LinkedList<>();
		if (!doesHaveSpaceLeft(alloc.getCurrentSizeOnBank(bank)))
		{
			displacedAllocs = shrinkToMakeSpace(alloc.getCurrentSizeOnBank(bank));
		}
		
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		return displacedAllocs;
	}
	
	public void removeBlock(String Id)
	{
		// Set the bank in the code snippet. Then we determine jump/call vs farjump/call
		for (List<MoveableBlock> allocsWithPriority : allocationsByPriority.values())
		{
			Iterator<MoveableBlock> iter = allocsWithPriority.iterator();
			while (iter.hasNext())
			{
				if (iter.next().getId().equalsIgnoreCase(Id))
				{
					iter.remove();
					return;
				}
			}
		}
	}
	
	public void assignBanks()
	{
		// Set the bank in the code snippet. Then we determine jump/call vs farjump/call
		for (List<MoveableBlock> allocsWithPriority : allocationsByPriority.values())
		{
			for (MoveableBlock alloc : allocsWithPriority)
			{
				alloc.setAssignedBank(bank);
			}
		}
	}
	
	public List<MoveableBlock> adjustAllocs()
	{
		List<MoveableBlock> blocksToRemove = new LinkedList<>();
		
		// Try to unpack allocs. If we can, then we remove the remote call which then
		// may trigger more unpacks
		List<Byte> priorityKeys = new ArrayList<>(allocationsByPriority.keySet());
		for (int priorityIdx = 0; priorityIdx < priorityKeys.size(); priorityIdx++)
		{
			List<MoveableBlock> allocsWithPriority = allocationsByPriority.get(priorityKeys.get(priorityIdx));
			for (int allocIdx = 0; allocIdx < allocsWithPriority.size(); allocIdx++)
			{
				MoveableBlock alloc = allocsWithPriority.get(allocIdx);
				
				// Only try to unshrink if it was shrunk
				if (alloc.shrinksNotMoves() && alloc.isShrunkOrMoved())
				{
					alloc.setShrunkOrMoved(false);
					if (reassignToSpaces())
					{
						FloatingBlock removed = alloc.revertShrink();
						
						// Check to see if we need to restart our iteration because the removed
						// block was a higher priority block in this bank
						if (removed.getAssignedBank() == bank)
						{
							// Remove it from this bank
							removeBlock(removed.getId());
							if (removed.getPriority() >= 0 && removed.getPriority() <= priorityKeys.get(priorityIdx))
							{
								priorityKeys = new ArrayList<>(allocationsByPriority.keySet());
								priorityIdx = 0;
							}
						}
						else
						{
							// Needs to be removed from elsewhere
							blocksToRemove.add(removed);
						}
					}
					// Couldn't expand
					else
					{
						alloc.setShrunkOrMoved(true);
					}
				}
			}
		}
		
		return blocksToRemove;
	}
	
	public void assignAddresses(Map<String, Integer> blockIdsToAddresses)
	{
		// Now we do the final space assignment and give out actual addresses to the bank
		reassignToSpaces();

		for (AllocatableSpace space : spaces)
		{
			space.assignAddresses(blockIdsToAddresses);
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
		for (List<MoveableBlock> allocWithPriority : allocationsByPriority.values())
		{
			for (MoveableBlock alloc : allocWithPriority)
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
		List<MoveableBlock> shrunkenAllocs = new LinkedList<>();
		byte priorityValue = shrinkToMakeSpace(space, shrunkenAllocs);
		
		// Always revert the changes
		unshrinkAllAllocs(shrunkenAllocs);
		return priorityValue;
	}
	
	private List<MoveableBlock> shrinkToMakeSpace(int space)
	{
		List<MoveableBlock> shrunkenAllocs = new LinkedList<>();
		
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
		List<MoveableBlock> allocsToPlace = new LinkedList<>();
		for (MoveableBlock alloc : shrunkenAllocs)
		{
			if (alloc.shrinksNotMoves())
			{
				allocsToPlace.add(alloc.applyShrink());
			}
			else
			{
				alloc.setShrunkOrMoved(false);
				allocsToPlace.add(alloc);
			}
		}
		
		return shrunkenAllocs;
	}
	
	private byte shrinkToMakeSpace(int space, List<MoveableBlock> shrunkenAllocsByReversePriority)
	{
		List<List<MoveableBlock>> allocByPriority = new ArrayList<>(allocationsByPriority.descendingMap().values());
		ListIterator<MoveableBlock> revIterator;
		MoveableBlock alloc;
		
		for (List<MoveableBlock> allocWithPriority : allocByPriority)
		{
			revIterator = allocWithPriority.listIterator(allocWithPriority.size());
			while (revIterator.hasPrevious())
			{
				alloc = revIterator.previous();
				if (alloc.canBeShrunkOrMoved())
				{
					alloc.setShrunkOrMoved(true);
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
	
	private void unshrinkAsMuchAsPossible(int space, List<MoveableBlock> shrunkenAllocsByReversePriority)
	{
		MoveableBlock toProcess;
		ListIterator<MoveableBlock> revIterator = shrunkenAllocsByReversePriority.listIterator(shrunkenAllocsByReversePriority.size());
		while (revIterator.hasPrevious())
		{
			toProcess = revIterator.previous();
			toProcess.setShrunkOrMoved(false);
			
			// If there is no longer space, then we can't unshrink this one
			if (!doesHaveSpaceLeft(space))
			{
				toProcess.setShrunkOrMoved(true);
			}
			else
			{
				revIterator.remove();
			}
		}
	}

	private void unshrinkAllAllocs(List<MoveableBlock> shrunkenAllocs)
	{
		for (MoveableBlock alloc : shrunkenAllocs)
		{
			alloc.setShrunkOrMoved(false);
		}
	}

	public byte getBank() 
	{
		return bank;
	}
}
