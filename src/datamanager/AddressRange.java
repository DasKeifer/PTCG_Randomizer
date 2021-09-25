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
	
	public boolean contains(int doesContain)
	{
		return start <= doesContain && stopExclusive < doesContain;
	}
	
	public boolean contains(AddressRange doesContain)
	{
		// TODO: test this
		return start <= doesContain.start && stopExclusive >= doesContain.stopExclusive;
	}
	
	public boolean overlaps(AddressRange toCheck)
	{
		// TODO: test this
		
		// If it contains the start address or the last address, then it must overlap
		// Also overlaps it is contained entirely in the toCheck range
		return contains(toCheck.start) || contains(toCheck.stopExclusive - 1) ||
				(start > toCheck.start && stopExclusive < toCheck.stopExclusive);
	}
	
	public AddressRange removeOverlap(AddressRange toRemove)
	{
		// TODO: test this - think its wrong - use above added methods
		if (start < toRemove.start)
		{
			// start earlier stop later - remove splits this
			if (stopExclusive > toRemove.stopExclusive)
			{
				stopExclusive = toRemove.start;
				return new AddressRange(toRemove.stopExclusive, stopExclusive);
			}
			// start earlier, stop in between - partial overlap
			else if (stopExclusive < toRemove.stopExclusive)
			{
				stopExclusive = toRemove.start;
			}
			// else: start earlier, stop early (or equal to) - no overlap
		}
		else if (start < toRemove.stopExclusive)
		{
			// start in between, stop in between - completely covered
			if (stopExclusive < toRemove.stopExclusive)
			{
				stopExclusive = start;
			}
			// start in between, stop later - partial overlap
			else
			{
				start = toRemove.stopExclusive;
			}
		}
		// else: start later - no overlap
		return null;
	}
	
	public int size()
	{
		return stopExclusive - start;
	}
	
	boolean isEmpty()
	{
		return size() <= 0;
	}
}
