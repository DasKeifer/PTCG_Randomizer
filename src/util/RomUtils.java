package util;

import constants.RomConstants;

public class RomUtils 
{
	public static byte determineBank(int address)
	{
		return (byte) (address / RomConstants.BANK_SIZE);
	}
	
	public static short convertToInBankOffset(int globalAddress)
	{
		byte bank = determineBank(globalAddress);
		return convertToInBankOffset(bank, globalAddress);
	}
	
	public static short convertToInBankOffset(byte bank, int globalAddress)
	{
		// If its bank 0 or 1, no changes are needed
		if (bank == 0 || bank == 1)
		{
			return (short) globalAddress;
		}
		
		// Otherwise adjust it appropriately to be in the second bank
		return (short) (globalAddress - (bank - 1) * RomConstants.BANK_SIZE);
	}
}
