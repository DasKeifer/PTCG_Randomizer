package datamanager;

import java.util.Map.Entry;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import compiler.CodeSnippit;

public abstract class MoveableBlock 
{
	private boolean shrunkMoved; // TODO make this temp somehow?
	private byte assignedBank;
	
	private byte priority;
	protected CodeSnippit toAdd;
	protected TreeMap<Byte, BankRange> allowableBankPreferences;
	
	protected MoveableBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		shrunkMoved = false;
		assignedBank = -1;
		
		this.priority = priority;
		// TODO: Copy? toAdd = new CodeSnippit(toPlaceInBank);
		allowableBankPreferences = new TreeMap<>();
	}
	
	public int writeData(byte[] bytes, Map<String, Integer> blockIdsToAddresses)
	{
		return toAdd.writeData(bytes, blockIdsToAddresses);
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
	
	void setShrunkOrMoved(boolean isShrunkOrMoved)
	{
		shrunkMoved = isShrunkOrMoved;
	}
	
	String getId()
	{
		return toAdd.getId();
	}
	
	byte getAssignedBank()
	{
		return assignedBank;
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
	
	public int getCurrentSizeOnAssignedBank()
	{
		if (assignedBank < 0)
		{
			return -1;
		}
		return getCurrentSizeOnBank(assignedBank);
	}
	
	public byte getPriority()
	{
		return priority;
	}
}
