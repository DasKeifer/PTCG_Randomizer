package datamanager;

import compiler.CodeSnippit;

public class ConstrainedBlock extends MoveableBlock
{	
	private CodeSnippit compressedSnippit;
	
	public enum AutoCompressOption
	{
		CALL, JUMP;
	}
	
	public ConstrainedBlock(byte priority, CodeSnippit toPlaceInBank, CodeSnippit compressedSnippit) 
	{
		super(priority, toPlaceInBank);
		// TODO: Copy? this.compressedSnippit = new CodeSnippit(compressedSnippit);
	}
	
	public ConstrainedBlock(byte priority, CodeSnippit toPlaceInBank, AutoCompressOption compressOption) 
	{
		super(priority, toPlaceInBank);
		// TODO: generate snippet
	}

	@Override
	public int getMinimalSizeOnBank(byte bank) 
	{
		return compressedSnippit.getMaxSizeOnBank(bank);
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
	public FloatingBlock revertShrink() {
		// TODO Auto-generated method stub
		return null;
	}
}