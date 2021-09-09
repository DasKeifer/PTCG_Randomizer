package compiler.referenceInstructs;


import compiler.FixedLengthInstruct;
import datamanager.AllocatedIndexes;
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
		Integer globalAddress = allocatedIndexes.get(addressLabel);
		if (globalAddress == null)
		{
			throw new IllegalAccessError("TODOOOOO");
		}
		
		byte bank = RomUtils.determineBank(globalAddress);
		short loadedAddress = RomUtils.convertToLoadedBankOffset(bank, globalAddress);
		
		if (includeBank)
		{
			bytes[addressToWriteAt++] = bank;
		}
		ByteUtils.writeAsShort(loadedAddress, bytes, addressToWriteAt);
	}
}
