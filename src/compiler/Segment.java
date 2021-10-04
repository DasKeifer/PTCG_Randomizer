package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.referenceInstructs.PlaceholderInstruction;
import rom.Texts;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;

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
	
	public int getWorstCaseSize(BankAddress segmentAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempIndexes)
	{
		BankAddress instructAddr = new BankAddress(segmentAddress);
		for (Instruction item : data)
		{
			instructAddr.addressInBank += item.getWorstCaseSize(instructAddr, assignedAddresses, tempIndexes);
		}
		return instructAddr.addressInBank - segmentAddress.addressInBank;
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
	
	public int writeBytes(byte[] bytes, int assignedAddress, AssignedAddresses assignedAddresses)
	{
		int writeAddress = assignedAddress;
		for (Instruction item : data)
		{
			writeAddress += item.writeBytes(bytes, writeAddress, assignedAddresses);
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
