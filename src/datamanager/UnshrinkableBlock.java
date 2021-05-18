package datamanager;


import compiler.DataBlock;

public class UnshrinkableBlock extends MoveableBlock
{
	public UnshrinkableBlock(byte priority, DataBlock toPlaceInBank) 
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
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return dataBlock.getWorstCaseSizeOnBank(bank);
	}
}
