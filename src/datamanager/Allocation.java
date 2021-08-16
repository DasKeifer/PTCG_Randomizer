package datamanager;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import util.ByteUtils;

public class Allocation 
{
	public static final Comparator<Allocation> PRIORITY_SORTER = new PrioritySorter();
	
	public Byte bank;
	public int address;
	public MoveableBlock data;
	private SortedSet<BankPreference> unattemptedAllowableBankPreferences;

	public Allocation(MoveableBlock data) 
	{
		this.data = data;
		this.bank = -1;
		this.address = -1;
		unattemptedAllowableBankPreferences = new TreeSet<>(BankPreference.BASIC_SORTER);
	}
	
	public void resetBankPreferences()
	{
		unattemptedAllowableBankPreferences = data.getAllowableBankPreferences();
	}
	
	public boolean isUnattemptedAllowableBanksEmpty()
	{
		return unattemptedAllowableBankPreferences.isEmpty();
	}
	
	public boolean shrinkIfPossible()
	{
		return data.shrinkIfPossible();
	}
	
	public byte popNextUnattemptedAllowableBank()
	{
		if (!isUnattemptedAllowableBanksEmpty())
		{
			return null;
		}
		BankPreference pref = new BankPreference(unattemptedAllowableBankPreferences.first());
		removeUnattemptedBank(pref.start);
		return pref;
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
				currPref.start = bank;
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
	
	public void setAssignedBank(byte bank)
	{
		this.bank = bank;
		data.setAssignedAddress(address);
	}

	public void setAssignedAddress(int address)
	{
		this.address = address;
		data.setAssignedAddress(address); // TODO: remove
	}

	public void clearAddress() 
	{
		this.address = -1;
	}

	public void clearBankAndAddress() 
	{
		this.bank = -1;
		this.address = -1;
	}
	
	private byte getNextUnatemptedAllowableBankPriority()
	{
		return unattemptedAllowableBankPreferences.first().priority;
	}
	
	public static class PrioritySorter implements Comparator<Allocation>
	{
		public int compare(Allocation a1, Allocation a2)
	    {   
			int compareVal = ByteUtils.unsignedCompareBytes(a1.getNextUnatemptedAllowableBankPriority(), a2.getNextUnatemptedAllowableBankPriority());
			
			if (compareVal == 0)
			{
				// TODO: Declare and make sure instructions can handle a -1 bank
				// Give larger blocks higher priority (TODO: or lower depending on how we want to pack)
				compareVal = Integer.compare(a1.data.getCurrentWorstCaseSizeOnBank(a1.bank), a2.data.getCurrentWorstCaseSizeOnBank(a2.bank));
			}
			
			if (compareVal == 0)
			{
				// if all else fails, use the unique block ID so things like sets will recognize these are not the same object
				compareVal = a1.data.getId().compareTo(a2.data.getId());
			}
			
			return compareVal;
	    }
	}
}
