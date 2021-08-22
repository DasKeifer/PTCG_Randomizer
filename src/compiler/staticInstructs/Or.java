package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.Register;

public class Or extends StaticInstruction
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
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		bytes[indexToWriteAt] = (byte) (0xB0 | reg.getValue());
	}
}
