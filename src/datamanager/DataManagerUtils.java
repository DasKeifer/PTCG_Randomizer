package datamanager;

import java.util.Iterator;

import compiler.DataBlock;
import constants.RomConstants;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;

public final class DataManagerUtils 
{
	private DataManagerUtils() {}
	
	public static void assignBlockAndSegmentBanks(DataBlock alloc, byte bank, AssignedAddresses assignedAddresses)
	{			
		for (String segId : alloc.getSegmentsById().keySet())
		{
			assignedAddresses.putBankOnly(segId, bank);
		}
	}

	public static void removeBlockAndSegmentAddresses(DataBlock alloc, AssignedAddresses assignedAddresses)
	{
		for (String segId : alloc.getSegmentsById().keySet())
		{
			assignedAddresses.remove(segId);
		}
	}
	
	static boolean debug = false;
	public static void assignBlockAndSegmentBankAddresses(DataBlock alloc, BankAddress blockAddress, AssignedAddresses assignedAddresses)
	{
		// Will throw if the addresses are invalid - means we can make some assumptions here
		AssignedAddresses relAddresses = alloc.getSegmentsRelativeAddresses(blockAddress, assignedAddresses);
		
		// For each segment relative address, offset it to the block address and add it to the
		// allocated indexes
		if (debug)
		{
			System.out.println("block address " + blockAddress);
		}
		
		String segmentId;
		Iterator<String> segItr = alloc.getSegmentIds().iterator();
		while (segItr.hasNext())
		{
			segmentId = segItr.next();
			BankAddress relAddress = relAddresses.getThrow(segmentId);
			if (debug)
			{
				System.out.println("segment address " + relAddress);
			}
			int addressInBank = relAddress.getAddressInBank() + blockAddress.getAddressInBank();
			// Make sure we didn't pass the end of the bank
			if (addressInBank > RomConstants.BANK_SIZE)
			{
				throw new RuntimeException("assignBlockAndSegmentBankAddresses Passed the end of a bank "
						+ "while assigning addresses for segment (" + segmentId + "). Each data block "
						+ "should fit assuming the blocks were successfully packed in earlier stages");
			}
			// If its the end of the bank, its ok if its the last segment or the next segment
			// is the end of block placeholder segment
			else if (addressInBank == RomConstants.BANK_SIZE)
			{
				if (!segItr.hasNext() || segItr.next().equals(alloc.getEndSegmentId()))
				{
					assignedAddresses.put(segmentId, (byte) (relAddress.getBank() + 1), (short) 0);
				}
				else
				{
					throw new RuntimeException("assignBlockAndSegmentBankAddresses Reached the end of a bank while "
							+ "assigning addresses but there is still a segment (" + segmentId + ") to assign. Each "
							+ "data block should fit assuming the blocks were successfully packed in earlier stages");
				}
			}
			else
			{
				assignedAddresses.put(segmentId, relAddress.getBank(), (short) addressInBank);
			}
		}
	}
}
