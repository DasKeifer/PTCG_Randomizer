package compiler.referenceInstructs;


import compiler.FixedLengthInstruct;
import datamanager.AllocatedIndexes;
import rom.Texts;
import util.ByteUtils;

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
	public int getWorstCaseSizeOnBank(byte unused1, int unused2, AllocatedIndexes unused3)
	{
		return SIZE;
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int addressToWriteAt, AllocatedIndexes allocatedIndexes) 
	{
		Integer globalAddress = allocatedIndexes.get(addressLabel);
		if (globalAddress == null)
		{
			throw new IllegalAccessError("TODOOOOO");
		}
		
		ByteUtils.writeLittleEndian(globalAddress - offset, bytes, addressToWriteAt, SIZE);
	}
}
