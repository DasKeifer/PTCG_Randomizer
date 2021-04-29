package datamanager;

import java.util.Map.Entry;
import java.util.TreeMap;

import compiler.CodeSnippit;

public abstract class FlexibleBlock 
{
	private byte priority;
	protected CodeSnippit toAdd;
	protected TreeMap<Byte, AddressRange> allowableAddressPreferences;
	
	protected FlexibleBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		this.priority = priority;
		toAdd = new CodeSnippit(toPlaceInBank);
		allowableAddressPreferences = new TreeMap<>();
	}
	
	public int writeData(byte[] bytes, int index)
	{
		return toAdd.writeData(byte[] bytes, int index);
	}
	
	protected void addAllowableAddressRange(byte priority, int globalAddressStart, int globalAddressEnd)
	{
		if (globalAddressStart > globalAddressEnd)
		{
			throw new UnsupportedOperationException("Start address is after the end address!");
		}
		
		allowableAddressPreferences.put(priority, new AddressRange(globalAddressStart, globalAddressEnd));
	}
	
	public TreeMap<Byte, AddressRange> getPreferencedAllowableAddresses()
	{
		TreeMap<Byte, AddressRange> copy = new TreeMap<>();
		for (Entry<Byte, AddressRange> entry : allowableAddressPreferences.entrySet())
		{
			copy.put(entry.getKey(), new AddressRange(entry.getValue()));
		}
		return copy;
	}
	
	public int getFullSize()
	{
		return toAdd.getSizeOnBank();
	}
	
	public abstract int getMinimalSize();
	public abstract boolean hasMinimalOption();
	
	public byte getPriority()
	{
		return priority;
	}
}
