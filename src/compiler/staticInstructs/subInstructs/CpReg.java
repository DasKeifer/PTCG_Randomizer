package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Cp;

public class CpReg extends Cp
{
	public static final int SIZE = 1;
	Register reg;

	public CpReg(Register reg)
	{
		super(SIZE);
		this.reg = reg;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt] = (byte) (0xB8 | reg.getValue());
	}
}
