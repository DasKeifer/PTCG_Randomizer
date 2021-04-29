package datamanager;

import compiler.CodeSnippit;

public class AutoMinimalConstrainedBlock extends ConstrainedBlock
{
	public enum CompressOption
	{
		NONE, CALL, JUMP;
	}
	private CompressOption compressOption;
	
	public AutoMinimalConstrainedBlock(byte priority, CodeSnippit toPlaceInBank, CompressOption compressOption) 
	{
		super(priority, toPlaceInBank);
		this.compressOption = compressOption;
	}

	@Override
	public int writeData(byte[] bytes, int index)
	{
		if (hasMinimalOption())
		{
			// TODO: generate and write?
			return compressedSnippit.write(bytes, index);
		}
		return super.writeData(bytes, index);
	}

	@Override
	public int getMinimalSize() 
	{
		switch(compressOption)
		{
			case JUMP:
			case CALL:
				// Far call/jump size
				return 4;
			case NONE:
			default:
				return getFullSize();
		}
	}

	@Override
	public boolean hasMinimalOption() 
	{
		return CompressOption.NONE != compressOption;
	}
}
