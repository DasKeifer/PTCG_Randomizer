package datamanager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import util.RomUtils;

public class AllocatableBank 
{
	byte bank;
	AddressRange bankRange;
	List<AddressRange> spaces;
	List<FixedBlock> fixedAllocations;
	// We don't use a set because we modify allocation and its bad practice to
	// do that for items in a set even if it should not impact the compare function
	List<MoveableBlock> priortizedAllocations;
	
	public AllocatableBank(byte bank)
	{
		this.bank = bank;
		int[] bankBounds = RomUtils.getBankBounds(bank);
		// + 1 because it is exclusive
		bankRange = new AddressRange(bankBounds[0], bankBounds[1] + 1);
		spaces = new LinkedList<>();
		fixedAllocations = new LinkedList<>();
		priortizedAllocations = new LinkedList<>();
	}
	
	public void addSpace(int startAddress, int stopAddress)
	{
		addSpace(new AddressRange(startAddress, stopAddress));
	}
	
	public void addSpace(AddressRange space)
	{
		// If the space isn't entirely in this banks range, then throw
		if (!bankRange.contains(space))
		{
			// TODO: more cases where I need to use Arrays.toString
			throw new IllegalArgumentException("Passed space is not entirely in this bank (" + bank + ")!" +
					" bank addresses: " + Arrays.toString(RomUtils.getBankBounds(bank)) + " and space addresses: " +
					space.start + ", " + space.stopExclusive);
		}
		// shift it to be relative to this bank and add it
		spaces.add(new AddressRange(space.start - bankRange.start, space.stopExclusive - bankRange.start));
	}

	// Manually remove a space. Removing for fixed blocks should be done
	// via the addToBank function
	public void removeAddressSpace(AddressRange range) 
	{		
		// If this space doesn't overlap with this bank, then there is nothing to do
		if (bankRange.overlaps(range))
		{
			// Convert it relative to this bank
			AddressRange bankRelative = new AddressRange(range.start - bankRange.start, range.stopExclusive - bankRange.start);
			
			// Then go through and remove the overlap potentially adding a new space in if the removed space is in the middle
			// of an existing space
			for (int spaceIdx = 0; spaceIdx < spaces.size(); spaceIdx++)
			{
				AddressRange space = spaces.get(spaceIdx);
				AddressRange otherSplit = space.removeOverlap(bankRelative);
				
				if (space.isEmpty())
				{
					spaces.remove(spaceIdx--);
				}
				else if (otherSplit != null)
				{
					// Add it and skip it since we already checked it. We add it after
					// because the function will shorten the address its called on to
					// the first of the two spaces and return the "higher" of the two
					spaces.add(spaceIdx++, new AddressRange(otherSplit));
				}
			}
		}
	}
	
	public void addFixedBlock(FixedBlock fixedAlloc, AllocatedIndexes allocIndexes)
	{
		fixedAllocations.add(fixedAlloc);
		// Ensure there is space for this fixed alloc. This will throw if there is not space
		getSpacesLeftRemovingFixedAllocs(allocIndexes);
	}
	
	private List<AddressRange> getSpacesLeftRemovingFixedAllocs(AllocatedIndexes allocIndexes)
	{
		List<AddressRange> spacesLeft = new LinkedList<>();
		for (AddressRange range : spaces)
		{
			spacesLeft.add(new AddressRange(range));
		}
		
		AddressRange fixedRange;
		AddressRange splitRange = null;
		for (FixedBlock block : fixedAllocations)
		{
			// If its a replacement block, we might not have space for it since its overwritting
			// other code
			if (block instanceof ReplacementBlock)
			{
				continue;
			}
			
			boolean containedInSpace = false;
			fixedRange = new AddressRange(block.getFixedAddress().addressInBank, block.getFixedAddress().addressInBank + block.getWorstCaseSize(allocIndexes));
			for (AddressRange spaceLeft : spacesLeft)
			{
				// If it is contained in the space, we found where it lives. We 
				// need to remove it from the space and potentially add a new
				// space to the list
				if (spaceLeft.contains(fixedRange))
				{
					splitRange = spaceLeft.removeOverlap(fixedRange);
					containedInSpace = true;
					break;
				}
			}
			
			// If we didn't find any space that fits this, then we cannot put it here so we
			// abort
			if (!containedInSpace)
			{
				throw new RuntimeException(String.format("There was not space from 0x%x to 0x%x in bank 0x%x for FixedBlock %s - "
						+ "only ReplacementBlocks do not need free space in the bank", block.getFixedAddress().addressInBank, 
						block.getFixedAddress().addressInBank + block.getWorstCaseSize(allocIndexes), bank, block.getId()));
			}
			
			// If we have a new range to add, do so
			if (splitRange != null)
			{
				spacesLeft.add(splitRange);
			}
		}
		
		// We still need to remove the overlap for the replacement blocks if there is any though
		for (FixedBlock block : fixedAllocations)
		{
			// Non replacement blocks have already been handled
			if (!(block instanceof ReplacementBlock))
			{
				continue;
			}
			
			fixedRange = new AddressRange(block.getFixedAddress().addressInBank, block.getFixedAddress().addressInBank + block.getWorstCaseSize(allocIndexes));
			for (AddressRange spaceLeft : spacesLeft)
			{
				splitRange = spaceLeft.removeOverlap(fixedRange);
				// If we have a new range to add, do so
				if (splitRange != null)
				{
					spacesLeft.add(splitRange);
				}
			}			
		}
		
		// Getting here means we found space for every fixed alloc
		return spacesLeft;
	}
	
