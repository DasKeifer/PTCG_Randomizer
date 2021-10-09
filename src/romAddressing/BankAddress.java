package romAddressing;

import constants.RomConstants;
import util.RomUtils;


public class BankAddress 
{
	public static final byte UNASSIGNED_BANK = -1;
	public static final short UNASSIGNED_ADDRESS = -1;
	
	public static final BankAddress UNASSIGNED = new BankAddress(UNASSIGNED_BANK, UNASSIGNED_ADDRESS);
	
	private byte bank;
	private short addressInBank;
	
	public BankAddress()
	{
		bank = UNASSIGNED.bank;
		addressInBank = UNASSIGNED.addressInBank;
	}
	
	public BankAddress(byte bank, short addressInBank)
	{
		setBank(bank);
		setAddressInBank(addressInBank);
	}

	public BankAddress(BankAddress toCopy) 
	{
		bank = toCopy.bank;
		addressInBank = toCopy.addressInBank;
	}
	
	public BankAddress(int globalAddress) 
	{		
		if (globalAddress < 0 || globalAddress >= RomConstants.NUMBER_OF_BANKS * RomConstants.BANK_SIZE)
		{
			throw new IllegalArgumentException("BankAddress - invalid globalAddress given (" + globalAddress + 
					") - the globalAddress must be between 0 and " + RomConstants.NUMBER_OF_BANKS * RomConstants.BANK_SIZE); 
		}
		setBank(RomUtils.determineBank(globalAddress));
		setAddressInBank(RomUtils.convertToBankOffset(globalAddress));
	}
	
	public BankAddress newAtStartOfBank()
	{
		return new BankAddress(bank, (short) 0);
	}
	
	public BankAddress newOffsetted(int offset)
	{
		if (!isAddressInBankRange(addressInBank + offset))
		{
			return null;
		}
		return new BankAddress(bank, (short) (addressInBank + offset));
	}
	
	/// Make the passed address relative to this objects addressInBank
	public BankAddress newRelativeToAddressInBank(int addressToMakeRelativeToAddressInBank)
	{
		if (!isAddressInBankRange(addressToMakeRelativeToAddressInBank - addressInBank))
		{
			return null;
		}
		return new BankAddress(bank, (short) (addressToMakeRelativeToAddressInBank - addressInBank));
	}

	public void setToCopyOf(BankAddress toCopy) 
	{
		bank = toCopy.bank;
		addressInBank = toCopy.addressInBank;
	}

	public void setBank(byte bank) 
	{
		if (!isBankRange(bank))
		{
			throw new IllegalArgumentException("BankAddress - invalid bank given (" + bank + 
					") - the bank must be between 0 and " + RomConstants.NUMBER_OF_BANKS + 
					" or the reserved UNASSIGNED_BANK value (" + UNASSIGNED_BANK + ")"); 
		}
		this.bank = bank;
	}
	
	public void setAddressInBank(short addressInBank) 
	{
		if (!isAddressInBankRange(addressInBank))
		{
			throw new IllegalArgumentException("BankAddress - invalid addressInBank given (" + addressInBank + 
					") - the bank must be between 0 and " + RomConstants.BANK_SIZE +
					" or the reserved UNASSIGNED_ADDRESS value (" + UNASSIGNED_ADDRESS + ")"); 
		}
		this.addressInBank = addressInBank;
	}
	
	private boolean isBankRange(byte toCheck)
	{
		return (toCheck >= 0 && toCheck < RomConstants.NUMBER_OF_BANKS) ||
				toCheck == UNASSIGNED_BANK;
	}
	
	private boolean isAddressInBankRange(int toCheck)
	{
		return (toCheck >= 0 && toCheck < RomConstants.BANK_SIZE) ||
				toCheck == UNASSIGNED_ADDRESS;
	}

	public boolean isFullAddress() 
	{
		return bank != UNASSIGNED_BANK && addressInBank != UNASSIGNED_ADDRESS;
	}

	public boolean isSameBank(BankAddress toCheck) 
	{
		return bank == toCheck.bank;
	}

	public boolean fitsInBankAddress(int size) 
	{
		// Can't check if this address isn't assigned yet
		if (addressInBank == UNASSIGNED_ADDRESS)
		{
			return false;
		}
		
		// -1 to make the check "inclusive"
		// i.e. if the address is the last address in the bank, it can still fit
		// one byte at that address. If its the first address in the bank, then
		// it can fit the whole bank size
		return isAddressInBankRange(addressInBank + size - 1);
	}

	public byte getBank()
	{
		return bank;
	}

	public short getAddressInBank()
	{
		return addressInBank;
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

	@Override
	public String toString()
	{
		return String.format("0x%x:%4x", bank, addressInBank);
	}
}
