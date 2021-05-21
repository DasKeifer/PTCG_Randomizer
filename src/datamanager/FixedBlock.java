package datamanager;


import compiler.DataBlock;
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
	
	public int writeBytes(byte[] bytes)
	{
		int lengthWritten = dataBlock.writeBytes(bytes);
		
		if (replaceLength >= 0)
		{
			if (lengthWritten > replaceLength)
			{
				throw new IllegalArgumentException("Data written (" + lengthWritten + ") is larger than allowed size (" + replaceLength + ")");
			}
			else if (lengthWritten < replaceLength)
			{
				// Write the remainder of the length with no-ops
				ByteUtils.setBytes(bytes, dataBlock.getAssignedAddress() + lengthWritten, replaceLength - lengthWritten, (byte) 0);
			}
		}
		
		return lengthWritten;
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
