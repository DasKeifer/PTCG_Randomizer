package datamanager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import constants.RomConstants;
import rom.Blocks;
import util.RomUtils;

public class DataManager
{
	// TODO: make static?
	// TODO: instead of saving by data blocks, maybe save by bank/space allocation?
	
	// Bank, object
	private SortedMap<Byte, AllocatableBank> freeSpace;
	
	public DataManager()
	{
		freeSpace = new TreeMap<>();
	}
	
	// New approach
	// First do fixed allocations
	// Start by allocating everything to its most desired bank
	// Then go through and for each one that doesn't fit, move try to move it to its next preferred bank
	// if there are no more banks, then shrink and repeat
	// if it does not shrink, then start over and see if others can shrink
	
	public void assignBlockLocations(
			byte[] bytesToPlaceIn,
			Blocks blocks)
	{
		// Determine what space we have free
		determineAllFreeSpace(bytesToPlaceIn);
		
//		for (Entry<Byte, AllocatableBank> entry : freeSpace.entrySet())
//		{
//			System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
//		}
		
		// Take into account the fixed blocks
		reserveFixedBlocksSpaces(blocks);
	
		// Allocate space for the constrained blocks then the unconstrained ones
		makeAllocations(blocks.getAllBlocksToAllocate());
	}
	
	private void reserveFixedBlocksSpaces(Blocks blocks)
	{
		for (FixedBlock block : blocks.getAllFixedBlocks())
		{
			removeFixedSpace(block);
		}
	}
	
	private void removeFixedSpace(FixedBlock block)
	{
		int address = block.getFixedAddress();
		AllocatableBank bank = freeSpace.get(RomUtils.determineBank(address));
		bank.removeAddressSpace(new AddressRange(address, address + block.size()));
	}
	
	private boolean makeAllocations(List<MoveableBlock> toAllocate)
	{		
		List<Allocation> unsuccessfulAllocs = new LinkedList<>();
		
		performInitialAllocation(toAllocate, unsuccessfulAllocs);
		shrinkAndPlaceRemaining(unsuccessfulAllocs);
		shrinkOtherAllocsAndPlaceRemaining(unsuccessfulAllocs);
		
		return unsuccessfulAllocs.isEmpty();
	}
	
	private void performInitialAllocation(List<MoveableBlock> toAllocate, List<Allocation> unsuccessfulAllocs)
	{
		// For the first pass, assign all to their most preferred bank
		List<Allocation> allocs = new LinkedList<Allocation>();
		for (MoveableBlock block : toAllocate)
		{
			allocs.add(new Allocation(block));
		}
		
		// then call the helper and recurse until we find a solution or run out of space to alloc something
		tryRecursivelyFixAllocs(allocs, unsuccessfulAllocs);
	}
	
	private void shrinkAndPlaceRemaining(List<Allocation> remainingAllocs)
	{
		// Shrink any that can and remove them from the remaining allocs
		List<Allocation> shrunkAllocs = new LinkedList<>();
		Iterator<Allocation> allocItr = remainingAllocs.iterator();
		Allocation alloc;
		while (allocItr.hasNext())
		{
			alloc = allocItr.next();
			if (alloc.data.canBeShrunkOrMoved() && !alloc.data.movesNotShrinks())
			{
				alloc.resetBankPreferences();
				alloc.data.setShrunkOrMoved(true);
				allocItr.remove();
			}
		}
		
		// Now try to recursively add them. Note that remaining allocs will preserve the ones that couldn't shrink
		tryRecursivelyFixAllocs(shrunkAllocs, remainingAllocs);
	}
	
	private void shrinkOtherAllocsAndPlaceRemaining(List<Allocation> remainingAllocs, List<Allocation> shrunkAllocs)
	{
		// Reset the bank preferences
		for (Allocation alloc : remainingAllocs)
		{
			alloc.resetBankPreferences();
		}
		
		tryRecursivelyShrinkAllocs(remainingAllocs, shrunkAllocs);
	}
	
	private void tryRecursivelyShrinkAllocs(List<Allocation> remainingAllocs, List<Allocation> shrunkAllocs)
	{
		// Add them to the most preferred bank
		Set<Byte> banksTouched = new HashSet<>();
		List<Allocation> failedAllocs = new LinkedList<>();
		tryAddAllocsToNextUnattemptedBank(remainingAllocs, banksTouched, failedAllocs);
		
		if (failedAllocs.size() < 1)
		{
			// TODO failure
			return;
		}
		
		// THink on this more...
		
		// Try to shrink - if successful
		List<Allocation> newAllocs = new LinkedList<>();
		List<AllocationDependecy> newAllocsRelations = new LinkedList<>(); // Contains shrunk allocs
		for (byte bank : banksTouched)
		{
			freeSpace.get(bank).shrinkToPackAllocs(newAllocsRelations);
		}

		// try and place new blocks
		List<Allocation> failedAllocs = new LinkedList<>();
		tryRecursivelyFixAllocs(newAllocs, failedAllocs);
		
		// If some didn't fit, try shrinking again...
		List<Allocation> moreShrunkAllocs = new LinkedList<>();
		shrinkOtherAllocsAndPlaceRemaining(failedAllocs, moreShrunkAllocs);
		
		// If they still didn't fit, unwind everything we tried to get them to fit
		if (failedAllocs.size() > 0)
		{
			for (Allocation alloc : moreShrunkAllocs)
			{
				alloc.data.setShrunkOrMoved(false);
			}
			
			for (AllocationDepedency dep : newAllocsRelations)
			{
				for (Allocation generated : dep.generated)
				{
					if (failedAllocs.contains(generated))
					{
						
					}
				}
			}
		}
		
				
				// if successful we are done
		
				// else, undo
		
		// else try next one (recurse)
	}
	
