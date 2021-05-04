package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;

public class Dec extends Instruction
{
	public static final int SIZE = 1;
	Register reg;

	public Dec(Register reg)
	{
		super(SIZE); // Size
		this.reg = reg;
	}

	public static Dec create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Dec only supports (Register): Given " + args.toString());
		}
		
		try
		{
			return new Dec(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Dec only supports (Register): " + iae.getMessage());
		}
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x05 | reg.getValue() << 3);
		return indexToAddAt;
	}
}
