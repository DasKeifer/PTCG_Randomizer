package compiler;

import java.util.Map;

import rom.Texts;

public abstract class Instruction
{		
	public abstract void linkData(
			Texts romTexts,
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	);
	
	public abstract int getWorstCaseSizeOnBank(byte bank, int instructionOffset);
	
	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt);
}
