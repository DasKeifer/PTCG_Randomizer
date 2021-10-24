package datamanager;

import java.util.List;

import compiler.static_instructs.Nop;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;
import util.RomUtils;

public class ReplacementBlock extends FixedBlock
{
	int replaceLength;
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public ReplacementBlock(String startingSegmentName, int fixedStartAddress)
	{
		super(startingSegmentName, fixedStartAddress);
		setReplacementBlockCommonData(-1);
	}
	
	public ReplacementBlock(String startingSegmentName, int fixedStartAddress, int replaceLength)
	{
		super(startingSegmentName, fixedStartAddress);
		setReplacementBlockCommonData(replaceLength);
	}
	
	public ReplacementBlock(List<String> sourceLines, int fixedStartAddress, int replaceLength)
	{
		super(sourceLines, fixedStartAddress);
		setReplacementBlockCommonData(replaceLength);
	}
	
	private void setReplacementBlockCommonData(int replaceLength)
	{
		this.replaceLength = replaceLength;
	}
	
	public void setReplaceLength(int length)
	{
		replaceLength = length;
	}	
	
	public int getSize()
	{
		return replaceLength;
	}
	
	@Override 
	public int getWorstCaseSize(AssignedAddresses unused)
	{
		return getSize();
	}
	
	@Override
	public AssignedAddresses getSegmentsRelativeAddresses(BankAddress blockAddress, AssignedAddresses assignedAddresses)
	{
		AssignedAddresses relAddresses = new AssignedAddresses();
		getSizeAndSegmentsRelativeAddresses(blockAddress, assignedAddresses, relAddresses, false); // false = don't include end segment
		relAddresses.put(getEndSegmentId(), new BankAddress(blockAddress.getBank(), (short) getSize()));
		return relAddresses;
	}

	@Override
	public void writeBytes(byte[] bytes, AssignedAddresses assignedAddresses)
	{
		if (replaceLength <= 0)
		{
			throw new IllegalArgumentException("ReplacementBlock replaceLength is invalid (" + replaceLength + ") - it must be greater than 0");
		}
		
		// Preliminary safety check
		// TODO later:  handle non-continuous write detection?
		int blockSize = super.getWorstCaseSize(assignedAddresses);
		if (blockSize > replaceLength)
		{
			throw new IllegalArgumentException("ReplacementBlock Data size (" + blockSize + ") is larger than allowed size (" + replaceLength + ")");
		}
		
		// Now that we know if fits, write the meat of the data
		super.writeBytes(bytes, assignedAddresses);
		
		// Now we need to see if we need to append nops at the end
		if (blockSize < replaceLength)
		{
			// Create the nop of the size remaining and write it where the other one left off
			(new Nop(replaceLength - blockSize)).writeBytes(bytes, RomUtils.convertToGlobalAddress(getFixedAddress()) + blockSize, assignedAddresses);
		}
	}
}
