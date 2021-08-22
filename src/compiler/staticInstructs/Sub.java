package compiler.staticInstructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.Register;
import compiler.staticInstructs.subInstructs.SubByte;
import compiler.staticInstructs.subInstructs.SubReg;

public abstract class Sub extends StaticInstruction
{
	protected Sub(int size)
	{
		super(size);
	}

	public static Sub create(String[] args)
	{
		final String SUPPORT_STRING = "Sub only supports (A, Byte) or (A, Register): Given ";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		// Ensure first arg is register A
		try
		{
			if (CompilerUtils.parseRegisterArg(args[0]) != Register.A)
			{
				throw new IllegalArgumentException(); // Caught below
			}
		}
		catch (IllegalArgumentException iae) 
		{
			throw new IllegalArgumentException(SUPPORT_STRING + " a register that is not A for first arg: " + Arrays.toString(args));
		}
			
		try
		{
			return new SubByte(CompilerUtils.parseByteArg(args[1]));
		}
		catch(IllegalArgumentException iae) {}

		try
		{
			return new SubReg(CompilerUtils.parseRegisterArg(args[1]));
		}
		catch(IllegalArgumentException iae) {}	

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
}
