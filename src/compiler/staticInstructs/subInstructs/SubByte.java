package compiler.staticInstructs.subInstructs;

import compiler.staticInstructs.Sub;

public class SubByte extends Sub
{
	public static final int SIZE = 2;
	byte val;
	
	public SubByte(byte val)
	{
		super(SIZE);
		this.val = val;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) 0xD6;
		bytes[indexToAddAt] = val;
	}
}
