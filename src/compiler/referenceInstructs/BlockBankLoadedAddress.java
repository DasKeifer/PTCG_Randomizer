package compiler.referenceInstructs;


import compiler.FixedLengthInstruct;
import datamanager.AllocatedIndexes;
import datamanager.BankAddress;
import rom.Texts;
import util.ByteUtils;
import util.RomUtils;


public class BlockBankLoadedAddress extends FixedLengthInstruct
{
	String addressLabel;
	boolean includeBank;
	
	public BlockBankLoadedAddress(String addressLabel, boolean includeBank)
	{
		// Size depends on if the bank is included or not
		super(includeBank ? 3 : 2);
		this.addressLabel = addressLabel;
		this.includeBank = includeBank;
	}

	@Override
	public void extractTexts(Texts texts)
	{
		// No texts in this instruct
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int addressToWriteAt, AllocatedIndexes allocatedIndexes) 
	{
		BankAddress address = allocatedIndexes.get(addressLabel);
		if (address == BankAddress.UNASSIGNED)
		{
			throw new IllegalAccessError("TODO!");
		}
		
		if (includeBank)
		{
			bytes[addressToWriteAt++] = address.bank;
		}
		ByteUtils.writeAsShort(RomUtils.convertFromBankOffsetToLoadedOffset(address.bank, address.addressInBank), bytes, addressToWriteAt);
	}
}
