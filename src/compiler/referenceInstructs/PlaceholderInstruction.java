package compiler.referenceInstructs;


import java.util.Map;

import compiler.CompilerUtils;
import compiler.Instruction;
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
		if (CompilerUtils.containsPlaceholder(line) || CompilerUtils.containsPlaceholder(rootBlockName))
		{
			return new PlaceholderInstruction(line, rootBlockName);
		}
		throw new IllegalArgumentException("Line does not explicitly or implicitly contain placeholder text!");
	}

	public void fillPlaceholdersAndCreateInstruction(Map<String, String> placeholderToArgs)
	{
		String lineReplaced = CompilerUtils.replacePlaceholders(line, placeholderToArgs);
		String rootBlockNameReplaced = CompilerUtils.replacePlaceholders(rootBlockName, placeholderToArgs);
		inst = CompilerUtils.parseInstruction(lineReplaced, rootBlockNameReplaced);
	}
	
	@Override
	public void extractTexts(Texts texts)
	{
		inst.extractTexts(texts);
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset, Map<String, Integer> allocatedIndexes) 
	{
		if (inst == null)
		{
			return 3; // Just a typical instruction size. Shouldn't be used really
		}
		return inst.getWorstCaseSizeOnBank(bank, instructionOffset, allocatedIndexes);
	}

	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt, Map<String, Integer> allocatedIndexes) 
	{
		if (inst == null)
		{
			throw new IllegalArgumentException("Cannot write placeholder instructions! Must replace all values in it");
		}
		return inst.writeBytes(bytes, addressToWriteAt, allocatedIndexes);
	}
}
