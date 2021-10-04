package datamanager;

import java.util.List;

import compiler.staticInstructs.Nop;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;
import util.ByteUtils;
import util.RomUtils;

public class ReplacementBlock extends FixedBlock
{
	int replaceLength;
	
	// TODO: Internally pad datablock with nops as part of preparing for alloc? That would make sense to me
	// TODO: add a way to get the end of the fixed block to skip the nops?
	
	// For write overs with unpredictable sizes including "minimal jumps" for extending/slicing existing code
	// Auto fill with nop (0x00) after
	public ReplacementBlock(String startingSegmentName, int fixedStartAddress)
	{
		super(startingSegmentName, fixedStartAddress);
		setCommonData(-1);
	}
	
	public ReplacementBlock(String startingSegmentName, int fixedStartAddress, int replaceLength)
	{
		super(startingSegmentName, fixedStartAddress);
		setCommonData(replaceLength);
	}
	
	public ReplacementBlock(List<String> sourceLines, int fixedStartAddress, int replaceLength)
	{
		super(sourceLines, fixedStartAddress);
		setCommonData(replaceLength);
	}
	
	private void setCommonData(int replaceLength)
	{
		this.replaceLength = replaceLength;
	}
	
	public void setReplaceLength(int length)
	{
		replaceLength = length;
	}
	
	boolean debug = false;
	@Override
	public void writeBytes(byte[] bytes, AssignedAddresses assignedAddresses)
	{
		if (replaceLength <= 0)
		{
			throw new IllegalArgumentException("ReplacementBlock replaceLength is invalid (" + replaceLength + ") - it must be greater than 0");
		}
		
		// Preliminary safety check. Need to handle non-continuous write detection
		// TODO: More Safety Check?
		if (getWorstCaseSize(assignedAddresses) > replaceLength)
		{
			throw new IllegalArgumentException("Data size (" + getWorstCaseSize(assignedAddresses) + ") is larger than allowed size (" + replaceLength + ")");
		}
			
		BankAddress bankAddress = assignedAddresses.getThrow(getId());
		int globalAddress = RomUtils.convertToGlobalAddress(bankAddress.bank, bankAddress.addressInBank);
		
		// TODO: Optimize? Write all extra bytes instead? Think this current way was leftover from previous approach
		ByteUtils.setBytes(bytes, globalAddress, replaceLength, Nop.NOP_VALUE);
		
		super.writeBytes(bytes, assignedAddresses);

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
