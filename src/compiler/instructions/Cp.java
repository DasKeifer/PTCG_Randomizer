package compiler.instructions;

public class Cp extends Instruction
{
	public static final int SIZE = 2;
	byte value;

	public Cp(byte value)
	{
		super(SIZE);
		this.value = value;
	}
	
	public static Cp create(Object[] args)
	{		
		if (args.length == 1 && (args[0] instanceof Byte))
		{
			return new Cp((Byte) args[0]);
		}
		
		throw new IllegalArgumentException("cp only accepts one arguement of type Byte");
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) 0xFE;
		bytes[indexToAddAt++] = value;
		return indexToAddAt;
	}
}