	public void addMoveableBlock(MoveableBlock alloc)
	{
		priortizedAllocations.add(alloc);
    }

	public boolean checkForAndRemoveExcessAllocs(List<MoveableBlock> allocsThatDontFit, AllocatedIndexes allocIndexes)
	{
		// Clear the spaces and output var
		// Clear just the addresses but keep the banks since they still are
		// here so we can get a better idea of the size
		allocsThatDontFit.clear();
		
		// Ensure the allocations are sorted
		priortizedAllocations.sort(MoveableBlock.PRIORITY_SORTER);
		
		return checkForAndRemoveExcessAllocsInCollection(priortizedAllocations.iterator(), allocIndexes, allocsThatDontFit);
	}

	// TODO: Probably can optimize packing into bank space some (i.e. leave most space, leave smallest space)
	private boolean checkForAndRemoveExcessAllocsInCollection(Iterator<MoveableBlock> allocItr, AllocatedIndexes allocIndexes, List<MoveableBlock> allocsThatDontFit)
	{
		boolean stable = false;
		boolean placed;
		MoveableBlock alloc;
		int allocSize;
		List<AddressRange> spacesLeft;
		while (!stable)
		{
			// start assuming this time we are good
			stable = true;
		
			// Reassign the fixed block addresses in case it can shrink based on where the movable blocks are allocated
			for (FixedBlock block : fixedAllocations)
			{
				DataManagerUtils.assignBlockAndSegmentBankAddresses(block, block.getFixedAddress(), allocIndexes);
			}
			
			// Now get the spaces that are left after the fixed block spaces are removed
			spacesLeft = getSpacesLeftRemovingFixedAllocs(allocIndexes);
			
			// Loop over each movable block and see if it fits
			while (allocItr.hasNext())
			{
				// For each block, we go through each space and see if there is room until
				// we either find room or run out of spaces
				alloc = allocItr.next();
				placed = false;
				allocSize = alloc.getWorstCaseSize(allocIndexes);
				for (AddressRange space : spacesLeft)
				{
					if (allocSize <= space.size())
					{
						space.start += allocSize;
						placed = true;
					}
				}
				
				if (!placed)
				{
					// We removed one - the list ins't stable now
					// We need another pass to see if anything else no longer
					// will fit
					stable = false;
					DataManagerUtils.removeBlockAndSegmentAddresses(alloc, allocIndexes);
					allocsThatDontFit.add(alloc);
					allocItr.remove();
				}
			}
		}
		
		return !allocsThatDontFit.isEmpty();
	}

	public void assignAddresses(AllocatedIndexes allocIndexes) 
	{
		// Get the spaces that are left after the fixed block spaces are removed
		List<AddressRange> spacesLeft = getSpacesLeftRemovingFixedAllocs(allocIndexes);
		
		boolean placed;
		int allocSize;
		
		for (MoveableBlock block : priortizedAllocations)
		{
			placed = false;
			allocSize = block.getWorstCaseSize(allocIndexes);
			for (AddressRange space : spacesLeft)
			{
				if (allocSize <= space.size())
				{
					DataManagerUtils.assignBlockAndSegmentBankAddresses(block, new BankAddress(bank, (short) space.start), allocIndexes);
					space.start += allocSize;
					placed = true;
				}
			}
			
			if (!placed)
			{
				throw new RuntimeException("TODO!");
			}
		}
	}

	public byte getBank() 
	{
		return bank;
	}
}
