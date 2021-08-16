package datamanager;

public class BankRange 
{
	byte start;
	byte stopExclusive;
	
	public BankRange(byte bank) 
	{
		start = bank;
		stopExclusive = (byte) (bank + 1);
	}
	
	public BankRange(byte start, byte stopExclusive) 
	{
		this.start = start;
		this.stopExclusive = stopExclusive;
	}
	
	public BankRange(BankRange toCopy) 
	{
		start = toCopy.start;
		stopExclusive = toCopy.stopExclusive;
	}
	
	public boolean isEmpty()
	{
		return stopExclusive == start;
	}
	
	// TODO: only works for "postive" ranges
	public boolean contains(byte bank)
	{
		if (start <= bank && stopExclusive > bank)
		{
			return true;
		}
		return false;
	}
}
