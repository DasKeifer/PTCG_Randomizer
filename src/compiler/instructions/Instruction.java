package compiler.instructions;

public abstract class Instruction
{
	private int size;
	
	protected Instruction(int size) 
	{
		this.size = size;
	}
	
	public static Instruction create(String instruction)
	{
		// Split
		String[] keyData = instruction.split(" ", 1);
		
		// TODO determine types and put in object list. Then sub objects call the instance of to see if its compatible
		Object[] data = new Object[0];
		if (keyData.length >= 1)
		{
			data = keyData[1].split(",");
		}
		
		switch (keyData[0])
		{
			// Loading
			case "lb":
				return Lb.create(data);
			case "ld":
				return Ld.create(data);
		
			// Logic
			case "cp":
				return Cp.create(data);
			case "inc":
				return Inc.create(data);
				
			// Flow control? Maybe handled in separate class?
				
			// Writing raw
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyData[0]);
		}
	}
	
	public int getSize()
	{
		return size;
	}
	
	public abstract int writeBytes(byte[] bytes, int indexToWriteAt);
}
