package datamanager;


import compiler.CodeSnippit;

public class CustomMinimalBlock extends ConstrainedBlock
{
	private CodeSnippit compressedSnippit;
	
	public CustomMinimalBlock(byte priority, CodeSnippit toPlaceInBank, CodeSnippit compressedSnippit) 
	{
		super(priority, toPlaceInBank);
		// TODO: Copy? this.compressedSnippit = new CodeSnippit(compressedSnippit);
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
	public NoConstraintBlock applyShrink() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NoConstraintBlock revertShrink() {
		// TODO Auto-generated method stub
		return null;
	}
}

