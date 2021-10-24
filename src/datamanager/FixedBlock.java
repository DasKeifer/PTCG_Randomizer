package datamanager;


import java.util.List;

import compiler.DataBlock;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;

public class FixedBlock extends DataBlock
{
	private BankAddress address;
	// The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	public FixedBlock(String startingSegmentName, int fixedStartAddress)
	{
		super(startingSegmentName);
		setFixedBlockCommonData(fixedStartAddress);
	}
	
	public FixedBlock(List<String> sourceLines, int fixedStartAddress)
	{
		super(sourceLines);
		setFixedBlockCommonData(fixedStartAddress);
	}
	
	private void setFixedBlockCommonData(int fixedStartAddress)
	{
		address = new BankAddress(fixedStartAddress);
	}

	public BankAddress getFixedAddress() 
	{
		return new BankAddress(address);
	}
	
	@Override
	public void writeBytes(byte[] bytes, AssignedAddresses assignedAddresses)
	{	
		BankAddress bankAddress = assignedAddresses.getThrow(getId());
		if (!bankAddress.equals(address))
		{
			throw new IllegalArgumentException("Passed address for FixedBlock (" + bankAddress + ") does" +
					"not match the fixed address (" + address + ")!"); 
		}
		super.writeBytes(bytes, assignedAddresses);
	}
}
