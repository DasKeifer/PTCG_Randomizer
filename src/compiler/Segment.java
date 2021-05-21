package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.dynamic.PlaceholderInstruction;
import rom.Texts;

class Segment extends SegmentReference
{
	List<Instruction> data;
	List<PlaceholderInstruction> placeholderInstructs;
	
	public Segment()
	{
		data = new LinkedList<>();
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
	
	public int getWorstCaseSizeOnBank(byte bank)
	{
		int size = 0;
		for (Instruction item : data)
		{
			size += item.getWorstCaseSizeOnBank(bank, assignedAddress + size);
		}
		return size;
	}
	
	public boolean setAssignedAddress(int address) 
	{
		if (this.assignedAddress == address)
		{
			return false;
		}
		
		this.assignedAddress = address;
		return true;
	}
	
	public void evaluatePlaceholders(Map<String, String> placeholderToArgs)
	{
		for (PlaceholderInstruction instruct : placeholderInstructs)
		{
			instruct.evaluatePlaceholdersAndCreateInstruction(placeholderToArgs);
		}
	}
	
	public void linkData(
			Texts romTexts, 
			Map<String, SegmentReference> labelToLocalSegment, 
			Map<String, SegmentReference> labelToSegment
	)
	{		
		for (Instruction item : data)
		{
			item.linkData(romTexts, labelToLocalSegment, labelToSegment);
		}
	}
	
	public int writeBytes(byte[] bytes)
	{
		int writeAddress = assignedAddress;
		if (assignedAddress < 0)
		{
			throw new IllegalArgumentException("Attempted to write a segment that does not have an assigned address!");
		}
		for (Instruction item : data)
		{
			writeAddress += item.writeBytes(bytes, writeAddress);
		}
		return writeAddress - assignedAddress;
	}
}
