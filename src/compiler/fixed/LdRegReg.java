package compiler.fixed;

import compiler.CompilerConstants.Register;

public class LdRegReg extends Ld
{
	Register to;
	Register from;
	
	public LdRegReg(Register loadTo, Register loadFrom)
	{
		super(1); // size
		to = loadTo;
		from = loadFrom;
	}

	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToWriteAt) 
	{
		bytes[indexToWriteAt] = (byte) (0x40 | (to.getValue() << 3) | (from.getValue()));
	}
}
