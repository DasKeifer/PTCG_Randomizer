package romAddressing;

import java.util.HashMap;
import java.util.HashSet;

public class AssignedAddresses 
{
	private HashMap<String, BankAddress> addresses;

	public AssignedAddresses()
	{
		addresses = new HashMap<>();
	}
	
	public AssignedAddresses(AssignedAddresses assignedAddresses) 
	{
		addresses = new HashMap<>(assignedAddresses.addresses);
	}
	
	public void remove(String key)
	{
		addresses.remove(key);
	}
	
	public void clear()
	{
		addresses.clear();
	}
	
	public void put(String key, BankAddress assignedAddress)
	{
		addresses.put(key, new BankAddress(assignedAddress));
	}
	
	public void put(String key, byte bank, short addressInBank)
	{
		addresses.put(key, new BankAddress(bank, addressInBank));
	}
	
	// Rename putBankOnly
	public void addSetBank(String key, byte bank)
	{
		if (addresses.containsKey(key))
		{
			addresses.get(key).setBank(bank);
		}
		else
		{
			addresses.put(key, new BankAddress(bank, BankAddress.UNASSIGNED_ADDRESS));
		}
	}

	// Rename putAddressInBankOnly
	public void setAddressInBank(String segmentId, short addressInBank) 
	{
		getThrow(segmentId).setAddressInBank(addressInBank);
	}

	public BankAddress getThrow(String segmentId)
	{
		BankAddress val = addresses.get(segmentId);
		if (val == null)
		{
			throw new IllegalArgumentException("assignedAddresses.getThrow failed to find BankAddress at " + segmentId);
		}
		return val;
	}
	
	public BankAddress getTry(String segmentId)
	{
		BankAddress val = addresses.get(segmentId);
		if (val == null)
		{
			return BankAddress.UNASSIGNED;
		}
		return val;
	}
	
	public HashSet<String> getKeys()
	{
		return new HashSet<>(addresses.keySet());
	}
}
