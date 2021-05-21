package compiler.dynamic;

import java.util.Map;

import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.SegmentReference;
import rom.Texts;
import util.ByteUtils;

public class BlockAddress extends Instruction
{
	String labelToGoTo;
	protected SegmentReference toGoTo;
	int addressToGoTo;
	int numBytes;
	int offset;
	
	public BlockAddress(String labelToGoTo, int numBytes, int offset)
	{
		this.labelToGoTo = labelToGoTo;
		toGoTo = null;
		addressToGoTo = CompilerUtils.UNASSIGNED_ADDRESS;
		this.numBytes = numBytes;
		this.offset = offset;
	}
	
	public BlockAddress(int addressToGoTo, int numBytes, int offset)
	{
		labelToGoTo = "";
		toGoTo = null;
		this.addressToGoTo = addressToGoTo;
		this.numBytes = numBytes;
		this.offset = offset;
	}
	
	@Override
	public void linkData(
			Texts romTexts,
			Map<String, SegmentReference> labelToLocalSegment, 
			Map<String, SegmentReference> labelToSegment
	) 
	{		
		// If its calling a label and is not already assigned
		if (!labelToGoTo.isEmpty() && toGoTo == null)
		{
			// No need to check for local ones - they are treated the same
			toGoTo = labelToSegment.get(labelToGoTo);
			if (toGoTo == null)
			{
				throw new IllegalArgumentException("Specified label '" + labelToGoTo + "' was not found!");
			}
		}
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instOffset)
	{
		return numBytes;
	}
	
	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt) 
	{
		int address = addressToGoTo;
		if (addressToGoTo == CompilerUtils.UNASSIGNED_ADDRESS)
		{
			if (toGoTo != null)
			{
				address = toGoTo.getAssignedAddress();
			}
			else
			{
				throw new IllegalAccessError("Block Addresss must be linked prior to writting!");
			}
		}
		
		ByteUtils.writeLittleEndian(address - offset, bytes, addressToWriteAt, numBytes);
		return numBytes;
	}
}
