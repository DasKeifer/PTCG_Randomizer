package datamanager;


import compiler.CodeSnippit;

public class CustomMinimalBlock extends ConstrainedBlock
{
	private CodeSnippit compressedSnippit;
	
	public CustomMinimalBlock(byte priority, CodeSnippit toPlaceInBank, CodeSnippit compressedSnippit) 
	{
		super(priority, toPlaceInBank);
		this.compressedSnippit = new CodeSnippit(compressedSnippit);
	}

	@Override
	public int writeData(byte[] bytes, int index)
	{
		if (hasMinimalOption())
		{
			return compressedSnippit.write(bytes, index);
		}
		return super.writeData(bytes, index);
	}

	@Override
	public int getMinimalSize() 
	{
		if (hasMinimalOption())
		{
			return compressedSnippit.size();
		}
		return getFullSize();
	}

	@Override
	public boolean hasMinimalOption() 
	{
		return compressedSnippit != null;
	}

}

