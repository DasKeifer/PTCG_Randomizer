package datamanager;

import java.util.Map;

import compiler.CodeSnippit;

public class NoConstraintBlock extends FlexibleBlock
{
	protected NoConstraintBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		super(priority, toPlaceInBank);
	}
	
	protected void addAddressRangePreference(byte priority, int globalAddressStart, int globalAddressEnd)
	{
		addAllowableAddressRange(priority, globalAddressStart, globalAddressEnd);
	}

	@Override
	public TreeMap<Byte, AddressRange> getPreferencedAllowableAddresses()
	{
		Map<Byte, AddressRange> toReturn = super.getPreferencedAllowableAddresses();
		toReturn.put(Byte.MAX_VALUE, new AddressRange(0, Integer.MAX_VALUE));
		return toReturn;
	}

	@Override
	public int getMinimalSize()
	{
		return getFullSize();
	}

	@Override
	public boolean hasMinimalOption()
	{
		return false;
	}
}
