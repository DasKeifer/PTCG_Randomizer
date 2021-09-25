package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.PushPopRegisterPair;

public class Push extends StaticInstruction
{
	public static final int SIZE = 1;
	PushPopRegisterPair pair;

	public Push(PushPopRegisterPair pair)
	{
		super(SIZE); // Size
		this.pair = pair;
	}

	public static Push create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Push only supports (PushPopRegisterPair): Given " + args.toString());
		}
		
		try
		{
			return new Push(CompilerUtils.parsePushPopRegisterPairArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Push only supports (PushPopRegisterPair): " + iae.getMessage());
		}
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt] = (byte) (0xC5 | pair.getValue() << 4);
	}
}
