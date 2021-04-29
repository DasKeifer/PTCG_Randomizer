package datamanager;


class AddressRange 
{
	int start;
	int stop;
	
	public AddressRange(int start, int stop)
	{
		this.start = start;
		this.stop = stop;
	}
	
	public AddressRange(AddressRange toCopy)
	{
		this(toCopy.start, toCopy.stop);
	}
	
	public int size()
	{
		return stop - start;
	}
	
	// TODO default sorter based on start
}
