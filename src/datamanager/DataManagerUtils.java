package datamanager;

import compiler.DataBlock;
import constants.RomConstants;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;

public class DataManagerUtils 
{
	public static void assignBlockAndSegmentBanks(DataBlock alloc, byte bank, AssignedAddresses assignedAddresses)
	{			
		for (String segId : alloc.getSegmentsById().keySet())
		{
			assignedAddresses.addSetBank(segId, bank);
		}
	}

	public static void removeBlockAndSegmentAddresses(DataBlock alloc, AssignedAddresses assignedAddresses)
	{
		for (String segId : alloc.getSegmentsById().keySet())
		{
			assignedAddresses.remove(segId);
		}
	}
	
	static boolean debug = true;
	public static void assignBlockAndSegmentBankAddresses(DataBlock alloc, BankAddress blockAddress, AssignedAddresses assignedAddresses)
	{
		AssignedAddresses relAddresses = alloc.getSegmentsRelativeAddresses(blockAddress, assignedAddresses);
		
		// For each segment relative address, offset it to the block address and add it to the
		// allocated indexes
		if (debug)
		{
			System.out.println("block address " + blockAddress);
		}
		
		for (String segment : alloc.getSegmentIds())
		{
			BankAddress relAddress = relAddresses.getThrow(segment);
			if (debug)
			{
				System.out.println("segment address " + relAddress);
			}
			int addressInBank = relAddress.getAddressInBank() + blockAddress.getAddressInBank();
			if (addressInBank == RomConstants.BANK_SIZE)
			{
				// TODO: check if end of seg
				assignedAddresses.put(segment, (byte) (relAddress.getBank() + 1), (short) 0);
			}
			else
			{
				assignedAddresses.put(segment, relAddress.getBank(), (short) addressInBank);
			}
		}
	}
}
