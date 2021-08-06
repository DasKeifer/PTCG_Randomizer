package datamanager;


import compiler.DataBlock;
import compiler.staticInstructs.Nop;
import util.ByteUtils;
import util.RomUtils;

public class FixedBlock extends BlockAllocData
{
	int replaceLength;
	// The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	// TODO: Add optional integrity checking surrounding area and replace
	// TODO: only allow fixed length data blocks to not have a replace length
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public FixedBlock(int startAddress, DataBlock replaceWith, int replaceLength)
	{
		super(replaceWith);
		replaceWith.setAssignedAddress(startAddress);
		this.replaceLength = replaceLength;
	}

	public int getFixedAddress() 
	{
		return dataBlock.getAssignedAddress();
	}
	
	@Override
	public void writeData(byte[] bytes)
	{
		dataBlock.setAssignedAddress(getFixedAddress()); // TODO: handle more gracefully - linking after assigning addresses...?
		if (replaceLength >= 0)
		{
			ByteUtils.setBytes(bytes, dataBlock.getAssignedAddress(), replaceLength, Nop.NOP_VALUE);
		}
		
		int lengthWritten = dataBlock.writeBytes(bytes);
		
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
		
		return dataBlock.getWorstCaseSizeOnBank(RomUtils.determineBank(getFixedAddress()));
	}
}
