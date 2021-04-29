package datamanager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

import constants.RomConstants;
import util.RomUtils;

public class DataManager
{
	// This may cause a ripple effect through so we will need to handle that
	
	
	// Bank, object
	private TreeMap<Byte, TreeSet<AllocatableSpace>> freeSpace;
	
	// Priority, object
	private TreeMap<Byte, List<AllocData>> allocsToProcess;
	
	public void determineDataLocations(
			byte[] bytesToPlaceIn,
			List<ReplacementBlock> replacementBlocks, 
			List<ConstrainedBlock> constrainedBlocks, 
			List<NoConstraintBlock> noConstraintBlocks
	)
	{
		freeSpace.clear();
		allocsToProcess.clear();
		
		// Write each replacement block
		for (ReplacementBlock block : replacementBlocks)
		{
			block.write(bytesToPlaceIn);
		}
		
		// Determine what space we have free
		determineAllFreeSpace(bytesToPlaceIn);
	
		// Allocate space for the constrained blocks then the unconstrained ones
		makeAllocations(constrainedBlocks);
		makeAllocations(noConstraintBlocks);
	}
	
	public <T extends FlexibleBlock> void makeAllocations(List<T> blocksToPlace)
	{
		// First add all the blocks to the pending allocations list
		for (FlexibleBlock block : blocksToPlace)
		{
			DataManagerUtils.addToPriorityMap(allocsToProcess, new AllocData(block));
		}
		
		// Now go through each block in priority order and try to place it
		// TODO: assume dependencies have higher priorities than the ones they are dependent upon/enforce it when
		// objects are being created. Or do dependencies not have their own priorities?
		// Dependency type - Require same bank, prefer same bank but allow different, exhaust preferences before splitting banks
		// Dependency type has priority, list of snippets, and each snippet has preferred addresses
		// How do we allow circular dependencies then?
		while (!allocsToProcess.isEmpty())
		{
			Entry<Byte, List<AllocData>> allocsWithPriority = allocsToProcess.entrySet().iterator().next();
			AllocData nextToPlace = allocsWithPriority.getValue().remove(0);
			if (allocsWithPriority.getValue().isEmpty())
			{
				allocsToProcess.remove(allocsWithPriority.getKey());
			}
			
			// If we fail to allocate, throw
			// Maybe we should allow the block to specify if its necessary or nice to have?
			if (!tryAllocate(nextToPlace))
			{
				throw new RuntimeException("Failed to place a block!");
			}
		}
	}
	
	private boolean tryAllocate(AllocData data)
	{
		boolean success = false;
		Set<AllocatableSpace> possibleSpacesWithShrinks = new HashSet<>();
		
		// First try to place and get the list of conflicting allocs
		success = attemptToPlace(data, possibleSpacesWithShrinks);

		// If we failed to place and we can shrink, shrink and try to place again
		// This will update the conflicting allocs
		if (!success)
		{
			if (data.canBeShrunk())
			{
				data.shrunk = true;
				success = attemptToPlace(data, possibleSpacesWithShrinks);
			}
		}

		// If we still failed, try to shrink others
		if (!success)
		{
			success = attemptToShrinkOtherAllocsAndPlace(data, possibleSpacesWithShrinks);
		}
		
		return success;
	}
	
	private boolean attemptToPlace(AllocData data, Set<AllocatableSpace> possibleSpacesWithShrinks)
	{
		// Make sure the list is clear
		possibleSpacesWithShrinks.clear();
		
		// For each possibility, try to allocate and add any allocations that 
		// conflicted with the option
		for (Entry<Byte, AddressRange> option : data.getPreferencedAllowableAddresses().entrySet())
		{
			if (allocateInRange(data, option.getValue(), possibleSpacesWithShrinks))
			{
				return true;
			}
		}
		
		return false;
	}

	private boolean allocateInRange(AllocData data, AddressRange range, Set<AllocatableSpace> possibleSpacesWithShrinks)
	{
		byte bank = RomUtils.determineBank(range.start);
		byte endBank = RomUtils.determineBank(range.stop);

		int spaceToFind = data.getCurrentSize();
		int foundSpaceIdx;
		TreeSet<AllocatableSpace> spaceInBank;
		List<AllocatableSpace> spaceInBankList;
		AllocatableSpace spaceFound;
	
		// Go through each bank and try to find the space
		for (; bank < endBank; bank++)
		{
			spaceInBank = freeSpace.get(bank);
			spaceInBankList = new LinkedList<>(spaceInBank);
			
			// Does it fit?
			foundSpaceIdx = fitsInSpaceIfEmpty(spaceToFind, spaceInBankList, 0);
			while (foundSpaceIdx != -1)
			{
				// Get the space
				spaceFound = spaceInBankList.get(foundSpaceIdx);
				
				if (spaceFound.spaceLeft() >= spaceToFind)
				{
					possibleSpacesWithShrinks.clear();
					spaceFound.add(data);
					return true;
				}
				else
				{
					possibleSpacesWithShrinks.add(spaceFound);
				}
				
				// Otherwise keep searching
				foundSpaceIdx = fitsInSpaceIfEmpty(spaceToFind, spaceInBankList, foundSpaceIdx);
			}
		}
		
		return false;
	}
	
	private int fitsInSpaceIfEmpty(int size, List<AllocatableSpace> spaces, int startIdx)
	{
		for (; startIdx < spaces.size(); startIdx++)
		{
			// Does it fit?
			if (spaces.get(startIdx).size() >= size)
			{
				return startIdx;
			}
		}
		
		return -1;
	}
	
	private boolean attemptToShrinkOtherAllocsAndPlace(AllocData data, Set<AllocatableSpace> possibleSpacesWithShrinks)
	{
		int spaceNeeded = data.getCurrentSize();
		
		byte bestOptionPriority = 0;
		AllocatableSpace bestOption = null;
		for (AllocatableSpace option : possibleSpacesWithShrinks)
		{
			byte optionPriority = option.canShrinkingToMakeSpace(spaceNeeded);
			if (optionPriority > bestOptionPriority)
			{
				bestOptionPriority = optionPriority;
				bestOption = option;
			}
		}
		
		// Did we find any that worked?
		if (bestOption != null)
		{
			bestOption.add(data);
			return true;
		}
		
		return false;
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
	
	// TODO: detect all spaces then clear the data. Then we don't have to worry about 
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
			TreeSet<AllocatableSpace> bankSet = new TreeSet<>();
			freeSpace.put(bank, bankSet);
			
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
						bankSet.add(new AllocatableSpace(spaceAddress, address));
						System.out.println(String.format("0x%x - 0x%x - 0x%x", bank, spaceAddress, address - spaceAddress));
					}
				}
			}
		}
	}
}
