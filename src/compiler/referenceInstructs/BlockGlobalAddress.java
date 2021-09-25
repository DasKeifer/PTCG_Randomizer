package compiler.referenceInstructs;


import compiler.FixedLengthInstruct;
import datamanager.AllocatedIndexes;
import datamanager.BankAddress;
import rom.Texts;
import util.ByteUtils;
import util.RomUtils;

public class BlockGlobalAddress extends FixedLengthInstruct
{
	final static int SIZE = 3;
	String addressLabel;
	int offset;
	
	public BlockGlobalAddress(String addressLabel, int offset)
	{
		super(SIZE);
		this.addressLabel = addressLabel;
		this.offset = offset;
	}

	@Override
	public void extractTexts(Texts texts)
	{
		// No texts in this instruct
	}

	@Override
	public int getWorstCaseSize(BankAddress unused1, AllocatedIndexes unused2, AllocatedIndexes unused3)
	{
		return SIZE;
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int addressToWriteAt, AllocatedIndexes allocatedIndexes) 
	{
		BankAddress address = allocatedIndexes.getTry(addressLabel);
		if (address == BankAddress.UNASSIGNED)
		{
			throw new IllegalAccessError("TODO!");
		}
		
		ByteUtils.writeLittleEndian(RomUtils.convertToGlobalAddress(address.bank, address.addressInBank) - offset, bytes, addressToWriteAt, SIZE);
	}
}
