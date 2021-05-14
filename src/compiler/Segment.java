package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rom.Texts;

public class Segment 
{
	List<Instruction> data;
	List<PlaceholderInstruction> placeholderInstructs;
	private int address;
	private short blockOffset;
	
	public Segment()
	{
		data = new LinkedList<>();
		address = CompilerUtils.UNASSIGNED_ADDRESS;
		blockOffset = CompilerUtils.UNASSIGNED_ADDRESS;
	}
	
	boolean setOffset(short blockOffset)
	{
		if (this.blockOffset == blockOffset)
		{
			return false;
		}
		
		this.blockOffset = blockOffset;
		return true;
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
		int offset = blockOffset;
		for (Instruction item : data)
		{
			offset += item.getWorstCaseSizeOnBank(bank, offset);
		}
		return offset - blockOffset;
	}

	public int getAddress() 
	{
		return address;
	}

	public short getBlockOffset() 
	{
		return blockOffset;
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
	
	// TODO write
}
