package datamanager;

import compiler.DataBlock;
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
	
	public static void assignBlockAndSegmentBankAddresses(DataBlock alloc, BankAddress blockAddress, AssignedAddresses assignedAddresses)
	{
		AssignedAddresses relAddresses = alloc.getSegmentsRelativeAddresses(blockAddress, assignedAddresses);
		
		// For each segment relative address, offset it to the block address and add it to the
		// allocated indexes
		for (String segment : alloc.getSegmentIds())
		{
			BankAddress relAddress = relAddresses.getThrow(segment);
			assignedAddresses.put(segment, relAddress.getBank(), (short) (relAddress.getAddressInBank() + blockAddress.getAddressInBank()));
		}
	}
	

}
