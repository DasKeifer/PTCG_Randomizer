package compiler.instructions;

public class LdhCommaA extends Instruction
{
	byte value;
	
	public LdhCommaA(byte ffAddressSecondByte) 
	{
		super(2); // Size
		value = ffAddressSecondByte;
	}

	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) 0xE0;
		bytes[indexToAddAt++] = value;
		return indexToAddAt;
	}
}
