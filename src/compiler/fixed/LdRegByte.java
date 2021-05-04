package compiler.fixed;

import compiler.CompilerConstants.Register;

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
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x06 | (reg.getValue() << 3));
		bytes[indexToAddAt++] = value;
		return indexToAddAt;
	}
}
