package rom;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import constants.RomConstants;
import util.RomUtils;

public class FreeSpaceManager
{
	private Map<Byte, Map<Integer, Integer>> freeSpace;
	
	public FreeSpaceManager(byte[] rawBytes)
	{
		freeSpace = new HashMap<>();
		determineAllFreeSpace(rawBytes);
	}
	
	private void determineAllFreeSpace(byte[] rawBytes)
	{
		freeSpace.clear();
		
		int spaceAddress;
		int address = 0;
		int nextBankBoundary;
		byte numBanks = (byte) Math.ceil((double) rawBytes.length / RomConstants.BANK_SIZE);
		for (byte bank = 0; bank < numBanks; bank++)
		{
			// Insert map for this bank
			Map<Integer, Integer> bankMap = new HashMap<>();
			freeSpace.put(bank, bankMap);
			
			// Determine where this bank ends
			nextBankBoundary = address + RomConstants.BANK_SIZE;
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
					if (address - spaceAddress > 2)
					{
						bankMap.put(spaceAddress, address - spaceAddress);
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
