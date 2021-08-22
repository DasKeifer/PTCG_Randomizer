package compiler;


import java.util.Map;

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
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, Map<String, Integer> allocatedIndexes)
	{
		writeStaticBytes(bytes, indexToAddAt);
	}

	public abstract void writeStaticBytes(byte[] bytes, int indexToAddAt);
}
