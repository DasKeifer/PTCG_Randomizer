package datamanager;

import compiler.CodeSnippit;

public class UnshrinkableConstrainedBlock extends ConstrainedBlock
{
	public UnshrinkableConstrainedBlock(byte priority, CodeSnippit toPlaceInBank) 
	{
		super(priority, toPlaceInBank);
	}

	@Override
	public boolean canBeShrunkOrMoved() 
	{
		return false;
	}
	
	@Override
	public boolean shrinksNotMoves() 
	{
		return false;
	}

	@Override
	public NoConstraintBlock applyShrink() 
	{
		return null;
	}

	@Override
	public NoConstraintBlock revertShrink() 
	{
		return null;
	}

	@Override
	public int getMinimalSizeOnBank(byte bank) 
	{
		return toAdd.getMaxSizeOnBank(bank);
	}

}
