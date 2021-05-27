package compiler.fixed;

import compiler.CompilerUtils;
import compiler.CompilerConstants.RegisterPair;

public class Lb extends FixedInstruction
{
	public static final int SIZE = 3;
	private RegisterPair pair;
	private byte value1;
	private byte value2;
	
	public Lb(RegisterPair pair, byte value1, byte value2)
	{
		super(SIZE);
		this.pair = pair;
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public static Lb create(String[] args)
	{
		if (args.length != 3)
		{
			throw new IllegalArgumentException("Lb only supports (RegisterPair, byte, byte): Given " + args.toString());
		}
		
		try
		{
			return new Lb(CompilerUtils.parseRegisterPairArg(args[0]),
					CompilerUtils.parseByteArg(args[1]),
					CompilerUtils.parseByteArg(args[2]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException("Lb only supports RegisterPair, byte, byte: " + iae.getMessage());
		}
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x01 | (pair.getValue() << 4));
		bytes[indexToAddAt++] = value2;
		bytes[indexToAddAt] = value1;
	}
}
