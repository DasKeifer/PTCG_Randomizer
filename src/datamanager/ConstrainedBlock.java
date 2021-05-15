package datamanager;

import compiler.DataBlock;

public class ConstrainedBlock extends MoveableBlock
{	
	private FloatingBlock shrunkBlock;
	
	public enum AutoCompressOption
	{
		CALL, JUMP;
	}
	
	public ConstrainedBlock(byte priority, DataBlock toPlaceInBank, DataBlock remotePortion) 
	{
		super(priority, toPlaceInBank);
		shrunkBlock = new FloatingBlock(priority, remotePortion);
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return shrunkBlock.getCurrentWorstCaseSizeOnBank(bank);
	}

	// TODO: Condense?
	@Override
	public boolean shrinksNotMoves() 
	{
		return true;
	}

	@Override
	public FloatingBlock applyShrink() 
	{
		return shrunkBlock;
	}

	@Override
	public FloatingBlock revertShrink() 
	{
		return shrunkBlock;
	}
}