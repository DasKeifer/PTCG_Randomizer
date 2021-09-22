package util;

import constants.RomConstants;

public class RomUtils 
{
	public static byte determineBank(int address)
	{
		return (byte) (address / RomConstants.BANK_SIZE);
	}

	public static short convertToBankOffset(int globalAddress) 
	{
		byte bank = determineBank(globalAddress);
		return convertToBankOffset(bank, globalAddress);
	}
	
	public static short convertToLoadedBankOffset(int globalAddress)
	{
		byte bank = determineBank(globalAddress);
		return convertToLoadedBankOffset(bank, globalAddress);
	}
	
	public static short convertToBankOffset(byte bank, int globalAddress) 
	{
		return (short) (globalAddress - bank * RomConstants.BANK_SIZE);
	}
	
	public static short convertToLoadedBankOffset(byte bank, int globalAddress)
	{
		// If its bank 0 or 1, no changes are needed
		if (bank == 0 || bank == 1)
		{
			return (short) globalAddress;
		}
		
		// Otherwise adjust it appropriately to be in the second bank
		return (short) (convertToBankOffset(bank, globalAddress) + RomConstants.BANK_SIZE);
	}
	
	public static int getEndOfBankAddressIsIn(int address)
	{
		return (determineBank(address) + 1) * RomConstants.BANK_SIZE;
	}
	
	public static int[] getBankBounds(byte bank)
	{
		return new int[] {bank * RomConstants.BANK_SIZE, (bank + 1) * RomConstants.BANK_SIZE - 1};
	}
	
	public static boolean isInBank(int address, byte bank)
	{
		return determineBank(address) == bank;
	}

	public static int convertToGlobalAddress(byte bank, short addressInBank)
	{
		return bank * RomConstants.BANK_SIZE + addressInBank;
	}

	public static int convertToGlobalAddressFromLoadedBankOffset(byte bank, short loadedBankAddress)
	{
		// Bank 0 & 1 are a bit special cases - no adjustment needed
		if (bank == 0 || bank == 1)
		{
			return loadedBankAddress;
		}
		return (bank - 1) * RomConstants.BANK_SIZE + loadedBankAddress;
	}

	public static short convertFromBankOffsetToLoadedOffset(byte bank, short addressInBank)
	{
		// Bank 0 is a bit special cases - no adjustment needed
		if (bank == 0)
		{
			return addressInBank;
		}
		return (short) (addressInBank + RomConstants.BANK_SIZE);
	}
}
