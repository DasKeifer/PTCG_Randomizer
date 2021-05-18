package datamanager;


class AddressRange 
{
	int start;
	int stopExclusive;
	
	public AddressRange(int start, int stopExclusive)
	{
		this.start = start;
		this.stopExclusive = stopExclusive;
	}
	
	public AddressRange(AddressRange toCopy)
	{
		this(toCopy.start, toCopy.stopExclusive);
	}
	
	public int size()
	{
		return stopExclusive - start;
	}
}
