package compiler.staticInstructs;

import java.util.Arrays;

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
		final String SUPPORT_STRING = "Pop only supports (PushPopRegisterPair): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Pop(CompilerUtils.parsePushPopRegisterPairArg(args[0]));
		}
		catch (IllegalArgumentException iae) {}
		
		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0xC1 | pair.getValue() << 4);
	}
}
