package datamanager;

import java.util.HashMap;

public class AllocatedIndexes extends HashMap<String, BankAddress>
{
	private static final long serialVersionUID = 42L;

	public AllocatedIndexes() {}
	
	public AllocatedIndexes(AllocatedIndexes allocIndexes) 
	{
		super(allocIndexes);
	}
	
	public void addSetBank(String key, byte bank)
	{
		if (containsKey(key))
		{
			get(key).bank = bank;
		}
		else
		{
			put(key, new BankAddress(bank, BankAddress.UNASSIGNED_ADDRESS));
		}
	}

	public BankAddress getThrow(String segmentId)
	{
		return get(segmentId);
	}
	
	public BankAddress getTry(String segmentId)
	{
		BankAddress val = get(segmentId);
		if (val == null)
		{
			return BankAddress.UNASSIGNED;
		}
		return val;
	}

	public void setAddressInBank(String segmentId, short addressInBank) 
	{
		getThrow(segmentId).addressInBank = addressInBank;
	}
}
