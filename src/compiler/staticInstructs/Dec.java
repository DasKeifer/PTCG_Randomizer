package compiler.staticInstructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.Register;

public class Dec extends StaticInstruction
{
	public static final int SIZE = 1;
	Register reg;

	public Dec(Register reg)
	{
		super(SIZE); // Size
		this.reg = reg;
	}

	public static Dec create(String[] args)
	{		
		final String SUPPORT_STRING = "Dec only supports (Register): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Dec(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch (IllegalArgumentException iae) {}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0x05 | reg.getValue() << 3);
	}
}
