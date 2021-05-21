package datamanager;

public class BankPreference extends BankRange
{
	byte priority;
	
	public BankPreference(byte priority, byte start, byte endExclusive)
	{
		super(start, endExclusive);
		this.priority = priority;
	}
}
