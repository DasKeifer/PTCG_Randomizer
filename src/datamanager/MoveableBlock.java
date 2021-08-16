package datamanager;


import compiler.DataBlock;

import java.util.SortedSet;
import java.util.TreeSet;

public abstract class MoveableBlock extends BlockAllocData
{
	private boolean shrunkMoved;
	private SortedSet<BankPreference> allowableBankPreferences;
	
	protected MoveableBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
	{
		super(toPlaceInBank);
		
		shrunkMoved = false;
		allowableBankPreferences = new TreeSet<>(BankPreference.BASIC_SORTER);
		
		for (BankPreference pref : prefs)
		{
			addAllowableBankRange(pref);
		}
	}

	public void addAllowableBankRange(BankPreference bankPref)
	{
		addAllowableBankRange(bankPref.priority, bankPref.start, bankPref.stopExclusive);
	}
	
	public void addAllowableBankRange(byte priority, BankRange bankRange)
	{
		addAllowableBankRange(priority, bankRange.start, bankRange.stopExclusive);
	}
	
	public void addAllowableBankRange(byte priority, byte startBank, byte stopBank)
	{
		if (startBank > stopBank)
		{
			throw new UnsupportedOperationException("Start bank is after the end bank!");
		}
		
		allowableBankPreferences.add(new BankPreference(priority, startBank, stopBank));
	}

	// TODO: remove
	public void setAssignedAddress(int address) 
	{
		dataBlock.setAssignedAddress(address);
	}
	
	void setShrunkOrMoved(boolean isShrunkOrMoved)
	{
		shrunkMoved = isShrunkOrMoved;
	}

	boolean shrinkIfPossible()
	{
		if (canBeShrunkOrMoved() && !movesNotShrinks() && !isShrunkOrMoved())
		{
			setShrunkOrMoved(true);
			return true;
		}
		return false;
	}
	
	boolean isShrunkOrMoved()
	{
		return shrunkMoved;
	}
	
	boolean canBeShrunkOrMoved()
	{
		return !isShrunkOrMoved();
	}
	
	public SortedSet<BankPreference> getAllowableBankPreferences()
	{
		SortedSet<BankPreference> copy = new TreeSet<>();
		for (BankPreference pref : allowableBankPreferences)
		{
			copy.add(new BankPreference(pref));
		}
		return copy;
	}
	
	public abstract boolean movesNotShrinks();
	public abstract UnconstrainedMoveBlock getRemoteBlock();	
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
}
