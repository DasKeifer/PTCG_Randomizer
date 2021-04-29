package datamanager;


import compiler.CodeSnippit;

public class ConstrainedBlock extends FlexibleBlock
{
	private CodeSnippit minimalBlock;
	
	public ConstrainedBlock(byte priority, CodeSnippit toPlaceInBank) 
	{
		super(priority, toPlaceInBank);
	}

	@Override
	public void writeData(byte[] bytes, int index)
	{
		if (hasMinimalOption())
		{
			minimal
		}
	}
	
	public void addMinimalBlock(CodeSnippit minimalBlock)
	{
		this.minimalBlock = new CodeSnippit(minimalBlock);
	}

	@Override
	public int writeData(byte[] bytes, int index)
	{
		if (hasMinimalOption())
		{
			return minimalBlock.write(bytes, index);
		}
		return super.writeData(bytes, index);
	}

	@Override
	public int getMinimalSize() 
	{
		if (hasMinimalOption())
		{
			return minimalBlock.getSize();
		}
		
		return getFullSize();
	}

	@Override
	public boolean hasMinimalOption() 
	{
		return minimalBlock != null;
	}

}
