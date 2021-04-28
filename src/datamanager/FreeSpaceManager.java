package datamanager;

import java.awt.geom.Rectangle2D;
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
	// TODO: Do location specific replaces, do restricted places, then do free replaces

	// TODO: it would be nice if we can chain allocs together based on if they are sequential in a space
	// If we chain them, then it probably makes a whole lot of things easier...
	
	private class AddressRange
	{
		int start;
		int stop;
		
		public AddressRange(int start, int stop)
		{
			this.start = start;
			this.stop = stop;
		}
		
		public AddressRange(AddressRange toCopy)
		{
			this(toCopy.start, toCopy.stop);
		}
		
		public int size()
		{
			return stop - start;
		}

		public boolean isInRange(int address)
		{
			return start <= address && address <= stop;
		}
		
		// TODO default sorter based on start
	}
	
	private class AllocatedRange extends AddressRange
	{
		AllocData data;
		AllocatedRange nextAlloc;
		
		public AllocatedRange(int start, int stop, AllocData data)
		{
			super(start, stop);
			this.data = data;
		}
		
		public AllocatedRange(AllocatedRange toCopy)
		{
			super(toCopy);
			data = toCopy.data;
		}
	}
	
	private class AllocData
	{
		byte priority;
		boolean shrunk;
		FlexibleBlock block;
		Map<Byte, AddressRange> allowedOptions;
	}
	
	// Bank, object
	private Map<Byte, Set<AddressRange>> freeSpace;
	private Map<Byte, Set<AllocatedRange>> allocatedSpace;
	
	// Priority, object
	private Map<Byte, List<AllocData>> allocsToProcess;
	
	public void makeAllocations(List<FlexibleBlock> blocksToPlace)
	{
		// First add all the blocks to the pending allocations list
		AllocData blockData;
		for (FlexibleBlock block : blocksToPlace)
		{
			blockData = new AllocData();
			blockData.allowedOptions = blockData.getPreferencedAllowableAddresses();
			blockData.block = block; // Intentionally a reference and not a copy
			addAllocToProcess(blockData);
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
	
	private void addAllocToProcess(AllocData data)
	{
		List<AllocData> blocksWithPriority = allocsToProcess.get(data.priority);
		
		if (blocksWithPriority == null)
		{
			blocksWithPriority = new LinkedList<>();
			allocsToProcess.put(data.priority, blocksWithPriority);
		}
		
		blocksWithPriority.add(data);
	}
	
	private void addAllocatedSpace(AllocatedRange allocated)
	{
		byte bank = RomUtils.determineBank(allocated.start);
		Set<AllocatedRange> blocksInBank = allocatedSpace.get(bank);
		
		if (blocksInBank == null)
		{
			blocksInBank = new HashSet<>();
			allocatedSpace.put(bank, blocksInBank);
		}
		
		blocksInBank.add(allocated);
	}
	
	private void addAllocToConflicts(Map<Byte, List<AllocatedRange>> conflictingAllocs, AllocatedRange conflict)
	{
		List<AllocatedRange> blocksWithPriority = conflictingAllocs.get(conflict.data.priority);
		
		if (blocksWithPriority == null)
		{
			blocksWithPriority = new LinkedList<>();
			conflictingAllocs.put(conflict.data.priority, blocksWithPriority);
		}
		
		blocksWithPriority.add(conflict);
	}
	
	private boolean tryAllocate(AllocData data)
	{
		boolean success = false;
		Map<Byte, List<AllocatedRange>> conflictingAllocs = new HashMap<>();
		
		// First try to place and get the list of conflicting allocs
		success = attemptToPlace(data, conflictingAllocs);

		// If we failed to place and we can shrink, shrink and try to place again
		// This will update the conflicting allocs
		if (!success)
		{
			if (data.block.canBeShrunk())
			{
				data.shrunk = true;
				success = attemptToPlace(data, conflictingAllocs);
			}
		}

		// If we still failed, try to shrink others
		if (!success)
		{
			success = attemptToShrinkAndPlace(data, conflictingAllocs);
		}
		
		return success;
	}
	
	private boolean attemptToPlace(AllocData data, Map<Byte, List<AllocatedRange>> conflictingAllocs)
	{
		// Make sure the list is clear
		conflictingAllocs.clear();
		
		int allocatedStart;
		int spaceToFind = data.size(); // TODO
		
		// For each possibility, try to allocate and add any allocations that 
		// conflicted with the option
		for (Entry<Byte, AddressRange> option : data.allowedOptions.entrySet())
		{
			allocatedStart = allocateInRange(spaceToFind, option.getValue(), conflictingAllocs);
			
			// Did we successfully allocate?
			if (allocatedStart >= 0)
			{
				// Add the alloc, clear the list of conflicting allocs and return
				addAllocatedSpace(new AllocatedRange(allocatedStart, allocatedStart + spaceToFind, data));
				conflictingAllocs.clear();
				return true;
			}
		}
		
		return false;
	}

	private int allocateInRange(int spaceToFind, AddressRange range, Map<Byte, List<AllocatedRange>> conflictingAllocs)
	{
		byte bank = RomUtils.determineBank(range.start);
		byte endBank = RomUtils.determineBank(range.stop);

		int allocSpaceIdx;
		int foundSpaceIdx;
		Set<AddressRange> spaceInBank;
		List<AddressRange> spaceInBankList;
		AddressRange spaceFound;
		
		List<AddressRange> unallocSpace = new LinkedList<>();
		Set<AllocatedRange> allocatedInBank;
		// Go through each bank and try to find the space
		for (; bank < endBank; bank++)
		{
			spaceInBank = freeSpace.get(bank);
			spaceInBankList = new LinkedList<>(spaceInBank);
			
			// Does it fit?
			foundSpaceIdx = fitsInSpace(spaceToFind, spaceInBankList, 0);
			while (foundSpaceIdx != -1)
			{
				// Get the space
				spaceFound = spaceInBankList.get(foundSpaceIdx);
				
				// Break it into smaller segments based on what is allocated
				unallocSpace.clear();
				unallocSpace.add(new AddressRange(spaceFound));
				allocatedInBank = allocatedSpace.get(bank);
				for (AllocatedRange allocated : allocatedInBank)
				{
					if (removeOverlap(allocated, unallocSpace))
					{
						addAllocToConflicts(conflictingAllocs, allocated);
					}
				}
				
				// Does it still fit? If so we found a winner!
				allocSpaceIdx = fitsInSpace(spaceToFind, unallocSpace, 0);
				if (allocSpaceIdx >= 0)
				{
					conflictingAllocs.clear();
					return unallocSpace.get(allocSpaceIdx).start;
				}
				
				// Otherwise keep searching
				foundSpaceIdx = fitsInSpace(spaceToFind, spaceInBankList, foundSpaceIdx);
			}
		}
		
		return -1;
	}
	
	private boolean removeOverlap(AddressRange toRemove, List<AddressRange> ranges)
	{
		boolean removedSpace = false;
		AddressRange current;
		for(int i = 0; i < ranges.size(); i++)
		{
			current = ranges.get(i);
			// overlaps with the start of what to remove
			if (current.isInRange(toRemove.start))
			{
				// Full to remove is covered - bisect this and add
				if (current.isInRange(toRemove.stop))
				{
					removedSpace = true;
					
					// Remove the current and replace with the split one
					ranges.remove(i);
					ranges.add(new AddressRange(current.start, toRemove.start));
					ranges.add(new AddressRange(toRemove.stop, current.stop));
					// Should be all done
					break;
				}
				// Only overlaps with the start - modify in place
				else
				{
					removedSpace = true;
					current.stop = toRemove.start;
				}
			}
			// Only overlaps with the stop - modify in place
			else if (ranges.get(i).isInRange(toRemove.stop))
			{
				removedSpace = true;
				current.start = toRemove.stop;
			}
			// Last check to see if the current range is completely enclosed by the remove
			else if (toRemove.isInRange(current.stop)) 
			{
				// Remove and decrement so we don't skip any
				removedSpace = true;
				ranges.remove(i);
				i--;
			}
		}
		
		return removedSpace;
	}
	
	private int fitsInSpace(int size, List<AddressRange> spaces, int startIdx)
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
	
	// The assumption here is that the shrunk alloc can find space in the rom... but we do that in a second pass anyways?
	private boolean attemptToShrinkAndPlace(AllocData data, Map<Byte, List<AllocatedRange>> conflictingAllocs)
	{
		boolean place = false;
		List<AllocatedRange> originalAllocs = new LinkedList<>();
		
		// Start with the lowest priority. Shrink it, shift it forward if possible and then see if the new alloc can fit
		for (Entry<Byte, List<AllocatedRange>> allocsWithPriority : conflictingAllocs.entrySet())
		{
			for (AllocatedRange alloc : allocsWithPriority.getValue())
			{
				if (alloc.canShrink())
				{
					originalAllocs.add(new AllocatedRange(alloc));
					AddressRange largerRange = shrinkAllocation();
					if (largerRange.size() >= data.size())
					{
						// Success!
						
						// Expand back as we can
					}
				}
			}
		}
	}
	
	private void shrinkAllocation(AllocatedRange alloc)
	{		
		// Shrink this allocation
		alloc.data.shrunk = true;
		alloc.stop = alloc.start + alloc.data.size();
		
		// Try to move the others left
		
		// Finally figure out the new block size we created at the end of the moves
		
	}
	
	private void reexpandAllocations(Map<Byte, List<AllocatedRange>> allocsShrunk, AddressRange newAllocation)
	{
		for (Entry<Byte, List<AllocatedRange>> allocsWithPriority : allocsShrunk.entrySet())
		{
			for (AllocatedRange alloc : allocsWithPriority.getValue())
			{
				// Try to expand it - if it overlaps with another alloc, try to push it too
				int shift = alloc.data.getFullSize() - alloc.data.size();
				int expandedEnd = alloc.end + shift;
	
				// Look through each alloc in this bank seeing if it overlaps
//				for (Set<AllocatedRange> otherAlloc : allocatedSpace)
			}
		}
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
			Map<Integer, Integer> bankMap = new HashMap<>();
			freeSpace.put(bank, bankMap);
			
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
						bankMap.put(spaceAddress, address - spaceAddress);
						System.out.println(String.format("0x%x - 0x%x - 0x%x", bank, spaceAddress, address - spaceAddress));
					}
				}
			}
		}
	}
	
	public int allocateSpace(int spaceNeeded)
	{
		int allocatedSpace = -1;
		// Go through each bank and try to find the space
		for (Entry<Byte, Map<Integer, Integer>> bankMap : freeSpace.entrySet())
		{
			allocatedSpace = allocateSpaceInBank(bankMap.getValue(), spaceNeeded);
			if (allocatedSpace >= 0)
			{
				break;
			}
		}
		
		return allocatedSpace;
	}

	public int allocateSpace(byte bank, int spaceNeeded)
	{
		return allocateSpaceInBank(freeSpace.get(bank), spaceNeeded);
	}

	public boolean allocateSpecificSpace(int address, int spaceNeeded)
	{
		boolean success = false;
		
		byte bank = RomUtils.determineBank(address);
		short relativeAddress = RomUtils.convertToInBankOffset(bank, address);
		Map<Integer, Integer> bankMap = freeSpace.get(bank);
		
		int freeSpaceEnd;
		int allocEnd = address + spaceNeeded;
		for (Entry<Integer, Integer> entry : bankMap.entrySet())
		{
			freeSpaceEnd = entry.getKey() + entry.getValue();
			
			// If its greater than or equal to the start of the free space
			// and its less than the end of the free space, this is our entry
			if (entry.getKey() <= relativeAddress && 
					freeSpaceEnd > relativeAddress)
			{
				// See if the end of the free space is far enough from the start of it to fit this allocation
				success = freeSpaceEnd >= allocEnd;
				
				if (success)
				{
					// Split the entry accordingly
					bankMap.remove(entry.getKey());
					
					// Determine space before and add it to the map as needed
					if (relativeAddress > entry.getKey())
					{
						bankMap.put(entry.getKey(), relativeAddress - entry.getKey());
					}
					
					// Now handle space at the end if there is any
					if (freeSpaceEnd > allocEnd)
					{
						bankMap.put(allocEnd, freeSpaceEnd - allocEnd);
					}
				}
				
				// We found the spot already - no sense in continuing the search
				break;
			}
		}
		
		return success;
	}
	
	private int allocateSpaceInBank(Map<Integer, Integer> bankMap, int spaceNeeded)
	{
		for (Entry<Integer, Integer> space : bankMap.entrySet())
		{
			// TODO: Try to find smallest spaces?
			if (space.getValue() > spaceNeeded)
			{
				int totalSpace = space.getValue();
				int spaceAddress = space.getKey();
				
				bankMap.remove(spaceAddress);
				bankMap.put(spaceAddress + spaceNeeded, totalSpace - spaceNeeded);
				return spaceAddress;
			}
		}
		
		return -1;
	}
}
