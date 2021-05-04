package compiler.fixed;

public abstract class Instruction
{
	private int size;
	
	protected Instruction(int size) 
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public abstract int writeBytes(byte[] bytes, int indexToWriteAt);
}
