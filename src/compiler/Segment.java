package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.dynamic.Instruction;

public class Segment 
{
	String id; // TODO: needed?
	List<Instruction> data;
	private int address;
	private short blockOffset;
	
	// TODO: instead of creating versions of the instructions with placeholders, create placeholder
	// instructions. Then when I go to finalize these, I can do a mass replace and write at that point
	
	public Segment(String id, short blockOffset)
	{
		this.id = id;
		data = new LinkedList<>();
		address = CompilerUtils.UNASSIGNED_ADDRESS;
		this.blockOffset = blockOffset;
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

	public void linkIntrablockLinks(Map<String, Segment> localSegments) 
	{
		for (Instruction item : data)
		{
			item.linkLocalLinks(localSegments);
		}
	}
	
	// TODO write
}
