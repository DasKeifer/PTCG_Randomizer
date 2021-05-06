package datamanager;

import compiler.DataBlock;

public class ConstrainedBlock extends MoveableBlock
{	
	private DataBlock compressedBlock;
	
	public enum AutoCompressOption
	{
		CALL, JUMP;
	}
	
	public ConstrainedBlock(byte priority, DataBlock toPlaceInBank, DataBlock compressedBlock) 
	{
		super(priority, toPlaceInBank);
		// TODO: Copy? this.compressedBlock = new Block(compressedBlock);
	}
	
	public ConstrainedBlock(byte priority, DataBlock toPlaceInBank, AutoCompressOption compressOption) 
	{
		super(priority, toPlaceInBank);
		// TODO: generate snippet
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return compressedBlock.getWorstCaseSizeOnBank(bank);
	}

	@Override
	public boolean shrinksNotMoves() 
	{
		return true;
	}

	@Override
	public FloatingBlock applyShrink() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FloatingBlock revertShrink() 
	{
		// TODO Auto-generated method stub
		return null;
	}
}