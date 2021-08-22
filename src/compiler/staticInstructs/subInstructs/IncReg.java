package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Inc;

public class IncReg extends Inc
{
	Register reg;

	public IncReg(Register reg)
	{
		super();
		this.reg = reg;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt] = (byte) (0x04 | reg.getValue() << 3);
	}
}
