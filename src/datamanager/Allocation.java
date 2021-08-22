package datamanager;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import compiler.CompilerUtils;
import util.ByteUtils;


public class Allocation 
{
	public static final Comparator<Allocation> PRIORITY_SORTER = new PrioritySorter();
	
	public Byte bank;
	public int address;
	public MoveableBlock data;
	// Include/have easy way to get segment offsets?
	private SortedSet<BankPreference> unattemptedAllowableBankPreferences;

	public Allocation(MoveableBlock data) 
	{
		this.data = data;
		this.bank = CompilerUtils.UNASSIGNED_BANK;
		this.address = CompilerUtils.UNASSIGNED_ADDRESS;
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
	
	public UnconstrainedMoveBlock shrinkIfPossible()
	{
		return data.shrinkIfPossible();
	}
	
	public byte popNextUnattemptedAllowableBank()
	{
		if (!isUnattemptedAllowableBanksEmpty())
		{
			return -1;
		}
		
		// get the next preference
		BankPreference pref = new BankPreference(unattemptedAllowableBankPreferences.first());
		byte prefId = pref.start;
		
		// Remove it from unattempted and update the preference
		removeUnattemptedBank(prefId);
		pref.start++;
		if (pref.isEmpty())
		{
			unattemptedAllowableBankPreferences.remove(pref);
		}
		
		// Return the id
		return prefId;
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
	}

	public void setAssignedAddress(int address)
	{
		this.address = address;
	}
	
	public int getCurrentWorstCaseSizeOnBank(byte bankToGetSizeOn, Map<String, Integer> allocatedIndexes)
	{
		return data.getCurrentWorstCaseSizeOnBank(address, bankToGetSizeOn, allocatedIndexes);
	}

	public void setAddressToUnassignedLocal() 
	{
		this.address = CompilerUtils.UNASSIGNED_LOCAL_ADDRESS;
	}

	public void clearBankAndAddress() 
	{
		this.bank = CompilerUtils.UNASSIGNED_BANK;
		this.address = CompilerUtils.UNASSIGNED_ADDRESS;
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
				// Give larger blocks higher priority
				compareVal = Integer.compare(a1.getCurrentWorstCaseSizeOnBank(a1.bank), a2.getCurrentWorstCaseSizeOnBank(a2.bank));
			}
			
			if (compareVal == 0)
			{
				// if all else fails, use the unique block ID so things like sets will recognize these are not the same object
				compareVal = a1.data.getId().compareTo(a2.data.getId());
			}
			
			return compareVal;
	    }
	}

	public byte removeAlloc() 
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
