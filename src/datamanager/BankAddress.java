package datamanager;

import util.RomUtils;

public class BankAddress 
{
	public static final byte UNASSIGNED_BANK = -1;
	public static final short UNASSIGNED_ADDRESS = -1;
	
	public static final BankAddress UNASSIGNED = new BankAddress(UNASSIGNED_BANK, UNASSIGNED_ADDRESS);
	
	public byte bank;
	public short addressInBank;
	
	public BankAddress()
	{
		bank = UNASSIGNED.bank;
		addressInBank = UNASSIGNED.addressInBank;
	}
	
	public BankAddress(byte bank, short addressInBank)
	{
		this.bank = bank;
		this.addressInBank = addressInBank;
	}

	public BankAddress(BankAddress toCopy) 
	{
		bank = toCopy.bank;
		addressInBank = toCopy.addressInBank;
	}

	public BankAddress(int globalAddress) 
	{
		bank = RomUtils.determineBank(globalAddress);
		addressInBank = RomUtils.convertToBankOffset(globalAddress);
	}
}
