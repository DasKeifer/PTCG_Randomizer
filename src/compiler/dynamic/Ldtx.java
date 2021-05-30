package compiler.dynamic;


import java.util.Map;

import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.SegmentReference;
import data.romtexts.OneBlockText;
import compiler.CompilerConstants.RegisterPair;
import rom.Texts;

public class Ldtx extends Instruction
{
	RegisterPair pair;
	OneBlockText text;
	
	// TODO: move to fixed
	
	public Ldtx(RegisterPair pair, OneBlockText text)
	{
		this.pair = pair;
		this.text = text;
	}
	
	public static Ldtx create(String arg)
	{	
		final String supportedArgs = "ldtx only supports (RegisterPair, String): ";	

		// Assume we have a RegisterPair first then the string
		String[] args = arg.split(",", 2);
		if (args.length != 2)
		{
			throw new IllegalArgumentException(supportedArgs + "given: " + args.toString());
		}
		
		try
		{
			return new Ldtx(CompilerUtils.parseRegisterPairArg(args[0]), CompilerUtils.parseOneBlockTextArg(args[1]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException(supportedArgs + iae.getMessage());
		}
	}
	
	@Override
	public void extractTexts(Texts texts)
	{
		text.finalizeAndAddTexts(texts);
	}

	@Override
	public void linkData(
			Texts idToText, 
			Map<String, SegmentReference> labelToLocalSegment,
			Map<String, SegmentReference> labelToSegment) 
	{
		// Nothing to do here - move to fixed?
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset) 
	{
		return 3;
	}

	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt) 
	{		
		// Write the instruction value then the text id
		bytes[addressToWriteAt] = (byte) (0x01 | (pair.getValue() << 4));
		text.writeTextId(bytes, addressToWriteAt + 1);
		return 3;
	}
}
