package compiler.fixed;

import java.util.Map;

import compiler.Instruction;
import compiler.Segment;
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
	
	public abstract int writeBytes(byte[] bytes, int indexToWriteAt);

	@Override
	public void linkData(
			Texts romTexts,
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
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

	@Override
	public int writeBytes(byte[] bytes, int blockStartIdx, int writeOffset) 
	{
		return writeBytes(bytes, blockStartIdx + writeOffset);
	}
}
