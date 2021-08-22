package compiler;

import java.util.Map;

import rom.Texts;

public abstract class Instruction
{		
	public abstract void extractTexts(Texts texts);
	
	public abstract int getWorstCaseSizeOnBank(byte bank, int instructionOffset, Map<String, Integer> allocatedIndexes);

	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt, Map<String, Integer> allocatedIndexes);
}
