package datamanager;


import java.util.Map;

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
	public int getShrunkWorstCaseSizeOnBank(byte bankToGetSizeOn, int allocAddress, Map<String, Integer> allocatedIndexes) 
	{
		return dataBlock.getWorstCaseSizeOnBank(bankToGetSizeOn, allocAddress, allocatedIndexes);
	}
}
