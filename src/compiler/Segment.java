package compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.reference_instructs.PlaceholderInstruction;
import constants.RomConstants;
import rom.Texts;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;

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
		BankAddress instructAddr = segmentAddress;
		for (Instruction item : data)
		{
			int instructSize = item.getWorstCaseSize(instructAddr, assignedAddresses, tempIndexes);
			// If it doesn't fit, we have an issue
			if (!instructAddr.fitsInBankAddress(instructSize))
			{
				return -1;
			}
			instructAddr = instructAddr.newOffsetted(instructSize);
		}
		
		// The instruction can be null if the last instruction perfectly aligned with the end
		// of the bank
		if (instructAddr == null)
		{
			return RomConstants.BANK_SIZE - segmentAddress.getAddressInBank();
		}
		return instructAddr.getAddressInBank() - segmentAddress.getAddressInBank();
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
	public void extractTextsFromInstructions(Texts texts) 
	{
		for (Instruction item : data)
		{
			item.extractText(texts);
		}
	}
	
	public int writeBytes(byte[] bytes, int assignedAddress, AssignedAddresses assignedAddresses)
	{
		int writeAddress = assignedAddress;
		for (Instruction item : data)
		{
			writeAddress += item.writeBytes(bytes, writeAddress, assignedAddresses);
		}
		
		return writeAddress - assignedAddress;
	}
}
