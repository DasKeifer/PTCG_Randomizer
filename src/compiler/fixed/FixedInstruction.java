package compiler.fixed;

import java.util.Map;

import compiler.Instruction;
import compiler.SegmentReference;
import rom.Texts;

public abstract class FixedInstruction extends Instruction
{
	private int size;
	
	protected FixedInstruction(int size) 
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return size;
	}
	
	@Override
	public void linkData(
			Texts romTexts,
			Map<String, SegmentReference> labelToLocalSegment, 
			Map<String, SegmentReference> labelToSegment
	) 
	{
		// Nothing to do since these don't have placeholders
		return;
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset)
	{
		return getSize();
	}
}
