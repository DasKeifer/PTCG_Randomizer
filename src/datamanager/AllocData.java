package datamanager;

import java.util.SortedMap;

class AllocData 
{
	boolean shrunk;
	FlexibleBlock block;
	
	public AllocData(FlexibleBlock block)
	{
		shrunk = false;
		this.block = block; // Intentionally a reference and not a copy
	}
	
	public boolean canBeShrunk()
	{
		return !shrunk && block.hasMinimalOption();
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
	
	public SortedMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		return block.getPreferencedAllowableBanks();
	}
}
