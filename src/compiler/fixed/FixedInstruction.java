package compiler.fixed;

import java.util.Map;

import compiler.Segment;
import compiler.dynamic.Instruction;
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
	public void evaluatePlaceholders(Texts romTexts, Map<String, Segment> labelToSegment) 
	{
		// Nothing to do since these don't have placeholders
		return;
	}
	
	@Override
	public boolean linkLocalLinks(Map<String, Segment> localSegments)
	{
		// Never assigned locally since these don't have placeholders
		return false;
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset)
	{
		return getSize();
	}
	
	@Override
	public int getMaxSize()
	{
		return getSize();
	}

	@Override
	public int writeBytes(byte[] bytes, int blockStartIdx, int writeOffset) 
	{
		return writeBytes(bytes, blockStartIdx + writeOffset);
	}
}
