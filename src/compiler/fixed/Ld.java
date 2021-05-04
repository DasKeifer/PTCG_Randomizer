package compiler.fixed;

import compiler.CompilerUtils;

public abstract class Ld extends Instruction
{
	protected Ld(int size)
	{
		super(size);
	}

	public static Ld create(String[] args)
	{
		if (args.length != 2)
		{
			throw new IllegalArgumentException("Ld only supports (Register, Byte), (Register, Register), or (RegisterPair, Short): Given " + args.toString());
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

		throw new IllegalArgumentException("Ld only supports (Register, Byte), (Register, Register), or (RegisterPair, Short): Given " + args.toString());
	}
}
