package datamanager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import util.RomUtils;

public class AllocatableBank 
{
	byte bank;
	List<AllocatableSpace> spaces;
	// We don't use a set because we modify allocation and its bad practice to
	// do that for items in a set even if it should not impact the compare function
	List<Allocation> priortizedAllocations;
	
	public AllocatableBank(byte bank)
	{
		this.bank = bank;
		spaces = new LinkedList<>();
		priortizedAllocations = new LinkedList<>();
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
		spaces.add(space);
	}

	// For fixed blocks
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
	
//	public int getLargestSpace()
//	{
//		int largestSpace = 0;
//		for (AllocatableSpace space : spaces)
//		{
//			if (space.size() > largestSpace)
//			{
//				largestSpace = space.size();
//			}
//		}
//		return largestSpace;
//	}
//	
//	public int getLargestFreeSpace()
//	{
//		int largestFreeSpace = 0;
//		for (AllocatableSpace space : spaces)
//		{
//			if (space.spaceLeft() > largestFreeSpace)
//			{
//				largestFreeSpace = space.spaceLeft();
//			}
//		}
//		return largestFreeSpace;
//	}
	
//	public boolean attemptToAdd(MoveableBlock alloc)
//	{
//		return attemptToAdd(alloc, false, null);
//	}
//
//	// Only (potentially) modifies blocks to alloc if true is returned
//	public boolean attemptToAdd(MoveableBlock alloc, List<UnconstrainedMoveBlock> blocksToAlloc)
//	{
//		return attemptToAdd(alloc, true, blocksToAlloc);
//	}

	// Only (potentially) modifies blocks to alloc if true is returned
//	private boolean attemptToAdd(MoveableBlock alloc, boolean attemptToShrinkOthers, List<UnconstrainedMoveBlock> blocksToAlloc)
//	{
//		if (getLargestFreeSpace() < alloc.getCurrentWorstCaseSizeOnBank(bank))
//		{
//			List<MoveableBlock> shrunkBlocks = new LinkedList<>();
//			if (!attemptToShrinkOthers)
//			{
//				return false;
//			}
//			else if (!shrinkToMakeSpace(alloc.getCurrentWorstCaseSizeOnBank(bank), shrunkBlocks, blocksToAlloc))
//			{
//				unshrinkAllTempShrunkAllocs(shrunkBlocks);				
//				return false;
//			}
//		}
//		
//		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
//		if (!reassignAndRefresh())
//		{
//			throw new RuntimeException("Failed to assign addresses! This should never happen here (attemptToAdd)!");
//		}
//		return true;
//	}
	
	public void addToBank(Allocation alloc)
	{
		alloc.setAssignedBank(bank);
		priortizedAllocations.add(alloc);
    }

	public boolean packAndRemoveExcessAllocs(List<Allocation> allocsThatDontFit, AllocatedIndexes allocIndexes)
	{
		return packAllocs(allocsThatDontFit, allocIndexes, true);
	}

	private boolean packAllocs(List<Allocation> allocsThatDontFit, AllocatedIndexes allocIndexes, boolean removeOnesThatDontFit)
	{
		// Clear the spaces and output var
		// Clear just the addresses but keep the banks since they still are
		// here so we can get a better idea of the size
		allocsThatDontFit.clear();
		for (AllocatableSpace space : spaces)
		{
			space.clearAllocsAndAddressToUnassignedLocal();
		}
		
		// Ensure the allocations are sorted
		priortizedAllocations.sort(Allocation.PRIORITY_SORTER);
		
		// Attempt to place each alloc in the list
		return packAllocsInCollection(priortizedAllocations.iterator(), allocIndexes, allocsThatDontFit, removeOnesThatDontFit);
	}

	// TODO: Probably can optimize packing into bank space some (i.e. leave most space, leave smallest space)
	private boolean packAllocsInCollection(Iterator<Allocation> allocItr, AllocatedIndexes allocIndexes, List<Allocation> allocsThatDontFit, boolean removeOnesThatDontFit)
	{
		boolean placed;
		Allocation alloc;
		while (allocItr.hasNext())
		{
			// For each block, we go through each space and see if there is room until
			// we either find room or run out of spaces
			alloc = allocItr.next();
			placed = false;
			for (AllocatableSpace space : spaces)
			{
				if (space.addAndAssignAddressIfSpaceLeft(alloc, allocIndexes))
				{
					placed = true;
					break;
				}
			}
			
			if (!placed)
			{
				if (removeOnesThatDontFit)
				{
					allocItr.remove();
					alloc.clearBankAndAddress();
				}
				allocsThatDontFit.add(alloc);
			}
		}
		
		return allocsThatDontFit.isEmpty();
	}

	public byte getBank() 
	{
		return bank;
	}
	
	public void clearAllAllocs() 
	{
		for (AllocatableSpace space : spaces)
		{
			space.clearAllocsAndAddressToUnassignedLocal();
		}
		
		// Now clear the banks
		for (Allocation alloc : priortizedAllocations)
		{
			alloc.clearBankAndAddress();
		}
		
		// Finally clear the list
		priortizedAllocations.clear();
	}

	public void unshrinkAsMuchAsPossible(List<Allocation> unshrunkAllocs, AllocatedIndexes allocIndexes) 
	{
		// For each allocation from highest priority to lowest, see if we can unshrink it
		List<Allocation> unusedList = new LinkedList<>();
		for (Allocation alloc : priortizedAllocations)
		{
			// If we can unshrink it, do so and then try to pack the allocations
			if (alloc.data.unshrinkIfPossible())
			{
				if (!packAllocs(unusedList, allocIndexes, false)) // False = don't remove excess
				{
					// If we didn't successfully pack, then we can't unshrink this one
					// so reshrink it but keep iterating because we might be able to 
					// fit other ones
					alloc.data.setShrunkOrMoved(true);
				}
			}
		}
	}
}
