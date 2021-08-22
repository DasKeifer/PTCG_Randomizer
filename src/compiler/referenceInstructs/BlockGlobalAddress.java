package compiler.referenceInstructs;

import java.util.Map;

import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.Segment;
import rom.Texts;
import util.ByteUtils;

public class BlockGlobalAddress extends Instruction
{
	final static int SIZE = 3;
	String labelToGoTo;
	protected Segment toGoTo;
	int offset;
	
	public BlockGlobalAddress(String labelToGoTo, int offset)
	{
		this.labelToGoTo = labelToGoTo;
		toGoTo = null;
		this.offset = offset;
	}

	@Override
	public void extractTexts(Texts texts)
	{
		// No texts in this instruct
	}
	
	@Override
	public void linkData(
			Texts romTexts,
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	) 
	{		
		// No need to check for local ones - they are treated the same
		toGoTo = labelToSegment.get(labelToGoTo);
		if (toGoTo == null)
		{
			throw new IllegalArgumentException("Specified label '" + labelToGoTo + "' was not found!");
		}
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instOffset)
	{
		return SIZE;
	}
	
	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt) 
	{
		if (toGoTo == null)
		{
			throw new IllegalAccessError("Block Addresss must be linked prior to writting!");
		}
		
		ByteUtils.writeLittleEndian(toGoTo.getAssignedAddress() - offset, bytes, addressToWriteAt, SIZE);
		return SIZE;
	}
}
