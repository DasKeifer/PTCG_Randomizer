package datamanager;


import compiler.DataBlock;
import util.ByteUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class MoveableBlock extends BlockAllocData
{
	public static final Comparator<MoveableBlock> PRIORITY_SORTER = new PrioritySorter();
	
	private SortedSet<BankPreference> allowableBankPreferences;
	private SortedSet<BankPreference> unattemptedAllowableBankPreferences;
	
	public MoveableBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
	{
		super(toPlaceInBank);
		
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
	
	public SortedSet<BankPreference> getAllowableBankPreferences()
	{
		SortedSet<BankPreference> copy = new TreeSet<>(BankPreference.BASIC_SORTER);
		for (BankPreference pref : allowableBankPreferences)
		{
			copy.add(new BankPreference(pref));
		}
		return copy;
	}
	
	public int getCurrentWorstCaseSize(AllocatedIndexes allocatedIndexes)
	{
		return dataBlock.getWorstCaseSize(allocatedIndexes);
	}
	
	// Package
	void resetBankPreferences()
	{
		unattemptedAllowableBankPreferences = getAllowableBankPreferences();
	}
	
	// Package
	boolean isUnattemptedAllowableBanksEmpty()
	{
		return unattemptedAllowableBankPreferences.isEmpty();
	}
	
	// Package
	byte popNextUnattemptedAllowableBank()
	{
		if (isUnattemptedAllowableBanksEmpty())
		{
			return -1;
		}
		
		// get the next preference
		BankPreference pref = new BankPreference(unattemptedAllowableBankPreferences.first());
		byte prefId = pref.start;
		
		// Remove it from unattempted and update the preference
		removeUnattemptedBank(prefId);
		if (pref.isEmpty())
		{
			unattemptedAllowableBankPreferences.remove(pref);
		}
		
		// Return the id
		return prefId;
	}
	
	// Package
	byte getNextUnatemptedAllowableBankPriority()
	{
		if (unattemptedAllowableBankPreferences.isEmpty())
		{
			return Byte.MAX_VALUE;
		}
		return unattemptedAllowableBankPreferences.first().priority;
	}
	
	private void removeUnattemptedBank(byte bank)
	{
		List<BankPreference> modified = new LinkedList<BankPreference>();
		Iterator<BankPreference> iter = unattemptedAllowableBankPreferences.iterator();
		BankPreference currPref;
		while (iter.hasNext())
		{
			currPref = iter.next();
			if (currPref.contains(bank))
			{
				// We always start with the first one in the range so that makes things easier since we don't have to worry about splitting banks
				iter.remove();
				currPref.start = (byte) (bank + 1);
				if (!currPref.isEmpty())
				{
					modified.add(currPref);
				}
			}
		}
		
		for (BankPreference pref : modified)
		{
			unattemptedAllowableBankPreferences.add(pref);
		}
	}
	
	public static class PrioritySorter implements Comparator<MoveableBlock>
	{
		public int compare(MoveableBlock a1, MoveableBlock a2)
	    {   
			int compareVal = ByteUtils.unsignedCompareBytes(a1.getNextUnatemptedAllowableBankPriority(), a2.getNextUnatemptedAllowableBankPriority());
			
			if (compareVal == 0)
			{
				// Give larger blocks higher priority - We have to do it agnostic to where things are
				// allocated but that is okay as this does not need to be 100% accurate
				final AllocatedIndexes emptyAllocs = new AllocatedIndexes();
				compareVal = Integer.compare(a1.getCurrentWorstCaseSize(emptyAllocs), a2.getCurrentWorstCaseSize(emptyAllocs));
			}
			
			if (compareVal == 0)
			{
				// if all else fails, use the unique block ID so things like sets will recognize these are not the same object
				compareVal = a1.getId().compareTo(a2.getId());
			}
			
			return compareVal;
	    }
	}
}
