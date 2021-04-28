package compiler.instructions;

import compiler.CompilerConstants.Register;

public class Or extends Instruction
{
	Register reg;
	
	public Or(Register reg)
	{
		super(1); // Size
		this.reg = reg;
	}

	@Override
	public int writeBytes(byte[] bytes, int indexToWriteAt) 
	{
		bytes[indexToWriteAt++] = (byte) (0xB0 | reg.getValue());
		return indexToWriteAt;
	}
}
