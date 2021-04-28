package compiler.instructions;

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
	
	public static Inc create(Object[] args)
	{		
		if (args.length == 1 && (args[0] instanceof Register))
		{
			return new Inc((Register) args[0]);
		}
		throw new IllegalArgumentException("inc only accepts one arguement of type Register");
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x04 | reg.getValue() << 3);
		return indexToAddAt;
	}
}
