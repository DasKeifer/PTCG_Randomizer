package datamanager;

import java.util.SortedMap;

// TODO: Compress into FlexibleBlock?
class AllocData 
{
	boolean shrunk;
	FlexibleBlock block;
	AllocData remoteAlloc;
	
	public AllocData(FlexibleBlock block)
	{
		shrunk = false;
		this.block = block; // Intentionally a reference and not a copy
	}
	
	public boolean canBeShrunk()
	{
		return !shrunk && block.hasMinimalOption();
	}

	public void setBank(byte bank)
	{
		block.setAssignedBank(bank);
	}
	
	public void setAddress(int address)
	{
		block.setAssignedAddress(address);
	}
	
	public AllocData createRemoteAlloc()
	{
		remoteAlloc = new AllocData(block.createRemoteBlock());
		return remoteAlloc;
	}
	
	public byte removeRemoteAlloc()
	{
		if (remoteAlloc != null)
		{
			return remoteAlloc.getBank();
		}
		return -1;
	}
	
	public byte getRemoteAllocPriority()
	{
		if (remoteAlloc != null)
		{
			return remoteAlloc.getPriority();
		}
		return -1;
	}
	
	public int getCurrentSize()
	{
		if (shrunk)
		{
			return block.getMinimalSize();
		}
		else
		{
			return block.getFullSize();
		}
	}
	
	public int getMinimalSize()
	{
		return block.getMinimalSize();
	}
	
	public byte getPriority()
	{
		return block.getPriority();
	}
	
	public byte getBank()
	{
		return block.getAssignedBank();
	}
	
	public SortedMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		return block.getPreferencedAllowableBanks();
	}
}
