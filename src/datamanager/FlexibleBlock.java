package datamanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import compiler.CodeSnippit;

public abstract class FlexibleBlock 
{
	private byte priority;
	protected CodeSnippit toAdd;
	protected Map<Byte, AddressRange> allowableAddressPreferences;
	
	protected FlexibleBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		this.priority = priority;
		toAdd = new CodeSnippit(toPlaceInBank);
		allowableAddressPreferences = new HashMap<>();
	}
	
	public void addAllowableAddressRange(byte priority, int globalAddressStart, int globalAddressEnd)
	{
		if (globalAddressStart > globalAddressEnd)
		{
			throw new UnsupportedOperationException("Start address is after the end address!");
		}
		
		allowableAddressPreferences.put(priority, new AddressRange(globalAddressStart, globalAddressEnd));
	}
	
	public Map<Byte, AddressRange> getPreferencedAllowableAddresses()
	{
		Map<Byte, AddressRange> copy = new HashMap<>();
		for (Entry<Byte, AddressRange> entry : allowableAddressPreferences.entrySet())
		{
			copy.put(entry.getKey(), new AddressRange(entry.getValue()));
		}
		return copy;
	}
	
	public int getFullSize()
	{
		// TODO
		return 0;
	}
	
	public int getMinimalSize()
	{
		// TODO
		return 0;
	}
	
	public boolean hasMinimalOption()
	{
		// TODO
		return false;
	}
	
	public byte getPriority()
	{
		return priority;
	}
}
