package datamanager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import util.RomUtils;

public class AllocatableBank 
{
	byte bank;
	List<AllocatableSpace> spaces;
	TreeMap<Byte, List<MoveableBlock>> allocationsByPriority;
	
	int largestSpace;
	int largestFreeSpace;
	
	public AllocatableBank(byte bank)
	{
		this.bank = bank;
		spaces = new LinkedList<>();
		allocationsByPriority = new TreeMap<>();
		largestSpace = 0;
		largestFreeSpace = 0;
	}
	
	public void addSpace(int startAddress, int stopAddress)
	{
		addSpace(new AllocatableSpace(startAddress, stopAddress));
	}
	
	public void addSpace(AddressRange space)
	{
		addSpace(new AllocatableSpace(space));
	}
	
	private void addSpace(AllocatableSpace space)
	{
		if (!RomUtils.isInBank(space.start, bank) || RomUtils.isInBank(space.stopExclusive, bank))
		{
			throw new IllegalArgumentException("Passed space is not entirely in this bank (" + bank + ")!" +
					" bank addresses: " + RomUtils.getBankBounds(bank).toString() + " and space addresses: " +
					space.start + ", " + space.stopExclusive);
		}
		
		if (space.size() > largestSpace)
		{
			largestSpace = space.size();
		}
		spaces.add(space);
	}
	
	public int getLargestSpace()
	{
		return largestSpace;
	}
	
	public int getLargestFreeSpace()
	{
		return largestFreeSpace;
	}
	
	public boolean attemptToAdd(MoveableBlock alloc)
	{
		return attemptToAdd(alloc, false, null);
	}

	// Only (potentially) modifies blocks to alloc if true is returned
	public boolean attemptToAdd(MoveableBlock alloc, List<FloatingBlock> blocksToAlloc)
	{
		return attemptToAdd(alloc, true, blocksToAlloc);
	}

	// Only (potentially) modifies blocks to alloc if true is returned
	private boolean attemptToAdd(MoveableBlock alloc, boolean attemptToShrinkOthers, List<FloatingBlock> blocksToAlloc)
	{
		if (largestFreeSpace < alloc.getCurrentWorstCaseSizeOnBank(bank))
		{
			List<MoveableBlock> shrunkBlocks = new LinkedList<>();
			if (attemptToShrinkOthers && !shrinkToMakeSpace(alloc.getCurrentWorstCaseSizeOnBank(bank), shrunkBlocks, blocksToAlloc))
			{
				unshrinkAllTempShrunkAllocs(shrunkBlocks);				
				return false;
			}
		}
		
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		if (!reassignAndRefresh())
		{
			throw new RuntimeException("Failed to assign addresses! This should never happen here (attemptToAdd)!");
		}
		return true;
	}
	
	// Maybe add a priority as an optimization?
	private boolean reassignAndRefresh()
	{
		// Clear the spaces and largest space data
		largestFreeSpace = 0;
		for (AllocatableSpace space : spaces)
		{
			space.clear(true);
		}
		
		// Attempt to place each block
		boolean placed;
		for (List<MoveableBlock> allocWithPriority : allocationsByPriority.values())
		{
			// For each block, we go through each space and see if there is room until
			// we either find room or run out of spaces
			for (MoveableBlock alloc : allocWithPriority)
			{
				placed = false;
				for (AllocatableSpace space : spaces)
				{
					if (space.addAndAssignAddressIfSpaceLeft(alloc))
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

		// Now calculate the free space we have
		int openSpace;
		for (AllocatableSpace space : spaces)
		{
			openSpace = space.spaceLeft();
			if (openSpace > largestFreeSpace)
			{
				largestFreeSpace = openSpace;
			}
		}
		
		return true;
	}
	
	public byte canShrinkingToMakeSpace(int space)
	{
		byte priorityValue = -1;
		List<MoveableBlock> shrunkenAllocs = new LinkedList<>();
		List<FloatingBlock> toAlloc = new LinkedList<>();
		if (shrinkToMakeSpace(space, shrunkenAllocs, toAlloc))
		{
			if (shrunkenAllocs.isEmpty())
			{
				// No need to unshrink since none where shrunk
				return Byte.MAX_VALUE;
			}
			else
			{
				// Last added will be the highest priority
				priorityValue = shrunkenAllocs.get(shrunkenAllocs.size()).getPriority();
			}
		}
		
		// Revert the changes and refresh so we leave in the same state we started
		unshrinkAllTempShrunkAllocs(shrunkenAllocs);
		return priorityValue;
	}

	private void unshrinkAllTempShrunkAllocs(List<MoveableBlock> shrunkenAllocs)
	{
		for (MoveableBlock alloc : shrunkenAllocs)
		{
			alloc.setShrunkOrMoved(false);
			
			// If it was moved, add it back in
			if (alloc.movesNotShrinks())
			{
				DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
			}
		}
		
		if (!reassignAndRefresh())
		{
			throw new RuntimeException("Failed to assign addresses! This should never happen here (unshrinkAllTempShrunkAllocs)!");
		}
	}

	// Only (potentially) modifies blocks to alloc if true is returned
	private boolean shrinkToMakeSpace(int space, List<MoveableBlock> shrunkenAllocsByReversePriority, List<FloatingBlock> blocksToAllocate)
	{
		// Create a separate list so the passed list is only modified if true is returned
		List<FloatingBlock> runningToAlloc = new LinkedList<>();
		
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
					// Shrink it and add it to our list
					alloc.setShrunkOrMoved(true);
					shrunkenAllocsByReversePriority.add(alloc);
					
					// Get the remote block that will need to be allocated
					if (alloc.getRemoteBlock() != null)
					{
						blocksToAllocate.add(alloc.getRemoteBlock());
						
						// If it moves, we need to remove it from this block too
						if (alloc.movesNotShrinks())
						{
							revIterator.remove();
						}
					}
					
					// See if we have space now
					if (!reassignAndRefresh())
					{
						throw new RuntimeException("Failed to assign addresses! This should never happen here (shrinkToMakeSpace)!");
					}
					if (largestFreeSpace >= space)
					{			
						// Add the list prior to returning now that we know
						// we are successful
						blocksToAllocate.addAll(runningToAlloc);
						return true;
					}
				}				
			}
		}
		
		return false;
	}

	public byte getBank() 
	{
		return bank;
	}
}
