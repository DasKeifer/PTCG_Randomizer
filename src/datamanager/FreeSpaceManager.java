package datamanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import constants.RomConstants;
import util.RomUtils;

public class FreeSpaceManager
{
	// TODO: Do location specific replaces, find the available space, then do restricted places and finally free replaces
		
	private class AllocatableSpace extends AddressRange
	{
		Map<Byte, List<AllocData>> allocationsByPriority;
		
		public AllocatableSpace(int start, int stop)
		{
			super(start, stop);
			allocationsByPriority = new HashMap<>();
		}
		
		public void add(AllocData alloc)
		{
			if (alloc.getSize() > spaceLeft())
			{
				if (!shrinkToMakeSpace(alloc))
				{
					throw new RuntimeException("Internal error - fialed to make space for new alloc");
				}
			}
			
			addToPriorityMap(allocationsByPriority, alloc);
		}
		
		public int spaceLeft()
		{ 
			// TODO
			return 0;
		}
		
		public byte canShrinkingToMakeSpace(int space)
		{
			// TODO:
			return 0;
		}
		
		private boolean shrinkToMakeSpace(AllocData alloc)
		{
			// TODO
			return false;
		}
	}
	
	private class AllocData
	{
		boolean shrunk;
		FlexibleBlock block;
		
		public AllocData(FlexibleBlock block)
		{
			shrunk = false;
			this.block = block; // Intentionally a reference and not a copy
		}
		
		public boolean canBeShrunk()
		{
			return block.hasMinimalOption();
		}
		
		public int getSize()
		{
			if (shrunk)
			{
				return block.getMinimalSize();
			}
			else
			{
				return block.getFullSize();
			}
		}
		
		public byte getPriority()
		{
			return block.getPriority();
		}
		
		public  Map<Byte, AddressRange> getPreferencedAllowableAddresses()
		{
			return block.getPreferencedAllowableAddresses();
		}
	}
	
	// Bank, object
	private Map<Byte, Set<AllocatableSpace>> freeSpace;
	
	// Priority, object
	private Map<Byte, List<AllocData>> allocsToProcess;
	
	// TODO a similar process for "free blocks" should be done as well. Perhaps reuse as those
	// are special cases of these that can't be shrunk and can go anywhere
	public void makeAllocations(List<FlexibleBlock> blocksToPlace)
	{
		// First add all the blocks to the pending allocations list
		for (FlexibleBlock block : blocksToPlace)
		{
			addToPriorityMap(allocsToProcess, new AllocData(block));
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
	
	private void addToPriorityMap(Map<Byte, List<AllocData>> priorityMap, AllocData data)
	{
		List<AllocData> blocksWithPriority = priorityMap.get(data.getPriority());
		
		if (blocksWithPriority == null)
		{
			blocksWithPriority = new LinkedList<>();
			priorityMap.put(data.getPriority(), blocksWithPriority);
		}
		
		blocksWithPriority.add(data);
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
			success = attemptToShrinkAndPlace(data, possibleSpacesWithShrinks);
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

		int spaceToFind = data.getSize();
		int foundSpaceIdx;
		Set<AllocatableSpace> spaceInBank;
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

	
	private boolean attemptToShrinkAndPlace(AllocData data, Set<AllocatableSpace> possibleSpacesWithShrinks)
	{
		int spaceNeeded = data.getSize();
		
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
	
	public FreeSpaceManager(byte[] rawBytes)
	{
		freeSpace = new HashMap<>();
		determineAllFreeSpace(rawBytes);		
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
			Set<AllocatableSpace> bankSet = new HashSet<>();
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
