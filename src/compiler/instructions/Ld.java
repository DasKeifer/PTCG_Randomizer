package compiler.instructions;

import compiler.CompilerConstants.Register;
import compiler.CompilerConstants.RegisterPair;

public abstract class Ld extends Instruction
{
	protected Ld(int size)
	{
		super(size);
	}

	public static Ld create(Object[] args)
	{
		if (args.length == 2 && 
				(args[0] instanceof Register) && 
				(args[1] instanceof Byte))
		{
			return new LdRegByte((Register) args[0], (Byte) args[1]);
		}
		else if (args.length == 2 && 
				(args[0] instanceof Register) && 
				(args[1] instanceof Register))
		{
			return new LdRegReg((Register) args[0], (Register) args[1]);
		}
		else if (args.length == 2 && 
				(args[0] instanceof RegisterPair) && 
				(args[1] instanceof Short))
		{
			return new LdPairShort((RegisterPair) args[0], (Short) args[1]);
		}
			

		throw new IllegalArgumentException("cp only accepts one arguement of type Byte");
	}
}
