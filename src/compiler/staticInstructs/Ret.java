package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.InstructionConditions;

public class Ret extends StaticInstruction
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
	
	public static Ret create(String[] args)
	{		
		if (args.length == 0)
		{
			return new Ret();
		}
		else if (args.length == 1)
		{
			try
			{
				return new Ret(CompilerUtils.parseInstructionConditionsArg(args[0]));
			}
			catch (IllegalArgumentException iae)
			{
				throw new IllegalArgumentException("Ret only supports ([No Args]) or (InstructionCondition): " + iae.getMessage());
			}
		}

		throw new IllegalArgumentException("Ret only supports ([No Args]) or (InstructionCondition): Given " + args.toString());
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		if (InstructionConditions.NONE == conditions)
		{
			bytes[indexToAddAt] = (byte) 0xC9;
		}
		else
		{
			bytes[indexToAddAt] = (byte) (0xC0 | (conditions.getValue() << 3));
		}
	}
}
