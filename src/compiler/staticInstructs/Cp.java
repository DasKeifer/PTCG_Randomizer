package compiler.staticInstructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.staticInstructs.subInstructs.CpByte;
import compiler.staticInstructs.subInstructs.CpReg;

public abstract class Cp extends StaticInstruction
{
	protected Cp(int size)
	{
		super(size);
	}

	public static Cp create(String[] args)
	{
		final String SUPPORT_STRING = "Cp only supports (Byte) or (Register): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new CpByte(CompilerUtils.parseByteArg(args[0]));
		}
		catch (IllegalArgumentException iae) {} // continue to next constructor

		try
		{
			return new CpReg(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch(IllegalArgumentException iae) {}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
}
