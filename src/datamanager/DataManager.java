package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import constants.RomConstants;
import rom.Blocks;
import util.RomUtils;

public class DataManager
{
	// TODO: make static
	
	// Bank, object
	private SortedMap<Byte, AllocatableBank> freeSpace;
	
	// Priority, object
	private SortedMap<Byte, List<MoveableBlock>> allocsToProcess;
	
	public DataManager()
	{
		freeSpace = new TreeMap<>();
		allocsToProcess = new TreeMap<>();
	}
	
	public void assignBlockLocations(
			byte[] bytesToPlaceIn,
			Blocks blocks)
	{
		// Determine what space we have free
		determineAllFreeSpace(bytesToPlaceIn);
		
		for (Entry<Byte, AllocatableBank> entry : freeSpace.entrySet())
		{
			System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
		}
		
		// Take into account the fixed blocks
		reserveFixedBlocksSpaces(blocks);
	
		// Allocate space for the constrained blocks then the unconstrained ones
		makeAllocations(blocks);
	}
	
	private void addBlockToAllocate(MoveableBlock block)
	{		
		// Add it to the map of ones to allocate
		DataManagerUtils.addToPriorityMap(allocsToProcess, block);
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
	
	private void makeAllocations(Blocks blocks)
	{		
		allocsToProcess.clear();
		for (MoveableBlock block : blocks.getAllBlocksToAllocate())
		{
			addBlockToAllocate(block);
		}
		
		// Go through each block in priority order that still needs to be allocated and try to place it
		while (!allocsToProcess.isEmpty())
		{
			// Get the list new each time. That way if we add a higher priority one back in,
			// it will get allocated immediately
			Entry<Byte, List<MoveableBlock>> allocsWithPriority = allocsToProcess.entrySet().iterator().next();
			MoveableBlock nextToPlace = allocsWithPriority.getValue().remove(0);
			
			// clean the map up if the list is now empty
			if (allocsWithPriority.getValue().isEmpty())
			{
				allocsToProcess.remove(allocsWithPriority.getKey());
			}
			
			// If we fail to allocate, throw
			if (!tryAllocate(nextToPlace))
			{
				throw new RuntimeException("Failed to place a block! Aborting!");
			}
		}
	}
	
	private boolean tryAllocate(MoveableBlock data)
	{
		// As we go we may find blocks that need to be allocated/reallocated
		List<FloatingBlock> blocksToAlloc = new LinkedList<>();
		
		// First try to place and get the list of conflicting allocs
		boolean success = attemptToPlace(data);

		// If we failed to place and we can shrink, shrink and try to place again
		// This will update the conflicting allocs
		if (!success)
		{
			// See if we can shrink this block and then place it]
			if (data.canBeShrunkOrMoved() && !data.movesNotShrinks())
			{
				data.setShrunkOrMoved(true);
				success = attemptToPlace(data);
				if (success)
				{
					blocksToAlloc.add(data.getRemoteBlock());
				}
			}
		}

		// If we still failed, try to shrink others
		if (!success)
		{
			success = attemptToShrinkOtherAllocsAndPlace(data, blocksToAlloc);
		}
		
		// If we succeeded, add any additional blocks that need to be allocated back in
		if (success)
		{
			for (FloatingBlock block : blocksToAlloc)
			{
				addBlockToAllocate(block);
			}
		}
		
		return success;
	}
	
	private boolean attemptToPlace(MoveableBlock data)
	{		
		// For each possibility, try to allocate and add any allocations that 
		// conflicted with the option
		for (Entry<Byte, BankRange> option : data.getPreferencedAllowableBanks().entrySet())
		{
			if (allocateInRange(data, option.getValue()))
			{
				return true;
			}
		}
		
		return false;
	}

	private boolean allocateInRange(MoveableBlock data, BankRange range)
	{
		// Go through each bank and try to find the space
		for (byte currBank = range.start; currBank < range.stopExclusive; currBank++)
		{
			AllocatableBank bankSpace = freeSpace.get(currBank);
			if (bankSpace.attemptToAdd(data))
			{
				return true;
			}
		}
		
		return false;
	}
	
	// Blocks to Alloc only potentially modified if true is returned (or error if caught)
	private boolean attemptToShrinkOtherAllocsAndPlace(MoveableBlock data, List<FloatingBlock> blocksToAlloc)
	{		
		byte bestOptionPriority = 0;
		AllocatableBank bestOption = null;
		for (Entry<Byte, BankRange> option : data.getPreferencedAllowableBanks().entrySet())
		{
			for (byte currBank = option.getValue().start; currBank < option.getValue().stopExclusive; currBank++)
			{
				AllocatableBank currOption = freeSpace.get(currBank);
				byte optionPriority = currOption.canShrinkingToMakeSpace(data.getCurrentWorstCaseSizeOnBank(currOption.getBank()));
				if (optionPriority > bestOptionPriority)
				{
					bestOptionPriority = optionPriority;
					bestOption = currOption;
				}
			}
		}
		
		// Did we find any that worked?
		if (bestOption != null)
		{
			// We just checked so we should always succeed here
			if (!bestOption.attemptToAdd(data, blocksToAlloc))
			{
				throw new IllegalArgumentException("Logic error: The selected bank (" + bestOption + ") no longer has space to add the block!");
			}
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
