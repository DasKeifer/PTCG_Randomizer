package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.RomUtils;

class AllocatableSpace extends AddressRange
{
	// Not really necessary but convenient
	List<Allocation> allocs;
	int nextStartAddress;
	
	public AllocatableSpace(int start, int stopExclusive)
	{
		super(start, stopExclusive);
		allocs = new LinkedList<>();
		nextStartAddress = start;
	}
	
	public AllocatableSpace(AddressRange range)
	{
		super(range);
		allocs = new LinkedList<>();
		nextStartAddress = start;
	}
	
	public void clearAllocsAndAddressToUnassignedLocal()
	{
		for (Allocation alloc : allocs)
		{
			alloc.setAddressToUnassignedLocal();
		}
		
		allocs.clear();
		nextStartAddress = start;
	}
	
	// Any reassigning is handled by the bank clearing and re-adding blocks
	public boolean addAndAssignAddressIfSpaceLeft(Allocation alloc, Map<String, Integer> bankAllocAddresses)
	{
		int blockSizeOnBank = alloc.getCurrentWorstCaseSizeOnBank(RomUtils.determineBank(start), bankAllocAddresses); 
		if (nextStartAddress + blockSizeOnBank > stopExclusive)
		{
			return false;
		}
		
		alloc.setAssignedAddress(nextStartAddress);
		allocs.add(alloc);
		nextStartAddress += blockSizeOnBank;
		return true;
	}
	
	public int spaceLeft()
	{ 
		return stopExclusive - nextStartAddress;
	}
}
