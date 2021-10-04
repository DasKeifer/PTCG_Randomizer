package compiler.staticInstructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.staticInstructs.subInstructs.IncPair;
import compiler.staticInstructs.subInstructs.IncReg;

public abstract class Inc extends StaticInstruction
{
	public static final int SIZE = 1;
	protected Inc()
	{
		super(SIZE);
	}

	public static Inc create(String[] args)
	{
		final String SUPPORT_STRING = "Inc only supports (Register) or (RegisterPair): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new IncReg(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch(IllegalArgumentException iae) {}

		try
		{
			return new IncPair(CompilerUtils.parseRegisterPairArg(args[0]));
		}
		catch(IllegalArgumentException iae) {}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
}
