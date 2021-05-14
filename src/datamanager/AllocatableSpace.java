package datamanager;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import compiler.CompilerUtils;
import util.RomUtils;

class AllocatableSpace extends AddressRange
{
	SortedMap<Byte, List<MoveableBlock>> allocationsByPriority;
	int nextStartAddress;
	
	public AllocatableSpace(int start, int stopExclusive)
	{
		super(start, stopExclusive);
		allocationsByPriority = new TreeMap<>();
		nextStartAddress = start;
	}
	
	public AllocatableSpace(AddressRange range)
	{
		super(range);
		allocationsByPriority = new TreeMap<>();
		nextStartAddress = start;
	}

	public void clear()
	{
		clear(false);
	}
	
	public void clear(boolean willStayInBank)
	{
		allocationsByPriority.clear();
		nextStartAddress = start;
		
		int unassignAddr = willStayInBank ? CompilerUtils.UNASSIGNED_LOCAL_ADDRESS : CompilerUtils.UNASSIGNED_ADDRESS;
		for (List<MoveableBlock> allocWithPriority : allocationsByPriority.values())
		{
			for (MoveableBlock alloc : allocWithPriority)
			{
				alloc.setAssignedAddress(unassignAddr);
			}
		}
	}
	
	// Any reassigning is handled by the bank clearing and re-adding blocks
	public boolean addAndAssignAddressIfSpaceLeft(MoveableBlock alloc)
	{
		int blockSizeOnBank = alloc.getCurrentSizeOnBank(RomUtils.determineBank(start)); 
		if (nextStartAddress + blockSizeOnBank > stopExclusive)
		{
			return false;
		}
		
		DataManagerUtils.addToPriorityMap(allocationsByPriority, alloc);
		alloc.setAssignedAddress(nextStartAddress);
		nextStartAddress += blockSizeOnBank;
		return true;
	}
	
	public int spaceLeft()
	{ 
		return stopExclusive - nextStartAddress;
	}
}
