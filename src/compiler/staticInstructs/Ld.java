package compiler.staticInstructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.staticInstructs.subInstructs.LdAHLIncDec;
import compiler.staticInstructs.subInstructs.LdAMemAddr;
import compiler.staticInstructs.subInstructs.LdPairShort;
import compiler.staticInstructs.subInstructs.LdRegByte;
import compiler.staticInstructs.subInstructs.LdRegReg;

public abstract class Ld extends StaticInstruction
{
	protected Ld(int size)
	{
		super(size);
	}

	public static Ld create(String[] args)
	{
		// TODO: remove for only lower and catch IOBE?
		final String SUPPORT_STRING = "Ld only supports (Register, Byte), (Register, Register), (RegisterPair, Short), "
				+ "(a, [hli or hld]), or ([hli or hld], a): Given ";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new LdRegByte(
					CompilerUtils.parseRegisterArg(args[0]),
					CompilerUtils.parseByteArg(args[1]));
		}
		catch(IllegalArgumentException iae) {}

		try
		{
			return new LdRegReg(
					CompilerUtils.parseRegisterArg(args[0]),
					CompilerUtils.parseRegisterArg(args[1]));
		}
		catch(IllegalArgumentException iae) {}
		
		try
		{
			return new LdPairShort(
					CompilerUtils.parseRegisterPairArg(args[0]),
					CompilerUtils.parseShortArg(args[1]));
		}
		catch(IllegalArgumentException iae) {}		

		try
		{
			return LdAHLIncDec.create(args);
		}
		catch(IllegalArgumentException iae) {}	

		try
		{
			return LdAMemAddr.create(args);
		}
		catch(IllegalArgumentException iae) {}	

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
}
