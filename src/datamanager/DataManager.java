package datamanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Set;

import constants.RomConstants;
public class DataManager
{
	// Bank, object
	private TreeMap<Byte, AllocatableBank> freeSpace;
	
	// Priority, object
	private TreeMap<Byte, List<FlexibleBlock>> allocsToProcess;
	
	public void determineDataLocations(
			byte[] bytesToPlaceIn,
			List<ReplacementBlock> replacementBlocks, 
			List<FlexibleBlock> blocksToPlace
	)
	{
		freeSpace.clear();
		allocsToProcess.clear();
		
		// Write each replacement block
		for (ReplacementBlock block : replacementBlocks)
		{
			// TODO
//			block.write(bytesToPlaceIn);
		}
		
		// Determine what space we have free
		determineAllFreeSpace(bytesToPlaceIn);
	
		// Allocate space for the constrained blocks then the unconstrained ones
		makeAllocations(blocksToPlace);
		
		// Attempt to pack and optimize the allocs
		assignAndPackAllocs();
		
		// TODO: Write blocks?
	}
	
	public void makeAllocations(List<FlexibleBlock> blocksToPlace)
	{
		// First add all the blocks to the pending allocations list
		for (FlexibleBlock block : blocksToPlace)
		{
			DataManagerUtils.addToPriorityMap(allocsToProcess, block);
		}
		
		// Now go through each block in priority order and try to place it
		// TODO: assume dependencies have higher priorities than the ones they are dependent upon/enforce it when
		// objects are being created. Or do dependencies not have their own priorities?
		// Dependency type - Require same bank, prefer same bank but allow different, exhaust preferences before splitting banks
		// Dependency type has priority, list of snippets, and each snippet has preferred addresses
		// How do we allow circular dependencies then?
		while (!allocsToProcess.isEmpty())
		{
			Entry<Byte, List<FlexibleBlock>> allocsWithPriority = allocsToProcess.entrySet().iterator().next();
			FlexibleBlock nextToPlace = allocsWithPriority.getValue().remove(0);
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
	
	private boolean tryAllocate(FlexibleBlock data)
	{
		boolean success = false;
		Set<AllocatableBank> possibleBanksWithShrinks = new HashSet<>();
		
		// First try to place and get the list of conflicting allocs
		success = attemptToPlace(data, possibleBanksWithShrinks);

		// If we failed to place and we can shrink, shrink and try to place again
		// This will update the conflicting allocs
		if (!success)
		{
			if (data.canBeShrunkOrMoved())
			{
				data.setShrunkOrMoved(true);
				success = attemptToPlace(data, possibleBanksWithShrinks);
			}
		}

		// If we still failed, try to shrink others
		if (!success)
		{
			success = attemptToShrinkOtherAllocsAndPlace(data, possibleBanksWithShrinks);
		}
		
		return success;
	}
	
	private boolean attemptToPlace(FlexibleBlock data, Set<AllocatableBank> possibleBanksWithShrinks)
	{
		// Make sure the list is clear
		possibleBanksWithShrinks.clear();
		
		// For each possibility, try to allocate and add any allocations that 
		// conflicted with the option
		for (Entry<Byte, BankRange> option : data.getPreferencedAllowableBanks().entrySet())
		{
			if (allocateInRange(data, option.getValue(), possibleBanksWithShrinks))
			{
				return true;
			}
		}
		
		return false;
	}

	private boolean allocateInRange(FlexibleBlock data, BankRange range, Set<AllocatableBank> possibleBanksWithShrinks)
	{
		// Go through each bank and try to find the space
		for (byte currBank = range.start; currBank < range.stopExclusive; currBank++)
		{
			AllocatableBank bankSpace = freeSpace.get(currBank);
			if (bankSpace.doesHaveSpaceLeft(data.getCurrentSizeOnBank(currBank)))
			{
				possibleBanksWithShrinks.clear();
				bankSpace.add(data);
				return true;
			}
			else
			{
				possibleBanksWithShrinks.add(bankSpace);
			}
		}
		
		return false;
	}
	
	private boolean attemptToShrinkOtherAllocsAndPlace(FlexibleBlock data, Set<AllocatableBank> possibleBanksWithShrinks)
	{		
		byte bestOptionPriority = 0;
		AllocatableBank bestOption = null;
		for (AllocatableBank option : possibleBanksWithShrinks)
		{
			byte optionPriority = option.canShrinkingToMakeSpace(data.getCurrentSizeOnBank(option.getBank()));
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

	private void assignAndPackAllocs()
	{
		// Go through and assign the banks
		for (AllocatableBank bank : freeSpace.values())
		{
			bank.assignBanks();
		}
		
		List<Byte> keys = new ArrayList<>(freeSpace.keySet());
		byte currBank;
		byte earliestTouchedBank;
		for (int keyIdx = 0; keyIdx < keys.size(); keyIdx++)
		{
			currBank = keys.get(keyIdx);
			
			// Adjust the allocs. If any alloc was removed, we need to start over
			// and try again
			earliestTouchedBank = freeSpace.get(currBank).adjustAllocs();
			if (earliestTouchedBank > 0)
			{
				keyIdx = earliestTouchedBank;
			}
		}
		
		for (AllocatableBank bank : freeSpace.values())
		{
			bank.assignAddresses();
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
