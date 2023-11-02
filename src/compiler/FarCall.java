package compiler;

import compiler.CompilerConstants.InstructionConditions;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

import java.io.IOException;
import java.util.Arrays;

import gbc_framework.QueuedWriter;
import gbc_framework.SegmentNamingUtils;
import compiler.reference_instructs.BlockGlobalAddress;
import compiler.reference_instructs.Call;
import compiler.reference_instructs.Jr;
import compiler.static_instructs.Rst;

public class FarCall extends Call
{
	protected BankAddress toGoTo;
	
	protected FarCall(String labelToGoTo, InstructionConditions conditions)
	{
		super(labelToGoTo, conditions);
		toGoTo = BankAddress.UNASSIGNED;
	}
	
	protected FarCall(int addressToGoTo, InstructionConditions conditions)
	{
		super((short) -1, conditions);
		toGoTo = new BankAddress(addressToGoTo);
	}	
	
	public static FarCall create(String[] args, String rootSegment)
	{	
		final String supportedArgs = "call/farcall only supports (int gloabalAddressToGoTo), (String labelToGoTo), (InstructionCondition, int gloabalAddressToGoTo) and (InstructionCondition, String labelToGoTo): ";	
		
		String labelOrAddrToGoTo = args[0];
		InstructionConditions conditions = InstructionConditions.NONE;
		if (args.length == 2)
		{
			labelOrAddrToGoTo = args[1];
			try
			{
				conditions = CompilerUtils.parseInstructionConditionsArg(args[0]);
			}
			catch (IllegalArgumentException iae)
			{
				throw new IllegalArgumentException(supportedArgs + iae.getMessage());	
			}
		}
		else if (args.length != 1)
		{
			throw new IllegalArgumentException(supportedArgs + "given " + Arrays.toString(args));
		}
		
		// See if its a hex address
		try 
		{
			return new FarCall(CompilerUtils.parseGlobalAddrArg(labelOrAddrToGoTo), conditions);
		}
		// Otherwise it should be a label
		catch (IllegalArgumentException iae)
		{
			if (SegmentNamingUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
			{
				labelOrAddrToGoTo = CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment);
			}
			return new FarCall(labelOrAddrToGoTo, conditions);
		}
	}
	
	protected BankAddress getAddressToGoTo(AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		BankAddress found = toGoTo;
		if (!found.isFullAddress())
		{
			found = Instruction.tryGetAddress(getLabel(), assignedAddresses, tempAssigns);
		}
		return found;
	}
	
	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		BankAddress found = getAddressToGoTo(assignedAddresses, tempAssigns);
		// If its not assigned, assume the worst
		if (!instructAddress.isFullAddress() || !found.isFullAddress() || isFarJpCall(instructAddress, found))
		{
			return 4;
		}
		
		// "normal" call
		return super.getWorstCaseSize(instructAddress, assignedAddresses, tempAssigns);
	}
	
	protected static boolean isFarJpCall(BankAddress instructAddress, BankAddress toGoTo)
	{
		return !isValidCall(instructAddress, toGoTo);
	}
	
	protected int getFarJpCallSize()
	{
		// To do a conditional farcall we need to do a JR before it
		if (getConditions() != InstructionConditions.NONE)
		{
			return 6;
		}
		return 4;
	}
	
	@Override
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{	
		BankAddress foundAddress = getAddressToGoTo(assignedAddresses, null);
		if (!foundAddress.isFullAddress())
		{
			if (getLabel() != null)
			{
				throw new IllegalAccessError("FarCall tried to write address for " + getLabel() + " but it is not fully assigned");
			}
			throw new IllegalAccessError("FarCall tried to write specific address but it is not fully assigned");
		}
		
		if (isFarJpCall(instructionAddress, foundAddress))
		{			
			int writeSize = 0;
			// To do a conditional far jp/call we need to do a JR before it
			if (getConditions() != InstructionConditions.NONE)
			{
				// Write a local JR to skip the far call
				writeSize += Jr.write(writer, getConditions().negate(), (byte) 4); // 4 = size of far call
			}
			
			writeSize += writeFarJpCall(writer, foundAddress);
			return writeSize;
		}
		else // normal call
		{
			return super.writeBytes(writer, instructionAddress, assignedAddresses);
		}
	}

	protected int writeFarJpCall(QueuedWriter writer, BankAddress addressToCall) throws IOException 
	{		
		// This is an "RST" call (id 0xC7). These are special calls loaded into the ROM at the beginning. For
		// this ROM, RST5 (id 0x28) jumps to the "FarCall" function in the home.asm which handles
		// doing the call to any location in the ROM
		Rst.write(writer, (byte) 0x28);
		
		// Write the address after the rst. This is how this specific call works
		BlockGlobalAddress.write(writer, addressToCall);
		return 4;
	}
}
