package datamanager;


import compiler.DataBlock;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public abstract class MoveableBlock extends BlockAllocData
{
	private boolean shrunkMoved;	
	private byte priority;
	protected TreeMap<Byte, BankRange> allowableBankPreferences;
	
	protected MoveableBlock(byte priority, DataBlock toPlaceInBank)
	{
		super(toPlaceInBank);
		
		shrunkMoved = false;
		this.priority = priority;
		allowableBankPreferences = new TreeMap<>();
	}
	
	protected void addAllowableBankRange(byte priority, byte startBank, byte stopBank)
	{
		if (startBank > stopBank)
		{
			throw new UnsupportedOperationException("Start bank is after the end bank!");
		}
		
		allowableBankPreferences.put(priority, new BankRange(startBank, stopBank));
	}

	public void setAssignedAddress(int address) 
	{
		dataBlock.setAssignedAddress(address);
	}

	public int getAddress() 
	{
		return dataBlock.getAssignedAddress();
	}
	
	void setShrunkOrMoved(boolean isShrunkOrMoved)
	{
		shrunkMoved = isShrunkOrMoved;
	}
	
	boolean isShrunkOrMoved()
	{
		return shrunkMoved;
	}
	
	boolean canBeShrunkOrMoved()
	{
		return !isShrunkOrMoved();
	}
		
	public SortedMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		SortedMap<Byte, BankRange> copy = new TreeMap<>();
		for (Entry<Byte, BankRange> entry : allowableBankPreferences.entrySet())
		{
			copy.put(entry.getKey(), new BankRange(entry.getValue()));
		}
		return copy;
	}
	
	public abstract boolean movesNotShrinks();
	public abstract FloatingBlock getRemoteBlock();	
	public abstract int getShrunkWorstCaseSizeOnBank(byte bank);
	
	public int getCurrentWorstCaseSizeOnBank(byte bank)
	{
		if (shrunkMoved)
		{			
			// Otherwise get its shrunk size
			return getShrunkWorstCaseSizeOnBank(bank);
		}
		else
		{
			return dataBlock.getWorstCaseSizeOnBank(bank);
		}
	}
	
	public byte getPriority()
	{
		return priority;
	}
	
	public int writeBytes(byte[] bytes)
	{
		return dataBlock.writeBytes(bytes);
	}
}
