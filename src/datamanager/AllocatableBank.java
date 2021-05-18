package datamanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import compiler.CompilerUtils;
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
	
	public boolean attemptToAdd(MoveableBlock alloc, List<MoveableBlock> displacedBlocks)
	{
		displacedBlocks.clear();
		return attemptToAdd(alloc, true, displacedBlocks);
	}
	
	private boolean attemptToAdd(MoveableBlock alloc, boolean attemptToShrinkOthers, List<MoveableBlock> displacedBlocks)
	{
		if (largestFreeSpace < alloc.getCurrentWorstCaseSizeOnBank(bank))
		{
			if (attemptToShrinkOthers && !shrinkToMakeSpace(alloc.getCurrentWorstCaseSizeOnBank(bank), displacedBlocks))
			{
				unshrinkAllTempShrunkAllocs(displacedBlocks);				
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
	
	public void removeBlock(String Id)
	{
		// Set the bank in the code snippet. Then we determine jump/call vs farjump/call
		Iterator<MoveableBlock> iter;
		MoveableBlock alloc;
		for (List<MoveableBlock> allocsWithPriority : allocationsByPriority.values())
		{
			iter = allocsWithPriority.iterator();
			while (iter.hasNext())
			{
				alloc = iter.next();
				if (alloc.getId().equalsIgnoreCase(Id))
				{
					alloc.setAssignedAddress(CompilerUtils.UNASSIGNED_ADDRESS);
					iter.remove();
					if (!reassignAndRefresh())
					{
						throw new RuntimeException("Failed to assign addresses! This should never happen here (removeBlock)!");
					}
					return;
				}
			}
		}
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
		if (shrinkToMakeSpace(space, shrunkenAllocs))
		{
			if (shrunkenAllocs.isEmpty())
			{
				// No need to unshrink since none where shrunk
				return Byte.MAX_VALUE;
			}
			else
			{
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
		}
		
		if (!reassignAndRefresh())
		{
			throw new RuntimeException("Failed to assign addresses! This should never happen here (unshrinkAllTempShrunkAllocs)!");
		}
	}

	private boolean shrinkToMakeSpace(int space, List<MoveableBlock> shrunkenAllocsByReversePriority)
	{
		shrunkenAllocsByReversePriority.clear();
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
					
					if (largestFreeSpace >= space)
					{						
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
