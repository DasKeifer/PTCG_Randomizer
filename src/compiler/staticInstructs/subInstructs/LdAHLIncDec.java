package compiler.staticInstructs.subInstructs;

import compiler.staticInstructs.Ld;

public class LdAHLIncDec extends Ld
{
	boolean loadToA;
	boolean increment;
	
	public LdAHLIncDec(boolean loadToA, boolean increment)
	{
		super(1); // size
		this.loadToA = loadToA;
		this.increment = increment;
	}
	
	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		byte val = 0x2;
		if (loadToA)
		{
			val = 0xA;
		}
		if (increment)
		{
			val |= 2 << 4;
		}
		else
		{
			val |= 3 << 4;
		}
		
		bytes[indexToWriteAt] = val;
	}
}
