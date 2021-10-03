package datamanager;


import java.util.List;

import compiler.DataBlock;

public class FixedBlock extends DataBlock
{
	private BankAddress address;
	// The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	public FixedBlock(String startingSegmentName, int fixedStartAddress)
	{
		super(startingSegmentName);
		setCommonData(fixedStartAddress);
	}
	
	public FixedBlock(List<String> sourceLines, int fixedStartAddress)
	{
		super(sourceLines);
		setCommonData(fixedStartAddress);
	}
	
	private void setCommonData(int fixedStartAddress)
	{
		address = new BankAddress(fixedStartAddress);
	}

	public BankAddress getFixedAddress() 
	{
		return new BankAddress(address);
	}
	
	@Override
	public void writeBytes(byte[] bytes, AllocatedIndexes allocatedIndexes)
	{	
		BankAddress bankAddress = allocatedIndexes.getThrow(getId());
		if (!bankAddress.equals(address))
		{
			throw new IllegalArgumentException("Passed address for FixedBlock (" + bankAddress + ") does" +
					"not match the fixed address (" + address + ")!"); 
		}
		super.writeBytes(bytes, allocatedIndexes);
	}
}
