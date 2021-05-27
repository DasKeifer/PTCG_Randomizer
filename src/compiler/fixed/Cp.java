package compiler.fixed;

import compiler.CompilerUtils;

public class Cp extends FixedInstruction
{
	public static final int SIZE = 2;
	byte value;

	public Cp(byte value)
	{
		super(SIZE);
		this.value = value;
	}
	
	public static Cp create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Cp only supports (Byte): Given " + args.toString());
		}
		
		try
		{
			return new Cp(CompilerUtils.parseByteArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Cp only supports (Byte): " + iae.getMessage());
		}
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) 0xFE;
		bytes[indexToAddAt] = value;
	}
}
