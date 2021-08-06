package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.RegisterPair;

public class Push extends StaticInstruction
{
	public static final int SIZE = 1;
	RegisterPair pair;

	public Push(RegisterPair pair)
	{
		super(SIZE); // Size
		this.pair = pair;
	}

	public static Push create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Push only supports (RegisterPair): Given " + args.toString());
		}
		
		try
		{
			return new Push(CompilerUtils.parseRegisterPairArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Push only supports (RegisterPair): " + iae.getMessage());
		}
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt] = (byte) (0xC5 | pair.getValue() << 4);
	}
}
