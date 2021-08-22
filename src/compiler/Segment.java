package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.referenceInstructs.PlaceholderInstruction;
import rom.Texts;

public class Segment
{
	List<Instruction> data;
	List<PlaceholderInstruction> placeholderInstructs;
	
	public Segment()
	{
		data = new LinkedList<>();
		placeholderInstructs = new LinkedList<>();
	}
	
	public void appendInstruction(Instruction instruct)
	{
		data.add(instruct);
	}
	
	public void appendPlaceholderInstruction(PlaceholderInstruction instruct)
	{
		appendInstruction(instruct);
		placeholderInstructs.add(instruct);
	}
	
	public int getWorstCaseSizeOnBank(int allocAddress, byte bankToGetSizeOn)
	{
		int size = 0;
		for (Instruction item : data)
		{
			size += item.getWorstCaseSizeOnBank(bankToGetSizeOn, allocAddress + size);
		}
		return size;
	}
	
	public void fillPlaceholders(Map<String, String> placeholderToArgs)
	{
		for (PlaceholderInstruction instruct : placeholderInstructs)
		{
			instruct.fillPlaceholdersAndCreateInstruction(placeholderToArgs);
		}
	}

	// Done at a separate time than placeholders in case we want to incrementally replace 
	// placeholders prior to extracting texts or linking
	public void extractTexts(Texts texts) 
	{
		for (Instruction item : data)
		{
			item.extractTexts(texts);
		}
	}
	
	public void linkData(
			Texts romTexts, 
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	)
	{		
		for (Instruction item : data)
		{
			item.linkData(romTexts, labelToLocalSegment, labelToSegment);
		}
	}
	
	public int writeBytes(byte[] bytes, int assignedAddress)
	{
		int writeAddress = assignedAddress;
		for (Instruction item : data)
		{
			writeAddress += item.writeBytes(bytes, writeAddress);
		}

		if (DataBlock.debug)
		{
			for (int i = assignedAddress; i < writeAddress; i++)
			{
				System.out.println(String.format("0x%x - 0x%x", i, bytes[i]));
			}
		}
		
		return writeAddress - assignedAddress;
	}
}
