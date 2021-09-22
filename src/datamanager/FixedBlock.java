package datamanager;


import compiler.DataBlock;
import util.RomUtils;

public class FixedBlock extends BlockAllocData
{
	private int address;
	// The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	public FixedBlock(int fixedStartAddress, DataBlock data)
	{
		super(data);
		address = fixedStartAddress;
	}

	// TODO change to new type?
	public int getFixedAddress() 
	{
		return address;
	}
	
	@Override
	public void writeData(byte[] bytes, AllocatedIndexes allocatedIndexes)
	{	
		BankAddress bankAddress = allocatedIndexes.getThrow(getId());
		int globalAddress = RomUtils.convertToGlobalAddress(bankAddress.bank, bankAddress.addressInBank);
		if (globalAddress != address)
		{
			throw new IllegalArgumentException("Passed address for FixedBlock (" + globalAddress + ") does" +
					"not match the fixed address (" + address + ")!"); 
		}
		dataBlock.writeBytes(bytes, allocatedIndexes);
	}

	public int getWorstCaseSize(AllocatedIndexes allocatedIndexes) 
	{
		return dataBlock.getWorstCaseSize(allocatedIndexes);
	}
}
