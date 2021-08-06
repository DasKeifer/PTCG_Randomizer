package compiler.staticInstructs.subInstructs;

import compiler.staticInstructs.Cp;

public class CpByte extends Cp
{
	public static final int SIZE = 2;
	byte value;

	public CpByte(byte value)
	{
		super(SIZE);
		this.value = value;
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) 0xFE;
		bytes[indexToAddAt] = value;
	}
}
