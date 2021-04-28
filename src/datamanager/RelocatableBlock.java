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
		int[] range = RomUtils.getBankBounds(bank);
		allowableAddressPreferences.put(priority, new AddressRange(range[0], range[1]));
	}
}
