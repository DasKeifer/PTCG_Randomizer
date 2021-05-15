package datamanager;


import compiler.DataBlock;
import util.ByteUtils;

public abstract class FixedBlock implements BlockAllocData
{
	int replaceLength;
	DataBlock replaceWith; // The remote block (if needed), should be referred to in the DataBlock so no need to track it here
	
	// TODO: Add optional integrity checking surrounding area and replace

	// For specific location writes with specific size or of empty data
	public FixedBlock(int startAddress, DataBlock toPlace)
	{
		replaceWith = toPlace;
		replaceWith.setAssignedAddress(startAddress);
		replaceLength = -1;
	}
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public FixedBlock(int startAddress, DataBlock replaceWith, int replaceLength)
	{
		this.replaceWith = replaceWith;
		replaceWith.setAssignedAddress(startAddress);
		this.replaceLength = replaceLength;
	}
	
	public int writeBytes(byte[] bytes)
	{
		int lengthWritten = replaceWith.writeBytes(bytes);
		
		if (replaceLength >= 0)
		{
			if (lengthWritten > replaceLength)
			{
				throw new IllegalArgumentException("Data written (" + lengthWritten + ") is larger than allowed size (" + replaceLength + ")");
			}
			else if (lengthWritten < replaceLength)
			{
				// Write the remainder of the length with no-ops
				ByteUtils.setBytes(bytes, replaceWith.getAssignedAddress() + lengthWritten, replaceLength - lengthWritten, (byte) 0);
			}
		}
		
		return lengthWritten;
	}
}
