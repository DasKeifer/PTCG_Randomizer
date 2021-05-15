package datamanager;

import java.util.Map.Entry;

import compiler.DataBlock;

import java.util.SortedMap;
import java.util.TreeMap;

public abstract class MoveableBlock implements BlockAllocData
{
	private boolean shrunkMoved; // TODO make this temp somehow?
	private byte assignedBank; // TODO: used?
	
	private byte priority;
	protected DataBlock toAdd;
	protected TreeMap<Byte, BankRange> allowableBankPreferences;
	
	protected MoveableBlock(byte priority, DataBlock toPlaceInBank)
	{
		shrunkMoved = false;
		assignedBank = -1;
		
		this.priority = priority;
		// TODO: Copy? toAdd = new CodeSnippit(toPlaceInBank);
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
		toAdd.setAssignedAddress(address);
	}

	@Override
	public int getAddress() 
	{
		return toAdd.getAssignedAddress();
	}
	
	void setShrunkOrMoved(boolean isShrunkOrMoved)
	{
		shrunkMoved = isShrunkOrMoved;
	}
	
	@Override
	public String getId()
	{
		return toAdd.getId();
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
	
	public abstract boolean shrinksNotMoves();
	public abstract FloatingBlock applyShrink();	
	public abstract FloatingBlock revertShrink();
	public abstract int getShrunkWorstCaseSizeOnBank(byte bank);
	
	public int getCurrentWorstCaseSizeOnBank(byte bank)
	{
		if (shrunkMoved)
		{
			// If it moves instead of shrinking, we can reduce it to 0
			if (!shrinksNotMoves())
			{
				return 0;
			}
			
			// Otherwise get its shrunk size
			return getShrunkWorstCaseSizeOnBank(bank);
		}
		else
		{
			return toAdd.getWorstCaseSizeOnBank(bank);
		}
	}
	
	public int getCurrentSizeOnAssignedBank()
	{
		if (assignedBank < 0)
		{
			return -1;
		}
		return getCurrentWorstCaseSizeOnBank(assignedBank);
	}
	
	public byte getPriority()
	{
		return priority;
	}
	
	public int writeBytes(byte[] bytes)
	{
		return toAdd.writeBytes(bytes);
	}
}
