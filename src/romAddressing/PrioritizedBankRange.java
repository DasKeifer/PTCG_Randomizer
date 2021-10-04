package romAddressing;

import java.util.Comparator;

import util.ByteUtils;

public class PrioritizedBankRange extends BankRange
{
	public static final Comparator<PrioritizedBankRange> BASIC_SORTER = new BasicSorter();
	byte priority;
	
	public PrioritizedBankRange(byte priority, byte start, byte stopExclusive)
	{
		super(start, stopExclusive);
		this.priority = priority;
	}
	
	public PrioritizedBankRange(byte priority, BankRange bankRange) 
	{
		super(bankRange);
		this.priority = priority;
	}
	
	public PrioritizedBankRange(PrioritizedBankRange toCopy)
	{
		super(toCopy);
		this.priority = toCopy.priority;
	}

	// TODO: Change to int for ease of use
	public byte getPriority()
	{
		return priority;
	}
	
	public static class BasicSorter implements Comparator<PrioritizedBankRange>
	{
		public int compare(PrioritizedBankRange p1, PrioritizedBankRange p2)
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
