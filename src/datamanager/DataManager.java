package datamanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

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
		// Create the allocations
		List<Allocation> allocs = new LinkedList<Allocation>();
		for (MoveableBlock block : toAllocate)
		{
			allocs.add(new Allocation(block));
		}

		if (!tryToPlaceAllocs(allocs, true)) // true = Try shrinking excess
		{
			clearAndResetAllAllocs(allocs);
			return shrinkAllToPlace(allocs);
		}
		
		return true;
	}
		
	private boolean tryToPlaceAllocs(List<Allocation> toAlloc, boolean tryShrinkingExcess)
	{
		List<Allocation> unsuccessfulAllocs = new LinkedList<Allocation>();
		if (!toAlloc.isEmpty())
		{
			Set<Byte> banksTouched = new HashSet<>();
			tryAddAllocsToNextUnattemptedBank(toAlloc, banksTouched, unsuccessfulAllocs);
			tryRecursivelyToPlaceAllocsRecursor(banksTouched, unsuccessfulAllocs);
		}
		
		if (!unsuccessfulAllocs.isEmpty() && tryShrinkingExcess)
		{
			return shrinkAndPlaceRemaining(unsuccessfulAllocs);
		}
		
		return unsuccessfulAllocs.isEmpty();
	}
	
	private void tryRecursivelyToPlaceAllocsRecursor(Set<Byte> banksToCheck, List<Allocation> unsuccessfulAllocs)
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
		tryRecursivelyToPlaceAllocsRecursor(banksToCheck, unsuccessfulAllocs);
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
				if (bank == null)
				{
					// Error! - ran out of preferences
					// TODO:
				}
				bank.addToBank(alloc);
				banksTouched.add(bank.getBank());
			}
		}
	}
		
	private boolean shrinkAndPlaceRemaining(List<Allocation> remainingAllocs)
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
		// False = don't try to shrink - we already did. Passing true and not guarding this would cause infinite loop!
		if (!shrunkAllocs.isEmpty())
		{
			return tryToPlaceAllocs(shrunkAllocs, false);
		}
		
		// Didn't shrink anything so there will be no change
		return false;
	}
	
	private void clearAndResetAllAllocs(List<Allocation> toAllocate)
	{
		// Clear the banks
		for (AllocatableBank bank : freeSpace.values())
		{
			bank.clearAllAllocs();
		}
		
		// Reset the alloc preferences
		// Address and bank cleared by above loop
		for (Allocation alloc : toAllocate)
		{
			alloc.resetBankPreferences();
		}
	}
	
	private boolean shrinkAllToPlace(List<Allocation> toAllocate)
	{
		// Shrink all allocations and add the shrunk ones
		List<Allocation> withRemoteAllocs = new LinkedList<>();
		Map<String, Allocation> dependencies = new HashMap<>();
		Allocation remoteAlloc;
		for (Allocation alloc : toAllocate)
		{
			withRemoteAllocs.add(alloc);
			UnconstrainedMoveBlock remoteBlock = alloc.shrinkIfPossible();
			if (remoteBlock != null)
			{
				remoteAlloc = new Allocation(remoteBlock);
				dependencies.put(alloc.data.getId(), remoteAlloc);
				withRemoteAllocs.add(remoteAlloc);
			}
		}
		
		// Now go through and allocate them. If they still don't fit, then there is nothing 
		// further we can do
		if (!tryToPlaceAllocs(toAllocate, false)) // False = don't try and shrink - everything already shrunk
		{
			return false;
		}

		// If they do fit, go through and try to unshrink as much as possible
		unshrinkAsMuchAsPossible(dependencies);
		
		return true;
	}
	
	private void unshrinkAsMuchAsPossible(Map<String, Allocation> dependencies)
	{
		unshrinkAsMuchAsPossibleRecursor(dependencies, freeSpace.keySet());
	}

	private void unshrinkAsMuchAsPossibleRecursor(Map<String, Allocation> dependencies, Set<Byte> banksToTry)
	{
		Set<Byte> banksToRecheck = new TreeSet<>();
		List<Allocation> unshrunkAllocs = new LinkedList<>();
		AllocatableBank bank;
		Allocation remoteAlloc;
		for (byte bankId : banksToTry)
		{
			// Get the bank and try to unshrink allocations and return which ones were shrunk
			bank = freeSpace.get(bankId);
			bank.unshrinkAsMuchAsPossible(unshrunkAllocs);
			
			// For each allocation that was unshrunk, get the remote block and remove it
			// and possibly add its former bank to the list of banks to retouch
			for (Allocation alloc : unshrunkAllocs)
			{
				remoteAlloc = dependencies.get(alloc.data.getId());
				if (remoteAlloc != null)
				{
					// Remove the remote block/alloc
					byte removeFrom = remoteAlloc.removeAlloc();
					
					// If we are not checking the bank this pass or if the bank it was
					// removed from is less than this one, it will need another pass. 
					// Otherwise we will hit it when we come to it
					if (!banksToTry.contains(removeFrom) || removeFrom < bankId)
					{
						banksToRecheck.add(removeFrom);
					}
				}
			}
		}
		
		// If we have more banks to check, iterate
		if (!banksToRecheck.isEmpty())
		{
			unshrinkAsMuchAsPossibleRecursor(dependencies, banksToRecheck);
		}
	}
	
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
