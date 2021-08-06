package datamanager;


import compiler.DataBlock;

public class UnshrinkableMoveBlock extends MoveableBlock
{
	public UnshrinkableMoveBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
	{
		super(priority, toPlaceInBank, prefs);
	}

	@Override
	public boolean canBeShrunkOrMoved() 
	{
		return true;
	}
	
	@Override
	public boolean movesNotShrinks() 
	{
		return true;
	}

	@Override
	public UnconstrainedMoveBlock getRemoteBlock() 
	{
		return null;
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return dataBlock.getWorstCaseSizeOnBank(bank);
	}
}
