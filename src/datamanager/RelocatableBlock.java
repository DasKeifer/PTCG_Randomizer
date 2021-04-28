package datamanager;


import compiler.CodeSnippit;
import util.RomUtils;

public abstract class RelocatableBlock extends FlexibleBlock
{	
	protected RelocatableBlock(CodeSnippit toPlaceInBank)
	{
		super(toPlaceInBank);
	}
	
	public void addAllowableBank(byte priority, byte bank)
	{
		allowableAddressPreferences.put(priority, RomUtils.getBankBounds(bank));
	}
}
