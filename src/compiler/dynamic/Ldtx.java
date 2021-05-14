package compiler.dynamic;


import java.util.Map;

import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.Segment;
import data.romtexts.OneBlockText;
import compiler.CompilerConstants.RegisterPair;
import rom.Texts;

public class Ldtx extends Instruction
{
	RegisterPair pair;
	OneBlockText text;
	
	public Ldtx(RegisterPair pair, OneBlockText text)
	{
		this.pair = pair;
		this.text = text;
	}
	
	public static Ldtx create(String arg)
	{	
		final String supportedArgs = "ldtx only supports (RegisterPair, String): ";	

		// Assume we have a RegisterPair first then the string
		String[] args = arg.split(",", 1);
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
	public void linkData(
			Texts idToText, 
			Map<String, Segment> labelToLocalSegment,
			Map<String, Segment> labelToSegment) 
	{
		text.finalizeAndAddTexts(idToText);
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instructionOffset) 
	{
		return 3;
	}

	@Override
	public int writeBytes(byte[] bytes, int blockStartIdx, int writeOffset) 
	{		
		// Write the instruction value then the text id
		bytes[blockStartIdx + writeOffset] = (byte) (0x01 | (pair.getValue() << 4));
		text.writeTextId(bytes, blockStartIdx + writeOffset + 1);
		return 3;
	}
}
