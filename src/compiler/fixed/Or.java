package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;

public class Or extends Instruction
{
	Register reg;
	
	public Or(Register reg)
	{
		super(1); // Size
		this.reg = reg;
	}

	public static Or create(String[] args)
	{		
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Or only supports (Register): Given " + args.toString());
		}
		
		try
		{
			return new Or(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Or only supports (Register): " + iae.getMessage());
		}
	}

	@Override
	public int writeBytes(byte[] bytes, int indexToWriteAt) 
	{
		bytes[indexToWriteAt++] = (byte) (0xB0 | reg.getValue());
		return indexToWriteAt;
	}
}
