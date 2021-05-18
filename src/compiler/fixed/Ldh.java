package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;

public class Ldh extends FixedInstruction
{
	byte value;
	boolean isAFirst;
	
	// values for constants defined in hram.asm if I want to add them as an enum
	
	public Ldh(byte ffAddressSecondByte, boolean isACommaVal) 
	{
		super(2); // Size
		value = ffAddressSecondByte;
		isAFirst = isACommaVal;
	}
	
	public static Ldh create(String[] args)
	{		
		String allowedFormatText = "Ldh only supports (A, [$FF(00 - FF)]) or ([$FF(00 - FF)], A): ";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(allowedFormatText + " Given " + args.toString());
		}

		// A, val
		String bracketsRemoved;
		try
		{
			bracketsRemoved = CompilerUtils.tryParseBracketedArg(args[1]);
			if (bracketsRemoved == null ||
					(CompilerUtils.parseRegisterArg(args[0]) == Register.A &&
					CompilerUtils.parseByteArg(bracketsRemoved) == 0xFF)) // Only reads the first byte of the short
			{
				return new Ldh(CompilerUtils.parseSecondByteOfShort(bracketsRemoved), true); // true = A, val
			}
		}
		catch (IllegalArgumentException iae) {}

		// val, A
		try
		{
			bracketsRemoved = CompilerUtils.tryParseBracketedArg(args[0]);
			if (CompilerUtils.parseByteArg(bracketsRemoved) == 0xFF && // Only reads the first byte of the short
					CompilerUtils.parseRegisterArg(args[1]) == Register.A)
			{
				return new Ldh(CompilerUtils.parseSecondByteOfShort(bracketsRemoved), false); // true = val, A
			}
		}
		catch (IllegalArgumentException iae) {}
		
		throw new IllegalArgumentException(allowedFormatText + " Given " + args.toString());
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		// A, val
		if (isAFirst)
		{
			bytes[indexToAddAt++] = (byte) 0xF0;
		}
		// val, A
		else
		{
			bytes[indexToAddAt++] = (byte) 0xE0;
		}
		bytes[indexToAddAt++] = value;
		return indexToAddAt;
	}
}
