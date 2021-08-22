package compiler.referenceInstructs;


import compiler.CompilerUtils;
import compiler.Segment;
import data.romtexts.OneBlockText;

import java.util.Arrays;
import java.util.Map;

import compiler.FixedLengthInstruct;
import compiler.CompilerConstants.RegisterPair;
import rom.Texts;

public class Ldtx extends FixedLengthInstruct
{
	public static final int SIZE = 3;
	RegisterPair pair;
	OneBlockText text;
	
	public Ldtx(RegisterPair pair, OneBlockText text)
	{
		super(SIZE);
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
			throw new IllegalArgumentException(supportedArgs + "given: " + Arrays.toString(args));
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
			Texts romTexts, 
			Map<String, Segment> labelToLocalSegment,
			Map<String, Segment> labelToSegment) 
	{
		// No linking required - only needs the text
	}

	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt) 
	{
		// Write the instruction value then the text id
		bytes[indexToAddAt] = (byte) (0x01 | (pair.getValue() << 4));
		text.writeTextId(bytes, indexToAddAt + 1);
	}
}
