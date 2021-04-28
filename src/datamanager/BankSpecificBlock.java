package datamanager;

import compiler.CodeSnippit;
import util.RomUtils;

public class BankSpecificBlock extends FlexibleBlock
{
	byte bank;
	CodeSnippit minimalBlock;
	
	protected BankSpecificBlock(CodeSnippit toPlaceInBank, byte bank)
	{
		super(toPlaceInBank);
		this.bank = bank;
	}
	
	public void addMinimalOption(CodeSnippit minimalSnippit)
	{
		minimalBlock = minimalSnippit;
	}
	
	public void addAllowableAddressRange(byte priority, int globalAddressStart, int globalAddressEnd)
	{
		if (RomUtils.determineBank(globalAddressStart) != bank || 
				RomUtils.determineBank(globalAddressEnd) != bank)
		{
			throw new IllegalArgumentException("The passed address range " + globalAddressStart + " - " + 
						globalAddressEnd + " is within bank " + bank + "'s address space");
		}
		
		super.addAllowableAddressRange(priority, globalAddressStart, globalAddressEnd);
	}

	public int getSize()
	{
		return 0
	}
	
	public int getMinimalSize()
	{
		return 
	}
}
