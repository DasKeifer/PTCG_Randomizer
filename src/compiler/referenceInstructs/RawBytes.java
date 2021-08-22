package compiler.referenceInstructs;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compiler.Instruction;
import compiler.Segment;
import rom.Texts;
import util.ByteUtils;

public class RawBytes extends Instruction
{
	// TODO: Move to static
	int size;
	List<byte[]> allBytes;
	
	public RawBytes(byte... bytes) 
	{
		allBytes = new LinkedList<>();
		allBytes.add(bytes);
		
		size = 0;
		size = allBytes.get(0).length;
	}
	
	public RawBytes(byte[]... bytes) 
	{
		allBytes = new LinkedList<>();
		Collections.addAll(allBytes, bytes);

		size = 0;
		for (byte[] set : allBytes)
		{
			size += set.length;
		}
	}

	@Override
	public void extractTexts(Texts texts)
	{
		// No texts in this instruct
	}
	
	@Override
	public void linkData(
			Texts romTexts, 
			Map<String, Segment> labelToLocalSegment,
			Map<String, Segment> labelToSegment) 
	{
		// All hard coded - nothing to do here
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset)
	{
		return size;
	}

	@Override
	public int writeBytes(byte[] outBytes, int addressToWriteAt) 
	{
		for (byte[] set : allBytes)
		{
			ByteUtils.copyBytes(outBytes, addressToWriteAt, set);
			addressToWriteAt += set.length;
		}
		return size;
	}
}
