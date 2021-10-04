package compiler.staticInstructs;

import java.util.Arrays;

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
		final String SUPPORT_STRING = "Or only supports (Register): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Or(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch (IllegalArgumentException iae) {}
		
		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}

	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		bytes[indexToWriteAt] = (byte) (0xB0 | reg.getValue());
	}
}
