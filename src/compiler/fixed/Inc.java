package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;

public class Inc extends Instruction
{
	public static final int SIZE = 1;
	Register reg;

	public Inc(Register reg)
	{
		super(SIZE); // Size
		this.reg = reg;
	}

	public static Inc create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Inc only supports (Register): Given " + args.toString());
		}
		
		try
		{
			return new Inc(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Inc only supports (Register): " + iae.getMessage());
		}
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x04 | reg.getValue() << 3);
		return indexToAddAt;
	}
}
