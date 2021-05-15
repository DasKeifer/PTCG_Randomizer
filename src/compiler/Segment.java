package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.dynamic.PlaceholderInstruction;
import rom.Texts;

public class Segment 
{
	List<Instruction> data;
	List<PlaceholderInstruction> placeholderInstructs;
	private int assignedAddress;
	
	public Segment()
	{
		data = new LinkedList<>();
		assignedAddress = CompilerUtils.UNASSIGNED_ADDRESS;
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

	public int getAssignedAddress() 
	{
		return assignedAddress;
	}
	
	public void evaluatePlaceholdersAndLinkData(
			Texts romTexts, 
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment, 
			Map<String, String> placeholderToArgs
	)
	{		
		// First replace any placeholder lines
		for (PlaceholderInstruction instruct : placeholderInstructs)
		{
			instruct.evaluatePlaceholdersAndCreateInstruction(placeholderToArgs);
		}
		
		// Then go through and link the data
		for (Instruction item : data)
		{
			item.linkData(romTexts, labelToLocalSegment, labelToSegment);
		}
	}
	
	public int writeBytes(byte[] bytes)
	{
		int writeAddress = assignedAddress;
		for (Instruction item : data)
		{
			writeAddress += item.writeBytes(bytes, writeAddress);
		}
		return writeAddress - assignedAddress;
	}
}
