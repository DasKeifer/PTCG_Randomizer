package datamanager;

import java.util.Comparator;

import util.ByteUtils;

public class BankPreference extends BankRange
{
	public static final Comparator<BankPreference> BASIC_SORTER = new BasicSorter();
	byte priority;
	
	public BankPreference(byte priority, byte start, byte stopExclusive)
	{
		super(start, stopExclusive);
		this.priority = priority;
	}
	
	public BankPreference(BankPreference toCopy)
	{
		super(toCopy);
		this.priority = toCopy.priority;
	}
	
	public static class BasicSorter implements Comparator<BankPreference>
	{
		public int compare(BankPreference p1, BankPreference p2)
	    {   
			int compareVal = ByteUtils.unsignedCompareBytes(p1.priority, p2.priority);
			
			if (compareVal == 0)
			{
				compareVal = ByteUtils.unsignedCompareBytes(p1.start, p2.start);
			}
			
			if (compareVal == 0)
			{
				compareVal = ByteUtils.unsignedCompareBytes(p1.stopExclusive, p2.stopExclusive);
			}
			
			return compareVal;
	    }
	}
}
