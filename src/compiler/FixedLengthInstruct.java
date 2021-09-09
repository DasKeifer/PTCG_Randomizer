package compiler;


import datamanager.AllocatedIndexes;

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
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset, AllocatedIndexes allocatedIndexes)
	{
		return getSize();
	}
	
	@Override
	public int writeBytes(byte[] bytes, int indexToAddAt, AllocatedIndexes allocatedIndexes)
	{
		writeFixedSizeBytes(bytes, indexToAddAt, allocatedIndexes);
		return size;
	}

	public abstract void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, AllocatedIndexes allocatedIndexes);
}
