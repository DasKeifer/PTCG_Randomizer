package datamanager;


import compiler.DataBlock;
import compiler.staticInstructs.Nop;
import util.ByteUtils;
import util.RomUtils;

public class FixedBlock extends BlockAllocData
{
	int replaceLength;
	int address;
	// The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	// TODO: Add optional integrity checking surrounding area and replace
	// TODO: only allow fixed length data blocks to not have a replace length
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public FixedBlock(int startAddress, DataBlock replaceWith, int replaceLength)
	{
		super(replaceWith);
		address = startAddress;
		this.replaceLength = replaceLength;
	}
	
	// TODO: add a way to get the end of the fixed block to skip the nops

	public int getFixedAddress() 
	{
		return address;
	}
	
	@Override
	public void writeData(byte[] bytes, int assignedAddress)
	{
		if (replaceLength >= 0)
		{
			ByteUtils.setBytes(bytes, address, replaceLength, Nop.NOP_VALUE);
		}
		
		int lengthWritten = dataBlock.writeBytes(bytes, assignedAddress);
		
//		if (dataBlock.getId().contains("MoreEffectBanksTweak")) 
//		{
//			for (int i = getFixedAddress(); i < getFixedAddress() + replaceLength; i++)
//			{
//				System.out.println(String.format("0x%x - 0x%x", i, bytes[i]));
//			}
//		}
		
		// Safety Check
		if (lengthWritten > replaceLength)
		{
			throw new IllegalArgumentException("Data written (" + lengthWritten + ") is larger than allowed size (" + replaceLength + ")");
		}
	}

	public int size() 
	{
		if (replaceLength >= 0)
		{
			return replaceLength;
		}
		
		return dataBlock.getWorstCaseSizeOnBank(address, RomUtils.determineBank(getFixedAddress()));
	}
}
