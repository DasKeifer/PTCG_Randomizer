package datamanager;

import java.util.Map.Entry;

public class DataManagerUtils 
{
	public static void assignBlockAndSegmentBanks(BlockAllocData alloc, byte bank, AllocatedIndexes allocIndexes)
	{			
		for (String segId : alloc.dataBlock.getSegmentsById().keySet())
		{
			allocIndexes.addSetBank(segId, bank);
		}
	}

	public static void removeBlockAndSegmentAddresses(BlockAllocData alloc, AllocatedIndexes allocIndexes)
	{
		for (String segId : alloc.dataBlock.getSegmentsById().keySet())
		{
			allocIndexes.remove(segId);
		}
	}
	
	public static void assignBlockAndSegmentBankAddresses(BlockAllocData alloc, BankAddress blockAddress, AllocatedIndexes allocIndexes)
	{
		AllocatedIndexes relAddresses = alloc.dataBlock.getSegmentsRelativeAddresses(blockAddress, allocIndexes);
		
		// For each segment relative address, offset it to the block address and add it to the
		// allocated indexes
		for (Entry<String, BankAddress> segEntry : relAddresses.entrySet())
		{
			segEntry.getValue().addressInBank += blockAddress.addressInBank;
			allocIndexes.put(segEntry.getKey(), segEntry.getValue());
		}
	}
	

}
