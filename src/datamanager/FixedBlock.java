package datamanager;


import compiler.DataBlock;

public class FixedBlock extends BlockAllocData
{
	private BankAddress address;
	// The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	public FixedBlock(int fixedStartAddress, DataBlock data)
	{
		super(data);
		address = new BankAddress(fixedStartAddress);
	}

	public BankAddress getFixedAddress() 
	{
		return new BankAddress(address);
	}
	
	@Override
	public void writeData(byte[] bytes, AllocatedIndexes allocatedIndexes)
	{	
		BankAddress bankAddress = allocatedIndexes.getThrow(getId());
		if (!bankAddress.equals(address))
		{
			throw new IllegalArgumentException("Passed address for FixedBlock (" + bankAddress + ") does" +
					"not match the fixed address (" + address + ")!"); 
		}
		dataBlock.writeBytes(bytes, allocatedIndexes);
	}

	public int getWorstCaseSize(AllocatedIndexes allocatedIndexes) 
	{
		return dataBlock.getWorstCaseSize(allocatedIndexes);
	}
}
