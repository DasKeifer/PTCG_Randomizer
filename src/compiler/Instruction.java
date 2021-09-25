package compiler;


import datamanager.AllocatedIndexes;
import datamanager.BankAddress;
import rom.Texts;

public abstract class Instruction
{		
	public abstract void extractTexts(Texts texts);
	
	public abstract int getWorstCaseSize(BankAddress instructionAddress, AllocatedIndexes allocatedIndexes, AllocatedIndexes tempIndexes);

	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt, AllocatedIndexes allocatedIndexes);
}
