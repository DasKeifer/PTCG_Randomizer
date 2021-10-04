package compiler.staticInstructs;

import java.util.Arrays;

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
		final String SUPPORT_STRING = "Push only supports (PushPopRegisterPair): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Push(CompilerUtils.parsePushPopRegisterPairArg(args[0]));
		}
		catch (IllegalArgumentException iae) {}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0xC5 | pair.getValue() << 4);
	}
}
