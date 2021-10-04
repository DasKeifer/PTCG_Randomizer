package compiler;


import rom.Texts;
import romAddressing.AssignedAddresses;

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
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, AssignedAddresses assignedAddresses)
	{
		writeStaticBytes(bytes, indexToAddAt);
	}

	public abstract void writeStaticBytes(byte[] bytes, int indexToAddAt);
}
