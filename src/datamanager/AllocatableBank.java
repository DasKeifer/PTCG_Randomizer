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
			boolean containedInSpace = false;
			fixedRange = new AddressRange(block.getFixedAddress() - bankRange.start, block.getFixedAddress() + block.getWorstCaseSize(allocIndexes) - bankRange.start);
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
				throw new RuntimeException("TODO!");
			}
			
			// If we have a new range to add, do so
			if (splitRange != null)
			{
				spacesLeft.add(splitRange);
			}
		}
		
		// Getting here means we found space for every fixed alloc
		return spacesLeft;
	}
	
	public void addMoveableBlockAndSetBank(MoveableBlock alloc, AllocatedIndexes allocIndexes)
	{
		priortizedAllocations.add(alloc);
		
		// Add each of the segments to this bank
		for (String segId : alloc.getSegmentsById().keySet())
		{
			allocIndexes.addSetBank(segId, bank);
		}
    }

	public boolean checkForAndRemoveExcessAllocs(List<MoveableBlock> allocsThatDontFit, AllocatedIndexes allocIndexes)
	{
		// Clear the spaces and output var
		// Clear just the addresses but keep the banks since they still are
		// here so we can get a better idea of the size
		allocsThatDontFit.clear();

		// Get the spaces that are left after the fixed block spaces are removed
		List<AddressRange> spacesLeft = getSpacesLeftRemovingFixedAllocs(allocIndexes);
		
		// Ensure the allocations are sorted
		priortizedAllocations.sort(MoveableBlock.PRIORITY_SORTER);
		
		return checkForAndRemoveExcessAllocsInCollection(priortizedAllocations.iterator(), spacesLeft, allocIndexes, allocsThatDontFit);
	}

	// TODO: Probably can optimize packing into bank space some (i.e. leave most space, leave smallest space)
	private boolean checkForAndRemoveExcessAllocsInCollection(Iterator<MoveableBlock> allocItr, List<AddressRange> spacesLeft, AllocatedIndexes allocIndexes, List<MoveableBlock> allocsThatDontFit)
	{
		boolean placed;
		MoveableBlock alloc;
		int allocSize;
		while (allocItr.hasNext())
		{
			// For each block, we go through each space and see if there is room until
			// we either find room or run out of spaces
			alloc = allocItr.next();
			placed = false;
			allocSize = alloc.dataBlock.getWorstCaseSize(allocIndexes);
			for (AddressRange space : spacesLeft)
			{
				if (space.size() <= allocSize)
				{
					space.start += allocSize;
					placed = true;
				}
			}
			
			if (!placed)
			{
				// remove each of the segments from this bank
				for (String segId : alloc.getSegmentsById().keySet())
				{
					allocIndexes.remove(segId);
				}
				allocsThatDontFit.add(alloc);
				allocItr.remove();
			}
		}
		
		return allocsThatDontFit.isEmpty();
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
			
			// TODO: need to also include segments in here... Also need to do this for fixed blocks.
			// Need to decide how optimized I want to be with JRs
			// Idea: Go through and add address to allocIndexes. Then go through again and check length
			// Against what was done. If changed, redo and keep going until stable. This will catch
			// the JRs shortening things
			// Actually maybe consider changing linking so we only need the datablock address? Rest
			// is done relative to that and stored internally?
			
			allocSize = block.dataBlock.getWorstCaseSize(allocIndexes);
			for (AddressRange space : spacesLeft)
			{
				if (space.size() <= allocSize)
				{
					allocIndexes.setAddressInBank(block.getId(), (short) space.start);
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
