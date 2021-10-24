package compiler;


import rom.Texts;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;

public interface Instruction
{		
	public abstract void extractText(Texts texts);
	
	public abstract int getWorstCaseSize(BankAddress instructionAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns);

	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses);
}
