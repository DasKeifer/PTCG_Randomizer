package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Ld;

public class LdHLAIncDec extends Ld
{
	public static final int SIZE = 1;
	Register reg;
	boolean increment;

	public LdHLAIncDec(Register reg, boolean incrementNotDec)
	{
		super(SIZE);
		this.reg = reg;
		this.increment = incrementNotDec;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		byte val = 0x2;
		if (increment)
		{
			bytes[indexToWriteAt] = (byte) (val | 2 << 4);
		}
		else
		{
			bytes[indexToWriteAt] = (byte) (val | 3 << 4);
		}
	}
}
