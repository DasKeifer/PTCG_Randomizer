package datamanager;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import compiler.CodeSnippit;

public abstract class FlexibleBlock 
{
	private boolean shrunkMoved; // TODO make this temp somehow?
	private byte assignedBank;
	private int assignedAddress;
	
	private byte priority;
	protected CodeSnippit toAdd;
	protected TreeMap<Byte, BankRange> allowableBankPreferences;
	
	protected FlexibleBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		shrunkMoved = false;
		assignedBank = -1;
		assignedAddress = -1;
		
		this.priority = priority;
		// TODO: Copy? toAdd = new CodeSnippit(toPlaceInBank);
		allowableBankPreferences = new TreeMap<>();
	}
	
	public int writeData(byte[] bytes, int index)
	{
		// TODO: return toAdd.writeData(bytes, index);
		return 0;
	}
	
	protected void addAllowableBankRange(byte priority, byte startBank, byte stopBank)
	{
		if (startBank > stopBank)
		{
			throw new UnsupportedOperationException("Start bank is after the end bank!");
		}
		
		allowableBankPreferences.put(priority, new BankRange(startBank, stopBank));
	}
	
	void setAssignedBank(byte bank)
	{
		assignedBank = bank;
	}
	
	void setAssignedAddress(int address)
	{
		assignedAddress = address;
	}
	
	void setShrunkOrMoved(boolean isShrunkOrMoved)
	{
		shrunkMoved = isShrunkOrMoved;
	}
	
	byte getAssignedBank()
	{
		return assignedBank;
	}
	
	int getAssignedAddress()
	{
		return assignedAddress;
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
	public abstract NoConstraintBlock applyShrink();	
	public abstract NoConstraintBlock revertShrink();
	public abstract int getMinimalSizeOnBank(byte bank);
	
	public int getCurrentSizeOnBank(byte bank)
	{
		if (shrunkMoved)
		{
			// If it moves instead of shrinking, we can reduce it to 0
			if (!shrinksNotMoves())
			{
				return 0;
			}
			
			// Otherwise get its shrunk size
			return getMinimalSizeOnBank(bank);
		}
		else
		{
			return toAdd.getMaxSizeOnBank(bank);
		}
	}
	
	public byte getPriority()
	{
		return priority;
	}
}
