package datamanager;

import compiler.CodeSnippit;

public class UnshrinkableBlock extends MoveableBlock
{
	public UnshrinkableBlock(byte priority, CodeSnippit toPlaceInBank) 
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
	public FloatingBlock applyShrink() 
	{
		return null;
	}

	@Override
	public FloatingBlock revertShrink() 
	{
		return null;
	}

	@Override
	public int getMinimalSizeOnBank(byte bank) 
	{
		return toAdd.getMaxSizeOnBank(bank);
	}

}
