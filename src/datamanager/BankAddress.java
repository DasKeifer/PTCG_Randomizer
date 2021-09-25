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

	public void setToCopyOf(BankAddress toCopy) 
	{
		bank = toCopy.bank;
		addressInBank = toCopy.addressInBank;
	}
	
    @Override
    public boolean equals(Object o) {
 
        // If the object is compared with itself then return true 
        if (o == this) {
            return true;
        }
 
        // Check if it is an instance of BankAddress
        if (!(o instanceof BankAddress)) 
        {
            return false;
        }
        
        // Compare the data and return accordingly
        BankAddress ba = (BankAddress) o;
        return Byte.compare(bank, ba.bank) == 0 && Short.compare(addressInBank, ba.addressInBank) == 0;
    }

	public boolean isFullAddress() 
	{
		return bank != UNASSIGNED_BANK && addressInBank != UNASSIGNED_ADDRESS;
	}

	// TODO: tostring
}
