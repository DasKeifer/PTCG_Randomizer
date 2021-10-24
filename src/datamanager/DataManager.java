package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import constants.RomConstants;
import rom.Blocks;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;
import util.RomUtils;

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
		
		byte numBanks = (byte) Math.ceil((double) rawBytes.length / RomConstants.BANK_SIZE);
		for (byte bank = 0; bank < numBanks; bank++)
		{
			// Insert map for this bank
			AllocatableBank bankSpace = new AllocatableBank(bank);
			freeSpace.put(bank, bankSpace);
			
			determineFreeSpaceInBank(rawBytes, bank, bankSpace);
		}
	}

	private void determineFreeSpaceInBank(byte[] rawBytes, byte bankToCheck, AllocatableBank bank)
	{
		int[] bankBounds = RomUtils.getBankBounds(bankToCheck);
		// Loop through the bank looking for empty space
		int address = bankBounds[0];
		while (address <= bankBounds[1])
		{
			if (rawBytes[address] == (byte) 0xFF)
			{
				int spaceStart = address;
				while (++address < bankBounds[1] && rawBytes[address] == (byte) 0xFF);
				
				// If we found space, then save it to the map
				// We only save spaces that are at least x long to prevent finding locations that are probably
				// actually images which can be 0xFF
				if (address - spaceStart > 40)
				{
					bank.addSpace(spaceStart, address);
				}
			} 
			address++;
		}
	}
}
