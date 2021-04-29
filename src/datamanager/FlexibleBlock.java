package datamanager;

import java.util.Map.Entry;
import java.util.TreeMap;

import compiler.CodeSnippit;

public abstract class FlexibleBlock 
{
	private byte assignedBank;
	private int assignedAddress;
	
	private byte priority;
	protected CodeSnippit toAdd;
	protected TreeMap<Byte, BankRange> allowableBankPreferences;
	
	protected FlexibleBlock(byte priority, CodeSnippit toPlaceInBank)
	{
		this.priority = priority;
		toAdd = new CodeSnippit(toPlaceInBank);
		allowableBankPreferences = new TreeMap<>();
	}
	
	public int writeData(byte[] bytes, int index)
	{
		return toAdd.writeData(bytes, index);
	}
	
	protected void addAllowableBankRange(byte priority, byte startBank, byte stopBank)
	{
		if (startBank > stopBank)
		{
			throw new UnsupportedOperationException("Start bank is after the end bank!");
		}
		
		allowableBankPreferences.put(priority, new BankRange(startBank, stopBank));
	}
	
	void setAssignedBank(byte bank)
	{
		assignedBank = bank;
	}
	
	void setAssignedAddress(int address)
	{
		assignedAddress = address;
	}
	
	byte getAssignedBank()
	{
		return assignedBank;
	}
	
	int getAssignedAddress()
	{
		return assignedAddress;
	}
	
	public TreeMap<Byte, BankRange> getPreferencedAllowableBanks()
	{
		TreeMap<Byte, BankRange> copy = new TreeMap<>();
		for (Entry<Byte, BankRange> entry : allowableBankPreferences.entrySet())
		{
			copy.put(entry.getKey(), new BankRange(entry.getValue()));
		}
		return copy;
	}
	
	public int getMaxSizeOnBank(byte bank)
	{
		return toAdd.getMaxSizeOnBank(bank);
	}
	
	public abstract int getMinimalSize();
	public abstract boolean hasMinimalOption();
	
	public byte getPriority()
	{
		return priority;
	}
}
