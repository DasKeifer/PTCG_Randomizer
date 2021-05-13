package compiler.dynamic;

import java.util.Map;

import compiler.Data;
import compiler.Segment;
import rom.Texts;

public abstract class Instruction extends Data
{		
	public abstract void evaluatePlaceholders(Texts romTexts, Map<String, Segment> labelToSegment);
	public abstract boolean linkLocalLinks(Map<String, Segment> localSegments);
	public abstract int getWorstCaseSizeOnBank(byte bank, int instructionOffset);
	public abstract int getMaxSize();
	
	// Return size or something else?
	public abstract int writeBytes(byte[] bytes, int blockStartIdx, int writeOffset);
}
