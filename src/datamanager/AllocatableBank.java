package datamanager;

import java.util.ArrayList;
import java.util.Arrays;
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
	
//	int largestSpace;
//	int largestFreeSpace;
	
	public AllocatableBank(byte bank)
	{
		this.bank = bank;
		spaces = new LinkedList<>();
		allocationsByPriority = new TreeMap<>();
//		largestSpace = 0;
//		largestFreeSpace = 0;
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
		// -1 since the stop is exclusive so it checks the last included byte
		if (!RomUtils.isInBank(space.start, bank) || !RomUtils.isInBank(space.stopExclusive - 1, bank))
		{
			// TODO: more cases where I need to use Arrays.toString
			throw new IllegalArgumentException("Passed space is not entirely in this bank (" + bank + ")!" +
					" bank addresses: " + Arrays.toString(RomUtils.getBankBounds(bank)) + " and space addresses: " +
					space.start + ", " + space.stopExclusive);
		}
		
//		if (space.size() > largestSpace)
//		{
//			largestSpace = space.size();
//		}
//		if (space.spaceLeft() > largestFreeSpace)
//		{
//			largestFreeSpace = space.spaceLeft();
//		}
		spaces.add(space);
	}

	public void removeAddressSpace(AddressRange range) 
	{
		for (int spaceIdx = 0; spaceIdx < spaces.size(); spaceIdx++)
		{
			AllocatableSpace space = spaces.get(spaceIdx);
			AddressRange otherSplit = space.removeOverlap(range);
			
			if (space.isEmpty())
			{
				spaces.remove(spaceIdx--);
			}
			else if (otherSplit != null)
			{
				spaces.add(spaceIdx++, new AllocatableSpace(otherSplit));
			}
		}
	}
	
	public int getLargestSpace()
	{
		int largestSpace = 0;
		for (AllocatableSpace space : spaces)
		{
			if (space.size() > largestSpace)
			{
				largestSpace = space.size();
			}
		}
		return largestSpace;
	}
	
	public int getLargestFreeSpace()
	{
		int largestFreeSpace = 0;
		for (AllocatableSpace space : spaces)
		{
			if (space.spaceLeft() > largestFreeSpace)
			{
				largestFreeSpace = space.spaceLeft();
			}
		}
		return largestFreeSpace;
	}
	
	public boolean attemptToAdd(MoveableBlock alloc)
	{
		return attemptToAdd(alloc, false, null);
	}

	// Only (potentially) modifies blocks to alloc if true is returned
	public boolean attemptToAdd(MoveableBlock alloc, List<UnconstrainedMoveBlock> blocksToAlloc)
	{
		return attemptToAdd(alloc, true, blocksToAlloc);
	}

	// Only (potentially) modifies blocks to alloc if true is returned
	private boolean attemptToAdd(MoveableBlock alloc, boolean attemptToShrinkOthers, List<UnconstrainedMoveBlock> blocksToAlloc)
	{
		if (getLargestFreeSpace() < alloc.getCurrentWorstCaseSizeOnBank(bank))
		{
			List<MoveableBlock> shrunkBlocks = new LinkedList<>();
			if (!attemptToShrinkOthers)
			{
				return false;
			}
			else if (!shrinkToMakeSpace(alloc.getCurrentWorstCaseSizeOnBank(bank), shrunkBlocks, blocksToAlloc))
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
		// Clear the spaces
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
		
		return true;
	}
	
	public byte canShrinkingToMakeSpace(int space)
	{
		byte priorityValue = -1;
		List<MoveableBlock> shrunkenAllocs = new LinkedList<>();
		List<UnconstrainedMoveBlock> toAlloc = new LinkedList<>();
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
	private boolean shrinkToMakeSpace(int space, List<MoveableBlock> shrunkenAllocsByReversePriority, List<UnconstrainedMoveBlock> blocksToAllocate)
	{
		// Create a separate list so the passed list is only modified if true is returned
		List<UnconstrainedMoveBlock> runningToAlloc = new LinkedList<>();
		
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
					if (getLargestFreeSpace() >= space)
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
