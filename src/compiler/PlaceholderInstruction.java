package compiler;


import java.util.Map;

import rom.Texts;

public class PlaceholderInstruction extends Instruction
{
	String line;
	Instruction inst;
	String rootBlockName;
	
	private PlaceholderInstruction(String line, String rootBlockName)
	{
		this.line = line;
		this.rootBlockName = rootBlockName;
	}
	
	public static PlaceholderInstruction create(String line, String rootBlockName)
	{
		if (CompilerUtils.isPlaceholderLine(line))
		{
			return new PlaceholderInstruction(line, rootBlockName);
		}
		throw new IllegalArgumentException("Line does not contain placeholder text!");
	}

	public void evaluatePlaceholdersAndCreateInstruction(Map<String, String> placeholderToArgs)
	{
		String lineReplaced = CompilerUtils.replacePlaceholders(line, placeholderToArgs);
		inst = CompilerUtils.parseInstruction(lineReplaced, rootBlockName);
	}

	@Override
	public void linkData(
			Texts romTexts, 
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	)
	{
		inst.linkData(romTexts, labelToLocalSegment, labelToSegment);
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset) 
	{
		if (inst == null)
		{
			return 3; // Just a typical instruction size. Shouldn't be used really
		}
		return inst.getWorstCaseSizeOnBank(bank, instructionOffset);
	}

	@Override
	public int writeBytes(byte[] bytes, int blockStartIdx, int writeOffset) 
	{
		if (inst == null)
		{
			throw new IllegalArgumentException("Cannot write placeholder instructions! Must replace all values in it");
		}
		return inst.writeBytes(bytes, blockStartIdx, writeOffset);
	}
}
