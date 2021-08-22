package compiler.referenceInstructs;

import java.util.Map;

import compiler.Instruction;
import compiler.Segment;
import rom.Texts;
import util.ByteUtils;
import util.RomUtils;

public class BlockBankLoadedAddress extends Instruction
{
	String labelToGoTo;
	protected Segment toGoTo;
	boolean includeBank;
	
	public BlockBankLoadedAddress(String labelToGoTo, boolean includeBank)
	{
		this.labelToGoTo = labelToGoTo;
		toGoTo = null;
		this.includeBank = includeBank;
	}

	@Override
	public void extractTexts(Texts texts)
	{
		// No texts in this instruct
	}
	
	@Override
	public void linkData(
			Texts romTexts,
			Map<String, Segment> labelToLocalSegment, 
			Map<String, Segment> labelToSegment
	) 
	{		
		// No need to check for local ones - they are treated the same
		toGoTo = labelToSegment.get(labelToGoTo);
		if (toGoTo == null)
		{
			throw new IllegalArgumentException("Specified label '" + labelToGoTo + "' was not found!");
		}
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instOffset)
	{
		return includeBank ? 3 : 2;
	}
	
	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt) 
	{
		if (toGoTo == null)
		{
			throw new IllegalAccessError("Block Addresss must be linked prior to writting!");
		}

		int globalAddress = toGoTo.getAssignedAddress();
		byte bank = RomUtils.determineBank(globalAddress);
		short loadedAddress = RomUtils.convertToLoadedBankOffset(bank, globalAddress);
		
		int size = 2;
		if (includeBank)
		{
			bytes[addressToWriteAt++] = bank;
			size++;
		}
		ByteUtils.writeAsShort(loadedAddress, bytes, addressToWriteAt);
		return size;
	}
}
