package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.RegisterPair;

public class Pop extends FixedInstruction
{
	public static final int SIZE = 1;
	RegisterPair pair;

	public Pop(RegisterPair pair)
	{
		super(SIZE); // Size
		this.pair = pair;
	}

	public static Pop create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Pop only supports (RegisterPair): Given " + args.toString());
		}
		
		try
		{
			return new Pop(CompilerUtils.parseRegisterPairArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Pop only supports (RegisterPair): " + iae.getMessage());
		}
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0xC1 | pair.getValue() << 4);
		return indexToAddAt;
	}
}
