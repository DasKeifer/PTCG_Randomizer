package compiler.reference_instructs;


import compiler.CompilerUtils;
import data.romtexts.OneBlockText;

import java.util.Arrays;

import compiler.FixedLengthInstruct;
import compiler.CompilerConstants.RegisterPair;
import rom.Texts;
import rom_addressing.AssignedAddresses;

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
	public void extractText(Texts texts)
	{
		text.finalizeAndAddTexts(texts);
	}

	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, AssignedAddresses unused) 
	{
		// Write the instruction value then the text id
		bytes[indexToAddAt] = (byte) (0x01 | (pair.getValue() << 4));
		text.writeTextId(bytes, indexToAddAt + 1);
	}
}
