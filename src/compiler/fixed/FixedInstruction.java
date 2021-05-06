package compiler.fixed;

import compiler.Data;

public abstract class FixedInstruction extends Data
{
	private int size;
	
	protected FixedInstruction(int size) 
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public abstract int writeBytes(byte[] bytes, int indexToWriteAt);
}
