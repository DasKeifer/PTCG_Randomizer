package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;

public class Ldh extends Instruction
{
	byte value;
	boolean isAFirst;
	
	public Ldh(byte ffAddressSecondByte, boolean isACommaVal) 
	{
		super(2); // Size
		value = ffAddressSecondByte;
		isAFirst = isACommaVal;
	}
	
	public static Ldh create(String[] args)
	{		
		if (args.length != 2)
		{
			throw new IllegalArgumentException("Ldh only supports (A, $FF(00 - FF)) or ($FF(00 - FF), A): Given " + args.toString());
		}

		// A, val
		try
		{
			if (CompilerUtils.parseRegisterArg(args[0]) == Register.A &&
					CompilerUtils.parseByteArg(args[1]) == 0xFF) // Only reads the first byte of the short
			{
				return new Ldh(CompilerUtils.parseSecondByteOfShort(args[1]), true); // true = A, val
			}
		}
		catch (IllegalArgumentException iae) {}

		// val, A
		try
		{
			if (CompilerUtils.parseByteArg(args[0]) == 0xFF && // Only reads the first byte of the short
					CompilerUtils.parseRegisterArg(args[1]) == Register.A)
			{
				return new Ldh(CompilerUtils.parseSecondByteOfShort(args[0]), false); // true = val, A
			}
		}
		catch (IllegalArgumentException iae) {}
		
		throw new IllegalArgumentException("Ldh only supports (A, $FF(00 - FF)) or ($FF(00 - FF), A): Given " + args.toString());
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
