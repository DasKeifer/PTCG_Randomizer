package compiler;

import java.util.Map;

public abstract class FixedLengthInstruct extends Instruction
{
	private int size;
	
	protected FixedLengthInstruct(int size) 
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return size;
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset, Map<String, Integer> allocatedIndexes)
	{
		return getSize();
	}
	
	@Override
	public int writeBytes(byte[] bytes, int indexToAddAt, Map<String, Integer> allocatedIndexes)
	{
		writeFixedSizeBytes(bytes, indexToAddAt, allocatedIndexes);
		return size;
	}

	public abstract void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, Map<String, Integer> allocatedIndexes);
}
