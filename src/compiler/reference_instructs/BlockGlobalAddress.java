package compiler.reference_instructs;


import compiler.FixedLengthInstruct;
import rom.Texts;
import rom_addressing.AssignedAddresses;
import rom_addressing.BankAddress;
import util.ByteUtils;
import util.RomUtils;

public class BlockGlobalAddress extends FixedLengthInstruct
{
	static final int SIZE = 3;
	String addressLabel;
	int offset;
	
	public BlockGlobalAddress(String addressLabel, int offset)
	{
		super(SIZE);
		this.addressLabel = addressLabel;
		this.offset = offset;
	}

	@Override
	public void extractText(Texts texts)
	{
		// No texts in this instruct
	}

	@Override
	public int getWorstCaseSize(BankAddress unused1, AssignedAddresses unused2, AssignedAddresses unused3)
	{
		return SIZE;
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses) 
	{
		BankAddress address = assignedAddresses.getTry(addressLabel);
		if (!address.isFullAddress())
		{
			throw new IllegalAccessError("BlockGlobalAddress tried to write address for " + addressLabel + " but it is not fully assigned: " + address.toString());
		}
		
		ByteUtils.writeLittleEndian(RomUtils.convertToGlobalAddress(address) - (long) offset, bytes, addressToWriteAt, SIZE);
	}
}
