package datamanager;


import compiler.DataBlock;

import java.util.Map;
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
	
	void setShrunkOrMoved(boolean isShrunkOrMoved)
	{
		shrunkMoved = isShrunkOrMoved;
	}

	public boolean unshrinkIfPossible()
	{
		if (!movesNotShrinks() && isShrunkOrMoved())
		{
			setShrunkOrMoved(true);
			return true;
		}
		return false;
	}
	
	public UnconstrainedMoveBlock shrinkIfPossible()
	{
		if (canBeShrunkOrMoved() && !movesNotShrinks() && !isShrunkOrMoved())
		{
			setShrunkOrMoved(true);
			return getRemoteBlock();
		}
		return null;
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
	public abstract int getShrunkWorstCaseSizeOnBank(byte bankToGetSizeOn, int allocAddress, Map<String, Integer> allocatedIndexes);
	
	public int getCurrentWorstCaseSizeOnBank(int allocAddress, byte bankToGetSizeOn, Map<String, Integer> allocatedIndexes)
	{
		if (shrunkMoved)
		{			
			// Otherwise get its shrunk size
			return getShrunkWorstCaseSizeOnBank(bankToGetSizeOn, allocAddress, allocatedIndexes);
		}
		else
		{
			return dataBlock.getWorstCaseSizeOnBank(bankToGetSizeOn, allocAddress, allocatedIndexes);
		}
	}
}
