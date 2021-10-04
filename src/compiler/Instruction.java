package compiler;


import rom.Texts;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;

public abstract class Instruction
{		
	public abstract void extractTexts(Texts texts);
	
	public abstract int getWorstCaseSize(BankAddress instructionAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempIndexes);

	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses);
}