	private void tryRecursivelyFixAllocs(List<Allocation> toAlloc, List<Allocation> unsuccessfulAllocs)
	{
		// Don't clear unsuccessful allocs so we can chain these calls and preserve unsuccessful ones
		
		if (!toAlloc.isEmpty())
		{
			Set<Byte> banksTouched = new HashSet<>();
			tryAddAllocsToNextUnattemptedBank(toAlloc, banksTouched, unsuccessfulAllocs);
			tryRecursivelyFixAllocsRecursor(banksTouched, unsuccessfulAllocs);
		}
	}
	
	private void tryRecursivelyFixAllocsRecursor(Set<Byte> banksToCheck, List<Allocation> unsuccessfulAllocs)
	{
		// Go through each bank and see what doesn't fit
		List<Allocation> allocsThatDontFit = new LinkedList<>();
		
		// Should always be clear
		for (byte bank : banksToCheck)
		{
			freeSpace.get(bank).packAndRemoveExcessAllocs(allocsThatDontFit);
		}
		
		// Recursion end case - all either fit or ran out of banks to be fit in
		if (allocsThatDontFit.isEmpty())
		{
			return;
		}
		
		banksToCheck.clear();
		tryAddAllocsToNextUnattemptedBank(allocsThatDontFit, banksToCheck, unsuccessfulAllocs);
		tryRecursivelyFixAllocsRecursor(banksToCheck, unsuccessfulAllocs);
	}
	
	private void tryAddAllocsToNextUnattemptedBank(List<Allocation> toAlloc, Set<Byte> banksTouched, List<Allocation> unsuccessfulAllocs)
	{
		for (Allocation alloc : toAlloc)
		{
			// Ran out of banks to try - add it to the list of failed allocs 
			if (alloc.isUnattemptedAllowableBanksEmpty())
			{
				unsuccessfulAllocs.add(alloc);
			}
			else
			{
				AllocatableBank bank = freeSpace.get(alloc.popNextUnattemptedAllowableBank());
				bank.addToBank(alloc);
				banksTouched.add(bank.getBank());
			}
		}
	}
		
	
	// Blocks to Alloc only potentially modified if true is returned (or error if caught)
//	private boolean attemptToShrinkOtherAllocsAndPlace(MoveableBlock data, List<UnconstrainedMoveBlock> blocksToAlloc)
//	{		
//		byte bestOptionPriority = 0;
//		AllocatableBank bestOption = null;
//		for (Entry<Byte, BankRange> option : data.getUnattemptedPreferencedAllowableBanks().entrySet())
//		{
//			for (byte currBank = option.getValue().start; currBank < option.getValue().stopExclusive; currBank++)
//			{
//				AllocatableBank currOption = freeSpace.get(currBank);
//				byte optionPriority = currOption.canShrinkingToMakeSpace(data.getCurrentWorstCaseSizeOnBank(currOption.getBank()));
//				if (optionPriority > bestOptionPriority)
//				{
//					bestOptionPriority = optionPriority;
//					bestOption = currOption;
//				}
//			}
//		}
//		
//		// Did we find any that worked?
//		if (bestOption != null)
//		{
//			// We just checked so we should always succeed here
//			if (!bestOption.attemptToAdd(data, blocksToAlloc))
//			{
//				throw new IllegalArgumentException("Logic error: The selected bank (" + bestOption + ") no longer has space to add the block!");
//			}
//			return true;
//		}
//		
//		return false;
//	}
	
	
	// TODO: we need to avoid images/gfx somehow - perhaps have it hardcoded which banks these occur in?
	// GFX banks
	
	//engine banks 0-8 + 9 & a
	//effect functions: b (overflow to a?)
	//data banks c
	//text banks d-19 + 1a & 1b
	//gfx 1d-3b, 20 engine related to gfx, + 1f, 2f,30, 3c
	//audio 3d & 3e
	//sfx 3f
	
	// TODO: detect all spaces then clear the data? Then we don't have to worry about 
	// overflowing as much
	private void determineAllFreeSpace(byte[] rawBytes)
	{
		freeSpace.clear();
		
		int spaceAddress;
		int address;
		int nextBankBoundary = 0;
		byte numBanks = (byte) Math.ceil((double) rawBytes.length / RomConstants.BANK_SIZE);
		for (byte bank = 0; bank < numBanks; bank++)
		{
			// Insert map for this bank
			AllocatableBank bankSpace = new AllocatableBank(bank);
			freeSpace.put(bank, bankSpace);
			
			// Reset the address in case the last bank ended with an empty space
			address = nextBankBoundary;
			
			// Determine where this bank ends
			nextBankBoundary = nextBankBoundary + RomConstants.BANK_SIZE;
			if (nextBankBoundary > rawBytes.length)
			{
				nextBankBoundary = rawBytes.length;
			}
			
			// Loop through the bank looking for empty space
			for (/*don't reset*/; address < nextBankBoundary; address++)
			{
				if (rawBytes[address] == (byte) 0xFF)
				{
					spaceAddress = address;
					for (address++; address < nextBankBoundary; address++)
					{
						if (rawBytes[address] != (byte) 0xFF)
						{
							break;
						}
					}
					
					// If we found space, then save it to the map
					// We only save spaces that are at least 3 long to prevent finding memory locations
					// which can be 0xFF
					if (address - spaceAddress > 40)
					{
						bankSpace.addSpace(spaceAddress, address);
						System.out.println(String.format("0x%x - 0x%x - 0x%x", bank, spaceAddress, address - spaceAddress));
					}
				}
			}
		}
	}
}
