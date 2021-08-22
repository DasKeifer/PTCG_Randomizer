package compiler.staticInstructs.subInstructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Ld;
import util.ByteUtils;

public class LdAMemAddr extends Ld
{
	public static final int SIZE = 3;
	short addr;
	boolean isAFirst;
	
	public LdAMemAddr(short addr, boolean isAFirst)
	{
		super(SIZE); // size
		this.addr = addr;
		this.isAFirst = isAFirst;
	}

	public static LdAMemAddr create(String[] args)
	{
		final String SUPPORT_STRING = "LdMemAddrA only supports (A, [memoryAddress]) and ([memoryAddress], A): Given";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + args.toString());
		}
		
		// A, addr
		try
		{
			if (CompilerUtils.parseRegisterArg(args[0]) == Register.A) 
			{
				// Pass only second byte
				return new LdAMemAddr(CompilerUtils.parseMemoryAddressArg(args[1]), true); // true = A, addr
			}
		}
		catch (IllegalArgumentException iae) {}

		// addr, A
		try
		{
			if (CompilerUtils.parseRegisterArg(args[1]) == Register.A) 
			{
				// Pass only second byte
				return new LdAMemAddr(CompilerUtils.parseMemoryAddressArg(args[0]), false); // false = addr, A
			}
		}
		catch (IllegalArgumentException iae) {}
		
		throw new IllegalArgumentException(SUPPORT_STRING + " Given " + Arrays.toString(args));
	}
	
	// TODO: Make naming consistent - sometime write at sometime add at
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToWriteAt) 
	{
		// A, val
		if (isAFirst)
		{
			bytes[indexToWriteAt++] = (byte) 0xFA;
		}
		// val, A
		else
		{
			bytes[indexToWriteAt++] = (byte) 0xEA;
		}
		ByteUtils.writeAsShort(addr, bytes, indexToWriteAt);
	}
}
