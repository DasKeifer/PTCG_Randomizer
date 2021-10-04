package romAddressing;


public class AddressRange 
{
	int start;
	int stopExclusive;
	
	public AddressRange(int start, int stopExclusive)
	{
		if (start >= stopExclusive)
		{
			throw new IllegalArgumentException("AddressRange: The exclusive stop passed (" + stopExclusive + ") was equal to or before the start (" + start + ")");
		}
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
		return start <= doesContain.start && stopExclusive >= doesContain.stopExclusive;
	}
	
	public boolean overlaps(AddressRange toCheck)
	{
		// If it contains the start address or the last address, then it must overlap
		// Also overlaps it is contained entirely in the toCheck range
		return contains(toCheck.start) || contains(toCheck.stopExclusive - 1) || // -1 on end since its not inclusive
				toCheck.contains(this);
	}
	
	public AddressRange removeOverlap(AddressRange toRemove)
	{
		if (overlaps(toRemove))
		{
			// If this whole space is overlapped, set this to empty with invalid values
			if (toRemove.contains(this))
			{
				start = -1;
				stopExclusive = -1;
			}
			// if the space to remove its entirely contained, remove it from this
			// range by splitting it. We return the latter of the two new ranges
			if (contains(toRemove))
			{
				// Split this range up
				AddressRange newRange = new AddressRange(toRemove.stopExclusive, stopExclusive);
				stopExclusive = toRemove.start; 
				return newRange;
			}
			// else shorten this up based on the removed portion
			else if (contains(toRemove.start))
			{
				// If it contains the start, then the end must go past the end of this
				// one since we already checked for fully containing the time. Just
				// set the stop the the other's start
				stopExclusive = toRemove.start; 
			}
			// else it contains the stop
			else 
			{
				// If it contains the stop, then the start must be before the start of this
				// one since we already checked for fully containing the time. Just
				// set the start the the other's stop
				// We do -1 since the stop of what to remove is exclusive
				start = toRemove.stopExclusive;
			}
		}
		
		// No additional range added
		return null;
	}
	
	public AddressRange shiftNew(int shift)
	{
		return new AddressRange(start + shift, stopExclusive + shift);
	}
	
	public void shiftInPlace(int shift)
	{
		start += shift;
		stopExclusive += shift;
	}
	
	// Shrinks the start only
	public void shrink(int shrink)
	{
		start += shrink;
		if (start > stopExclusive)
		{
			start = stopExclusive;
		}
	}
	
	public int size()
	{
		return stopExclusive - start;
	}
	
	public boolean isEmpty()
	{
		return size() <= 0;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getStopExclusive()
	{
		return stopExclusive;
	}
}
