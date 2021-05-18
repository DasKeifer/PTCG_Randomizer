package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import compiler.SegmentReference;
import constants.RomConstants;

public class DataManager
{
	// Bank, object
	private SortedMap<Byte, AllocatableBank> freeSpace;
	
	// Priority, object
	private SortedMap<Byte, List<MoveableBlock>> allocsToProcess;
	
	// BlockId, Block
	// This can be used for determining references of one block in another and ensures
	// each ID is unique
	private Map<String, BlockAllocData> blocksById;
	
	// SegmentId, Segment Reference
	private Map<String, SegmentReference> segmentRefsById;
	
	public void writeData(
			byte[] bytesToPlaceIn,
			List<FixedBlock> replacementBlocks, 
			List<MoveableBlock> blocksToPlace)
	{
		// Assign Ids (I.e. processes idsToText)
		// TODO: should this be done here or before this?
		// I'm thinking before this - i.e. finalize (fill in placeholders) then write
		// But maybe as a first step - gather all the blocks and ids, link them all,
		// then allocate and write. Perhaps the linking is done external to here though?
		// Probably should be
		
		determineDataLocations(bytesToPlaceIn, replacementBlocks, blocksToPlace);
		
		// At this point all Ids and addresses should be known

		// Write each replacement block
		// This needs to be done after the movable blocks
		for (FixedBlock block : replacementBlocks)
		{
			block.writeBytes(bytesToPlaceIn);
		}
		
		for (MoveableBlock block : blocksToPlace)
		{
			block.writeBytes(bytesToPlaceIn);
		}
	}
	
	private void addBlockById(BlockAllocData block)
	{
		// Ensure no duplicate Ids
		if (blocksById.put(block.getId(), block) != null)
		{
			throw new IllegalArgumentException("Duplicate block ID detected! There must be only " +
					"one allocation block per data block");
		}

		// Add the references for its segments
		for (Entry<String, SegmentReference> idSegRef : block.getSegmentReferencesById().entrySet())
		{
			if (segmentRefsById.put(idSegRef.getKey(), idSegRef.getValue()) != null)
			{
				throw new IllegalArgumentException("Duplicate segment ID detected: " + idSegRef.getKey());
			}
		}
	}
	
	private void addBlockToAllocate(MoveableBlock block)
	{
		addBlockById(block);
		
		// Add it to the map of ones to allocate
		DataManagerUtils.addToPriorityMap(allocsToProcess, block);
	}
	
	private void determineDataLocations(
			byte[] bytesToPlaceIn,
			List<FixedBlock> replacementBlocks, 
			List<MoveableBlock> blocksToPlace
	)
	{
		freeSpace.clear();
		allocsToProcess.clear();	
		blocksById.clear();
		segmentRefsById.clear();

		// First add all the blocks to  we don't have ID conflicts
		for (FixedBlock block : replacementBlocks)
		{
			addBlockById(block);
		}
		
		for (MoveableBlock block : blocksToPlace)
		{
			addBlockToAllocate(block);
		}
		
		// Determine what space we have free
		determineAllFreeSpace(bytesToPlaceIn);
	
		// Allocate space for the constrained blocks then the unconstrained ones
		makeAllocations();
	}
	
	private void makeAllocations()
	{		
		// Now go through each block in priority order and try to place it
		// TODO: assume dependencies have higher priorities than the ones they are dependent upon/enforce it when
		// objects are being created. Or do dependencies not have their own priorities?
		// Dependency type - Require same bank, prefer same bank but allow different, exhaust preferences before splitting banks
		// Dependency type has priority, list of snippets, and each snippet has preferred addresses
		// How do we allow circular dependencies then?
		while (!allocsToProcess.isEmpty())
		{
			Entry<Byte, List<MoveableBlock>> allocsWithPriority = allocsToProcess.entrySet().iterator().next();
			while (!allocsWithPriority.getValue().isEmpty())
			{
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
	}
	
	private boolean tryAllocate(MoveableBlock data)
	{
		// First try to place and get the list of conflicting allocs
		boolean success = attemptToPlace(data);

		// If we failed to place and we can shrink, shrink and try to place again
		// This will update the conflicting allocs
		if (!success)
		{
			// TODO: rework this some more - if we shrunk, we will need to add
			// new entries into our maps. Also need to handle the unshrink case possibly
			if (data.canBeShrunkOrMoved())
			{
				data.setShrunkOrMoved(true);
				success = attemptToPlace(data);
			}
		}

		// If we still failed, try to shrink others
		if (!success)
		{
			// TODO: rework this some more - if we shrunk, we will need to add
			// new entries into our maps.  Also need to handle the unshrink case possibly
			success = attemptToShrinkOtherAllocsAndPlace(data);
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
	
	private boolean attemptToShrinkOtherAllocsAndPlace(MoveableBlock data)
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
			// TODO: flow this out and include it in stuff that needs allocating
			List<MoveableBlock> displacedBlocks = new LinkedList<>();
			bestOption.attemptToAdd(data, displacedBlocks);
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
