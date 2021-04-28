package compiler.instructions;

public class LdhAComma extends Instruction
{
	byte value;
	
	public LdhAComma(byte ffAddressSecondByte) 
	{
		super(2); // Size
		value = ffAddressSecondByte;
	}
	
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) 0xF0;
		bytes[indexToAddAt++] = value;
		return indexToAddAt;
	}
}
