package datamanager;

import compiler.DataBlock;
import compiler.staticInstructs.Nop;
import util.ByteUtils;
import util.RomUtils;

public class ReplacementBlock extends FixedBlock
{
	int replaceLength;
	
	// TODO: Internally pad datablock with nops as part of preparing for alloc? That would make sense to me
	
	// TODO: Add optional integrity checking surrounding area and replace
	// TODO: only allow fixed length data blocks to not have a replace length
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public ReplacementBlock(int startAddress, DataBlock replaceWith, int replaceLength)
	{
		super(startAddress, replaceWith);
		this.replaceLength = replaceLength;
	}
	
	// TODO: add a way to get the end of the fixed block to skip the nops?
	boolean debug = false;
	@Override
	public void writeData(byte[] bytes, AllocatedIndexes allocatedIndexes)
	{
		// Preliminary safety check. Need to handle non-continuous write detection
		// TODO: More Safety Check?
		if (dataBlock.getWorstCaseSize(allocatedIndexes) > replaceLength)
		{
			throw new IllegalArgumentException("Data size (" + dataBlock.getWorstCaseSize(allocatedIndexes) + ") is larger than allowed size (" + replaceLength + ")");
		}
			
		BankAddress bankAddress = allocatedIndexes.getThrow(getId());
		int globalAddress = RomUtils.convertToGlobalAddress(bankAddress.bank, bankAddress.addressInBank);
		
		// TODO: Optimize? Write all extra bytes instead? Think this current way was leftover from previous approach
		ByteUtils.setBytes(bytes, globalAddress, replaceLength, Nop.NOP_VALUE);
		
		super.writeData(bytes, allocatedIndexes);

//		debug = getId().contains("MoreEffect");
		if (debug)
		{
			System.out.println("HCE - " + getId());
			int address = RomUtils.convertToGlobalAddress(getFixedAddress().bank, getFixedAddress().addressInBank);
			for (int i = address; i < address + replaceLength; i++)
			{
				System.out.println(String.format("0x%x - 0x%x", i, bytes[i]));
			}
		}
	}
}
