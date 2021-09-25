package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.PushPopRegisterPair;

public class Pop extends StaticInstruction
{
	public static final int SIZE = 1;
	PushPopRegisterPair pair;

	public Pop(PushPopRegisterPair pair)
	{
		super(SIZE); // Size
		this.pair = pair;
	}

	public static Pop create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Pop only supports (PushPopRegisterPair): Given " + args.toString());
		}
		
		try
		{
			return new Pop(CompilerUtils.parsePushPopRegisterPairArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Pop only supports (PushPopRegisterPair): " + iae.getMessage());
		}
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt] = (byte) (0xC1 | pair.getValue() << 4);
	}
}
