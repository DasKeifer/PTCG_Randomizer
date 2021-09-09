package compiler;


import datamanager.AllocatedIndexes;
import rom.Texts;

public abstract class Instruction
{		
	public abstract void extractTexts(Texts texts);
	
	public abstract int getWorstCaseSizeOnBank(byte bank, int instructionOffset, AllocatedIndexes allocatedIndexes);

	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt, AllocatedIndexes allocatedIndexes);
}
