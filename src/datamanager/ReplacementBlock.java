package datamanager;

import java.util.List;

import compiler.staticInstructs.Nop;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;
import util.RomUtils;

public class ReplacementBlock extends FixedBlock
{
	int replaceLength;
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public ReplacementBlock(String startingSegmentName, int fixedStartAddress)
	{
		super(startingSegmentName, fixedStartAddress);
		setCommonData(-1);
	}
	
	public ReplacementBlock(String startingSegmentName, int fixedStartAddress, int replaceLength)
	{
		super(startingSegmentName, fixedStartAddress);
		setCommonData(replaceLength);
	}
	
	public ReplacementBlock(List<String> sourceLines, int fixedStartAddress, int replaceLength)
	{
		super(sourceLines, fixedStartAddress);
		setCommonData(replaceLength);
	}
	
	private void setCommonData(int replaceLength)
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

	static boolean debug = false;
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

//		debug = getId().contains("MoreEffect");
		if (debug)
		{
			System.out.println("HCE - " + getId());
			int address = RomUtils.convertToGlobalAddress(getFixedAddress());
			for (int i = address; i < address + replaceLength; i++)
			{
				System.out.println(String.format("0x%x - 0x%x", i, bytes[i]));
			}
		}
	}
}
