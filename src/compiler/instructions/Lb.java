package compiler.instructions;

import compiler.CompilerConstants.RegisterPair;

public class Lb extends Instruction
{
	public static final int SIZE = 3;
	RegisterPair pair;
	byte value1;
	byte value2;

	public Lb(RegisterPair pair, byte value1, byte value2)
	{
		super(SIZE); // size
		this.pair = pair;
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public static Lb create(Object[] args)
	{
		if (args.length == 3 && 
				(args[0] instanceof RegisterPair) && 
				(args[1] instanceof Byte) && 
				(args[2] instanceof Byte))
		{
			return new Lb((RegisterPair) args[0], (Byte) args[1], (Byte) args[2]);
		}
		throw new IllegalArgumentException("Lb only accepts: RegisterPair, byte, byte");
	}
	
	@Override
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x01 | (pair.getValue() << 4));
		bytes[indexToAddAt++] = value2;
		bytes[indexToAddAt++] = value1;
		return indexToAddAt;
	}
}
