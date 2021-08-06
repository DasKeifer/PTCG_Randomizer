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
		return false;
	}
	
	@Override
	public boolean movesNotShrinks() 
	{
		return false; // Does neither but default to shrink since that would at least leave it in the bank
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
