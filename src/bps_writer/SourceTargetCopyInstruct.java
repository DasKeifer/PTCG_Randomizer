package bps_writer;

import java.io.IOException;
import java.util.Arrays;

import bps_writer.BpsWriter.BpsHunkCopyType;
import compiler.FixedLengthInstruct;
import gbc_framework.SegmentedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.RomUtils;


public class SourceTargetCopyInstruct extends FixedLengthInstruct
{
	private int copyFromStartIndex;
	private BpsHunkCopyType type;
	
	// No version that takes an address because thats only use in the case where we
	// use an address from the rom which will always use the default logic
	public SourceTargetCopyInstruct(int copyFromStartIndex, int copyLength, BpsHunkCopyType type) 
	{
		super(copyLength);
		this.copyFromStartIndex = copyFromStartIndex;
		this.type = type;
	}
	
	public static SourceTargetCopyInstruct create(String key, String[] args)
	{	
		final String supportedArgs = "bps_sc and bps_tc only supports (int Offset, int copyLength): ";	

		if (args.length != 2)
		{
			throw new IllegalArgumentException(supportedArgs + "given: " + Arrays.toString(args));
		}
		
		// Determine the type
		BpsHunkCopyType type;
		if (key.endsWith("sc"))
		{
			type = BpsHunkCopyType.SOURCE_COPY;
		}
		else if (key.endsWith("tc"))
		{
			type = BpsHunkCopyType.TARGET_COPY;
		}
		else
		{
			throw new IllegalArgumentException(supportedArgs + "given: " + Arrays.toString(args));
		}
		
		try
		{
			return new SourceTargetCopyInstruct(Integer.parseInt(args[0]), Integer.parseInt(args[1]), type);
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException(supportedArgs + "given: " + Arrays.toString(args) + " and encountered error: "+ iae.getMessage());
		}
	}
	
	@Override
	public void writeFixedSizeBytes(SegmentedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{
		if (writer instanceof BpsWriter)
		{
			// We need to add a new hunk for the copy and then another new one for the next read segment
			int instructAddr = RomUtils.convertToGlobalAddress(instructionAddress);
			((BpsWriter)writer).newCopyHunk(instructAddr, type, copyFromStartIndex, getSize());
			((BpsWriter)writer).newSegment(instructAddr + getSize());
		}
		else
		{
			throw new IllegalArgumentException("TODO:");
		}
	}
}

