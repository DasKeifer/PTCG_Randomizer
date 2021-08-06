package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.dynamicInstructs.Jump;
import compiler.dynamicInstructs.JumpCallCommon;
import compiler.CompilerConstants.InstructionConditions;
import compiler.CompilerConstants.Register;

public class Nop extends StaticInstruction
{
	public static byte NOP_VALUE = 0x00;
	
	public Nop()
	{
		this(1);
	}
	
	public Nop(int nopSize)
	{
		super(nopSize);
	}

	public static Nop create(String[] args)
	{		
		final String SUPPORT_STRING = "Nop only supports () or (byte effectiveNumNops): Given ";
		if (args.length > 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + args.toString());
		}
		
		if (args.length == 0)
		{
			return new Nop();
		}
		else
		{
			try
			{
				return new Nop(CompilerUtils.parseByteArg(args[0]));
			}
			catch (IllegalArgumentException iae)
			{
				throw new IllegalArgumentException(SUPPORT_STRING + iae.getMessage());
			}
		}
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		int size = getSize();	
		// Write the Nops regardless of size
		for (int offset = 0; offset < size; offset++)
		{
			bytes[indexToAddAt + offset] = NOP_VALUE;
		}
		
		// takes 3 cycles to jump so 4 or greater its more efficient to jump
		if (size > 3)
		{
			JumpCallCommon.writeJr(bytes, indexToAddAt, InstructionConditions.NONE, (byte) (size - 2));
		}
	}
}
