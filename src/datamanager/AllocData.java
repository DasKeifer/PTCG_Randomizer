package datamanager;

import java.util.TreeMap;

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
		return block.hasMinimalOption();
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
	
	public TreeMap<Byte, AddressRange> getPreferencedAllowableAddresses()
	{
		return block.getPreferencedAllowableAddresses();
	}
}
