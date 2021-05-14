package compiler.dynamic;

import java.util.Map;

import compiler.Instruction;
import compiler.Segment;
import data.RomText;
import rom.Texts;

// TODO: Have this separate? Depends on how we do things

public class Ldtx extends Instruction
{
	// TODO: two options - The raw text or a value from a register/address?
	
	RomText text;
	
	public Ldtx(RomText text)
	{
		this.text = text;
	}

	@Override
	public void evaluatePlaceholders(Texts romTexts, Map<String, Segment> labelToSegment)
	{
		// TODO Auto-generated method stub
		// TODO: call finalizeandadd here. This should work since we are before
		// the text is actually written
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank) 
	{
		return 2; // TODO: is this right?
	}

	@Override
	public int writeBytes(byte[] bytes, int indexToWriteAt) 
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
