package compiler;


import datamanager.AllocatedIndexes;
import rom.Texts;

public abstract class StaticInstruction extends FixedLengthInstruct
{
	protected StaticInstruction(int size) 
	{
		super(size);
	}
	
	@Override
	public void extractTexts(Texts texts)
	{
		// Nothing to do here - these are fully static
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, AllocatedIndexes allocatedIndexes)
	{
		writeStaticBytes(bytes, indexToAddAt);
	}

	public abstract void writeStaticBytes(byte[] bytes, int indexToAddAt);
}
