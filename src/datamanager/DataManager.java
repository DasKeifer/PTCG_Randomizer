package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import constants.RomConstants;
import rom.Blocks;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;

public class DataManager
{	
	// BankId, bank object
	private SortedMap<Byte, AllocatableBank> freeSpace;
	private AssignedAddresses assignedAddresses;
	
	public DataManager()
	{
		freeSpace = new TreeMap<>();
		assignedAddresses = new AssignedAddresses();
	}
	
	public AssignedAddresses allocateBlocks(
			byte[] bytesToPlaceIn,
			Blocks blocks)
	{
		// Determine what space we have free
		determineAllFreeSpace(bytesToPlaceIn);
		
//		for (Entry<Byte, AllocatableBank> entry : freeSpace.entrySet())
//		{
//			System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
//		}
		
		// Assign fixed blocks first so the moveable ones can reference them
		allocateFixedBlocks(blocks);
	
		// Allocate space for the constrained blocks then the unconstrained ones
		if (tryToAssignBanks(blocks.getAllBlocksToAllocate()))
		{
			// If we were successful, assign the addresses for each item and then return
			// a copy of the data
			assignAddressesInBanks();
			return new AssignedAddresses(assignedAddresses);
		}
		
		return null;
	}
	
	private void allocateFixedBlocks(Blocks blocks)
	{
		// TODO: Not working or getting overwritten? Also should be done later
		
		// First assign them to their banks. This allows some optimization of sizes some
		for (FixedBlock block : blocks.getAllFixedBlocks())
		{
			BankAddress address = block.getFixedAddress();
			DataManagerUtils.assignBlockAndSegmentBanks(block, address.getBank(), assignedAddresses);
		}
		
		// Now go through and assign preliminary addresses and add them to the banks
		for (FixedBlock block : blocks.getAllFixedBlocks())
		{
			BankAddress address = block.getFixedAddress();
			DataManagerUtils.assignBlockAndSegmentBankAddresses(block, address, assignedAddresses);
			freeSpace.get(block.getFixedAddress().getBank()).addFixedBlock(block, assignedAddresses);
		}
	}
	
	private boolean tryToAssignBanks(List<MoveableBlock> toAlloc)
	{
		// Set/Reset each of the blocks working copy of the preferences
		for (MoveableBlock block : toAlloc)
		{
			block.resetBankPreferences();
		}
		
		// Then recursively try to pack them
		return tryToAssignBanksRecursor(toAlloc);
	}

	private boolean tryToAssignBanksRecursor(List<MoveableBlock> toAlloc)
	{		
		// Assign each alloc to its next preferred/allowable bank
		if (!assignAllocationsToTheirNextBank(toAlloc))
		{
			return false;
		}
		
		// Go through and remove any allocations that don't fit in the banks as
		// they are currently assigned
		List<MoveableBlock> allocsThatDontFit = new LinkedList<>();
		removeExcessAllocsFromBanks(allocsThatDontFit);
		
		// If all allocs fit, we are done
		if (allocsThatDontFit.isEmpty())
		{
			return true;
		}
		
		// Otherwise recurse again and see if the ones that didn't fit will fit in
		// their next bank (if they have one)
		return tryToAssignBanksRecursor(allocsThatDontFit);
	}
	
	private boolean assignAllocationsToTheirNextBank(List<MoveableBlock> toAlloc)
	{
		for (MoveableBlock alloc : toAlloc)
		{
			// Ran out of banks to try! Failed to allocate a block
			if (alloc.isUnattemptedAllowableBanksEmpty())
			{
				return false;
			}
			else
			{
				byte nextBank = alloc.popNextUnattemptedAllowableBank();
				AllocatableBank bank = freeSpace.get(nextBank);
				if (bank == null)
				{
					throw new RuntimeException(String.format("Popped next allowable bank (0x%x) for block " + 
							alloc.getId() + " but failed to get a reference to the bank! This should never "
							+ "happen if valid banks are given for the preferences", nextBank));
				}
				bank.addMoveableBlock(alloc);
				DataManagerUtils.assignBlockAndSegmentBanks(alloc, bank.bank, assignedAddresses);
			}
		}
		
		return true;
	}
	
	private void removeExcessAllocsFromBanks(List<MoveableBlock> allocsThatDontFit)
	{		
		removeExcessAllocsFromBanksRecursor(allocsThatDontFit);
	}

	private boolean removeExcessAllocsFromBanksRecursor(List<MoveableBlock> allocsThatDontFit)
	{	
		// We have to track this separately from the list since the list passed in may
		// not be empty
		boolean foundAllocThatDoesntFit = false;
		
		// For each bank, pack and remove any excess
		List<MoveableBlock> bankAllocsThatDontFit = new LinkedList<>();
		for (AllocatableBank bank : freeSpace.values())
		{
			foundAllocThatDoesntFit = bank.checkForAndRemoveExcessAllocs(bankAllocsThatDontFit, assignedAddresses) 
					|| foundAllocThatDoesntFit;
			allocsThatDontFit.addAll(bankAllocsThatDontFit);
		}
		
		// If we went through all banks and didn't find any that no longer fit, then we
		// have found all of them
		if (!foundAllocThatDoesntFit)
		{
			return foundAllocThatDoesntFit;
		}
		
		// Otherwise recurse again
		return removeExcessAllocsFromBanksRecursor(allocsThatDontFit);
	}
	
	private void assignAddressesInBanks()
	{
		// For each bank, assign actual addresses
		for (AllocatableBank bank : freeSpace.values())
		{
			bank.assignAddresses(assignedAddresses);
		}
	}
	
	// TODO later: we need to avoid images/gfx somehow - perhaps have it hardcoded which banks these occur in?
	// or maybe just have a separate file to read to describe space in rom?
	
	//engine banks 0-8 + 9 & a
	//effect functions: b (overflow to a?)
	//data banks c
	//text banks d-19 + 1a & 1b
	//gfx 1d-3b, 20 engine related to gfx, + 1f, 2f,30, 3c
	//audio 3d & 3e
	//sfx 3f
	
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
