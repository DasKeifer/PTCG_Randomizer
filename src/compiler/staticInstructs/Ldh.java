package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import util.ByteUtils;

import java.util.Arrays;

import compiler.CompilerConstants.Register;

public class Ldh extends StaticInstruction
{
	public static final int SIZE = 2;
	byte value;
	boolean isAFirst;
	
	// values for constants defined in hram.asm if I want to add them as an enum
	
	public Ldh(byte ffAddressSecondByte, boolean isACommaVal) 
	{
		super(SIZE);
		value = ffAddressSecondByte;
		isAFirst = isACommaVal;
	}
	
	// TODO genericise so it can take ld and do more than just FF refs? No keep as separate, more specific function
	
	public static Ldh create(String[] args)
	{		
		String allowedFormatText = "Ldh only supports (A, [$FF(00 - FF)]) or ([$FF(00 - FF)], A): ";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(allowedFormatText + " Given " + Arrays.toString(args));
		}

		// A, val
		short memAddress;
		try
		{
			memAddress = CompilerUtils.parseMemoryAddressArg(args[1]);
			if (CompilerUtils.parseRegisterArg(args[0]) == Register.A &&
					// This will check only the first byte. Might be tempting to do a greater than 
					// compare but shouldn't because Java only used signed values so the compare would
					// need to be different than expected
					(byte) (memAddress >> 8) == (byte)0xFF) 
			{
				// Pass only second byte
				return new Ldh((byte) (memAddress & 0xFF), true); // true = A, val
			}
		}
		catch (IllegalArgumentException iae) {}

		// val, A
		try
		{
			memAddress = CompilerUtils.parseMemoryAddressArg(args[0]);
			if (// This will check only the first byte. Might be tempting to do a greater than 
					// compare but shouldn't because Java only used signed values so the compare would
					// need to be different than expected
					(byte) (memAddress >> 8) == (byte)0xFF &&
					CompilerUtils.parseRegisterArg(args[1]) == Register.A)
			{
				// Pass only second byte
				return new Ldh((byte) (memAddress & 0xFF), false); // false = val, A
			}
		}
		catch (IllegalArgumentException iae) {}
		
		throw new IllegalArgumentException(allowedFormatText + " Given " + Arrays.toString(args));
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
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
		bytes[indexToAddAt] = value;
	}
}
