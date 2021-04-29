package datamanager;

import compiler.CodeSnippit;

public abstract class ConstrainedBlock extends FlexibleBlock
{	
	public ConstrainedBlock(byte priority, CodeSnippit toPlaceInBank) 
	{
		super(priority, toPlaceInBank);
	}
}