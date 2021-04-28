package compiler.instructions;

import compiler.CompilerConstants.InstructionConditions;

public class Ret extends Instruction
{
	public static final int SIZE = 1;
	InstructionConditions conditions;
	
	public Ret(InstructionConditions retConditions)
	{
		super(1); // Size
		conditions = retConditions;
	}
	
	public Ret()
	{
		this(InstructionConditions.NONE);
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		if (InstructionConditions.NONE == conditions)
		{
			bytes[indexToAddAt++] = (byte) 0xC9;
		}
		else
		{
			bytes[indexToAddAt++] = (byte) (0xC0 | (conditions.getValue() << 3));
		}
		return indexToAddAt;
	}
}
