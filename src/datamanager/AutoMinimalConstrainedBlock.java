package datamanager;

import compiler.CodeSnippit;

// TODO: extend customConstraintedBlock? or maybe better make this as a helper/constructor for it
public class AutoMinimalConstrainedBlock extends ConstrainedBlock
{
	public enum CompressOption
	{
		CALL, JUMP;
	}
	private CompressOption compressOption;
	
	public AutoMinimalConstrainedBlock(byte priority, CodeSnippit toPlaceInBank, CompressOption compressOption) 
	{
		super(priority, toPlaceInBank);
		this.compressOption = compressOption;
	}

	@Override
	public int getMinimalSizeOnBank(byte bank) 
	{
		// TODO: Far call/jump size
		return 4;
	}

	@Override
	public boolean shrinksNotMoves() 
	{
		return true;
	}

	@Override
	public NoConstraintBlock applyShrink() 
	{
		// TODO
		return null;
	}

	@Override
	public NoConstraintBlock revertShrink() 
	{
		// TODO
		return null;
	}
}
