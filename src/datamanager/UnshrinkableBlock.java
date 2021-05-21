package datamanager;


import compiler.DataBlock;

// TODO what is this useful for?
public class UnshrinkableBlock extends MoveableBlock
{
	public UnshrinkableBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
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
	public FloatingBlock getRemoteBlock() 
	{
		return null;
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return dataBlock.getWorstCaseSizeOnBank(bank);
	}
}
