package datamanager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rom_addressing.AddressRange;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;
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
			throw new IllegalArgumentException("Passed space is not entirely in this bank (" + bank + ")!" +
					" bank addresses: " + Arrays.toString(RomUtils.getBankBounds(bank)) + " and space addresses: " +
					space.getStart() + ", " + space.getStopExclusive());
		}
		// shift it to be relative to this bank and add it
		spaces.add(space.shiftNew(-bankRange.getStart()));
	}

	// Manually remove a space. Removing for fixed blocks should be done
	// via the addToBank function
	public void removeAddressSpace(AddressRange range) 
	{		
		// If this space doesn't overlap with this bank, then there is nothing to do
		if (bankRange.overlaps(range))
		{
			// Convert it relative to this bank
			AddressRange bankRelative = range.shiftNew(-bankRange.getStart());
			
			// Then go through and remove the overlap potentially adding a new space in if the removed space is in the middle
			// of an existing space
			AddressRange space;
			Iterator<AddressRange> spaceItr = spaces.iterator();
			List<AddressRange> newRanges = new LinkedList<>();
			while (spaceItr.hasNext())
			{
				space = spaceItr.next();
				AddressRange otherSplit = space.removeOverlap(bankRelative);
				
				if (space.isEmpty())
				{
					spaceItr.remove();
				}
				else if (otherSplit != null)
				{
					// We don't need to add the split now since we already have checked the space it came from
					newRanges.add(otherSplit);
				}
			}
			
			// Add any new ranges that were created
			spaces.addAll(newRanges);
		}
	}
	
	public void addFixedBlock(FixedBlock fixedAlloc, AssignedAddresses assignedAddresses)
	{
		fixedAllocations.add(fixedAlloc);
		// Ensure there is space for this fixed alloc. This will throw if there is not space
		getSpacesLeftRemovingFixedAllocs(assignedAddresses);
	}
	
	private List<AddressRange> getSpacesLeftRemovingFixedAllocs(AssignedAddresses assignedAddresses)
	{
		List<AddressRange> spacesLeft = new LinkedList<>();
		for (AddressRange range : spaces)
		{
			spacesLeft.add(new AddressRange(range));
		}
		
		removeFixedBlocksSpaces(spacesLeft, assignedAddresses);
		removeReplacementBlocksSpaces(spacesLeft, assignedAddresses);
			
		// Getting here means we found space for every fixed alloc
		return spacesLeft;
	}

	private void removeFixedBlocksSpaces(List<AddressRange> spacesLeft, AssignedAddresses assignedAddresses)
	{
		AddressRange containingSpace = null;
		AddressRange fixedRange;
		int spaceIndex;
		for (FixedBlock block : fixedAllocations)
		{
			// If its a replacement block, we might not have space for it since its overwriting
			// other code. For those we skip them here and handle them later
			if (block instanceof ReplacementBlock)
			{
				continue;
			}
			
			// For non replacement blocks, we need to make sure they fit in a space
			// We know that it should always be contained in a space
			fixedRange = new AddressRange(block.getFixedAddress().getAddressInBank(), block.getFixedAddress().getAddressInBank() + block.getWorstCaseSize(assignedAddresses));
			for (spaceIndex = 0; spaceIndex < spacesLeft.size(); spaceIndex++)
			{
				// If it is contained in the space, we found where it lives. We 
				// need to remove it from the space and potentially add a new
				// space to the list
				if (spacesLeft.get(spaceIndex).contains(fixedRange))
				{
					containingSpace = spacesLeft.get(spaceIndex);
					break;
				}
			}
			
			// If we didn't find any space that fits this, then we cannot put it here so we
			// abort
			if (containingSpace == null)
			{
				throw new RuntimeException(String.format("There was not space from 0x%x to 0x%x in bank 0x%x for FixedBlock %s - "
						+ "only ReplacementBlocks do not need free space in the bank", block.getFixedAddress().getAddressInBank(), 
						block.getFixedAddress().getAddressInBank() + block.getWorstCaseSize(assignedAddresses), bank, block.getId()));
			}
			
			// Otherwise its good - we found a space that contains it as expected
			AddressRange splitRange = containingSpace.removeOverlap(fixedRange);
			if (containingSpace.isEmpty())
			{
				spacesLeft.remove(spaceIndex);
			}
			if (splitRange != null)
			{
				spacesLeft.add(splitRange);
			}
		}
	}
	
	private void removeReplacementBlocksSpaces(List<AddressRange> spacesLeft, AssignedAddresses assignedAddresses)
	{
		// We still need to remove the overlap for the replacement blocks if there is any though
		AddressRange fixedRange;
		AddressRange space;
		AddressRange splitRange;
		List<AddressRange> newRanges = new LinkedList<>();
		Iterator<AddressRange> spacesItr;
		for (FixedBlock block : fixedAllocations)
		{
			// Non replacement blocks have already been handled
			if (!(block instanceof ReplacementBlock))
			{
				continue;
			}
			
			fixedRange = new AddressRange(block.getFixedAddress().getAddressInBank(), block.getFixedAddress().getAddressInBank() + block.getWorstCaseSize(assignedAddresses));
			spacesItr = spacesLeft.iterator();
			while (spacesItr.hasNext())
			{
				space = spacesItr.next();
				splitRange = space.removeOverlap(fixedRange);
				// Make sure its not empty. If it is remove it
				if (space.isEmpty())
				{
					spacesItr.remove();
				}
				// If we have a new range to add, add it to a temp list. We have to do this
				// because we could overlap with more than one space and we can't add during
				// the loop because that would cause concurrent modifications
				if (splitRange != null)
				{
					newRanges.add(splitRange);
				}
			}			
		}
		
		// Add any newly created spaces to the list
		spacesLeft.addAll(newRanges);
	}
	
	public void addMoveableBlock(MoveableBlock alloc)
	{
		priortizedAllocations.add(alloc);
    }

	public boolean checkForAndRemoveExcessAllocs(List<MoveableBlock> allocsThatDontFit, AssignedAddresses assignedAddresses)
	{
		// Clear the spaces and output var
		// Clear just the addresses but keep the banks since they still are
		// here so we can get a better idea of the size
		allocsThatDontFit.clear();
		
		// Ensure the allocations are sorted
		priortizedAllocations.sort(MoveableBlock.PRIORITY_SORTER);
		
		return checkForAndRemoveExcessAllocsInCollection(priortizedAllocations.iterator(), assignedAddresses, allocsThatDontFit);
	}

	// TODO later: Probably can optimize packing into bank space some (i.e. leave most space, leave smallest space)
	private boolean checkForAndRemoveExcessAllocsInCollection(Iterator<MoveableBlock> allocItr, AssignedAddresses assignedAddresses, List<MoveableBlock> allocsThatDontFit)
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
				DataManagerUtils.assignBlockAndSegmentBankAddresses(block, block.getFixedAddress(), assignedAddresses);
			}
			
			// Now get the spaces that are left after the fixed block spaces are removed
			spacesLeft = getSpacesLeftRemovingFixedAllocs(assignedAddresses);
			
			// Loop over each movable block and see if it fits
			while (allocItr.hasNext())
			{
				// For each block, we go through each space and see if there is room until
				// we either find room or run out of spaces
				alloc = allocItr.next();
				placed = false;
				allocSize = alloc.getWorstCaseSize(assignedAddresses);
				for (AddressRange space : spacesLeft)
				{
					if (allocSize <= space.size())
					{
						space.shrink(allocSize);
						placed = true;
					}
				}
				
				if (!placed)
				{
					// We removed one - the list ins't stable now
					// We need another pass to see if anything else no longer
					// will fit
					stable = false;
					DataManagerUtils.removeBlockAndSegmentAddresses(alloc, assignedAddresses);
					allocsThatDontFit.add(alloc);
					allocItr.remove();
				}
			}
		}
		
		return !allocsThatDontFit.isEmpty();
	}

	boolean debug = false;
	public void assignAddresses(AssignedAddresses assignedAddresses) 
	{
		// Get the spaces that are left after the fixed block spaces are removed
		List<AddressRange> spacesLeft = getSpacesLeftRemovingFixedAllocs(assignedAddresses);
		
		boolean placed;
		int allocSize;
		
		for (MoveableBlock block : priortizedAllocations)
		{
			if (debug)
			{
				System.out.print("Assigning address for block " + block.getId());
			}
			placed = false;
			allocSize = block.getWorstCaseSize(assignedAddresses);			
			if (debug)
			{
				System.out.print(" - size " + allocSize);
			}
			for (AddressRange space : spacesLeft)
			{
				if (allocSize <= space.size())
				{
					DataManagerUtils.assignBlockAndSegmentBankAddresses(block, new BankAddress(bank, (short) space.getStart()), assignedAddresses);
					space.shrink(allocSize);
					placed = true;
					break;
				}
			}
			
			if (!placed)
			{
				throw new RuntimeException(String.format("Failed to assign a address to block " + block.getId() +
						" while assigning addresses for bank 0x%x. This should not occur if all banks are added then "
						+ "Allocated via DataManger!", bank));
			}
		}
	}

	public byte getBank() 
	{
		return bank;
	}
}
