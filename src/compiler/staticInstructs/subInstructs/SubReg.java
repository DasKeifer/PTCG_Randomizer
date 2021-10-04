package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Sub;

public class SubReg extends Sub
{
	public static final int SIZE = 1;
	Register reg;
	
	public SubReg(Register reg)
	{
		super(SIZE);
		this.reg = reg;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0x90 | reg.getValue());
	}
}
