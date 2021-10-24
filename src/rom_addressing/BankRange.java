package rom_addressing;

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
		if (start >= stopExclusive)
		{
			throw new IllegalArgumentException("BankRange: The exclusive stop passed (" + stopExclusive + ") was equal to or before the start (" + start + ")");
		}
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
	
	public boolean contains(byte bank)
	{
		return start <= bank && stopExclusive > bank;
	}

	// Shrinks the start only
	public void shrink(byte shrink)
	{
		start += shrink;
		if (start > stopExclusive)
		{
			start = stopExclusive;
		}
	}
	
	public byte getStart()
	{
		return start;
	}
	
	public byte getStopExclusive()
	{
		return stopExclusive;
	}
}
