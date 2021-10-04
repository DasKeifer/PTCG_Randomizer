package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Ld;

public class LdRegByte extends Ld 
{
	Register reg;
	byte value;
	
	public LdRegByte(Register reg, byte value)
	{
		super(2); // size
		this.reg = reg;
		this.value = value;
	}
	
	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt++] = (byte) (0x06 | (reg.getValue() << 3));
		bytes[indexToWriteAt] = value;
	}
}
