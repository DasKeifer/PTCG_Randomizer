package compiler.referenceInstructs;


import java.util.Arrays;

import compiler.CompilerUtils;
import datamanager.AllocatedIndexes;
import datamanager.BankAddress;
import util.RomUtils;
import compiler.CompilerConstants.InstructionConditions;

public class Jump extends JumpCallCommon
{
	private static final byte CONDITIONLESS_INSTRUCT = (byte) 0xC3;
	private static final byte CONDITIONED_INSTRUCT = (byte) 0xC2;
	private static final byte FAR_INSTRUCT_RST_VAL = 0x30;
	
	protected boolean isLocalLabel;
	
	public Jump(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
		isLocalLabel = false;
	}
	
	public Jump(int addressToGoTo, InstructionConditions conditions) 
	{
		super(addressToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
		isLocalLabel = false;
	}
	
	public static Jump createJr(String[] args, String rootSegment)
	{		
		final String supportedArgs = "jr only supports (String labelToJumpTo) and (InstructionCondition, String labelToJumpTo): ";	
		final String offsetError = "jr cannot accept a offset for internal calls, use a reference and for writing over data see nop - " + supportedArgs;	
		
		String labelToJumpTo = args[0];
		InstructionConditions conditions = InstructionConditions.NONE;
		if (args.length == 2)
		{
			if (CompilerUtils.isHexArg(args[1]))
			{
				throw new IllegalArgumentException(offsetError + Arrays.toString(args));
			}
			labelToJumpTo = args[1];
			
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
			throw new IllegalArgumentException(supportedArgs + Arrays.toString(args));
		}
		else if (CompilerUtils.isHexArg(args[0]))
		{
			throw new IllegalArgumentException(offsetError + Arrays.toString(args));
		}

		if (CompilerUtils.isOnlySubsegmentPartOfLabel(labelToJumpTo))
		{
			return new Jump(CompilerUtils.formSegmentLabelArg(labelToJumpTo, rootSegment), conditions);
		}
		
		return new Jump(labelToJumpTo, conditions);
	}

	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AllocatedIndexes allocatedIndexes)
	{
		// TODO: Need to make sure this handles unassigned values
		// Is it a JR or a JP?
		if (isLocalLabel)
		{
			BankAddress addressToGoTo = getAddressToGoTo(allocatedIndexes);
			if (canJr(RomUtils.convertToGlobalAddress(instructAddress.bank, instructAddress.addressInBank), 
					RomUtils.convertToGlobalAddress(addressToGoTo.bank, addressToGoTo.addressInBank)))
			{
				return 2;
			}
			return 3;
		}
		// Is it a JP or a farjump? Use the parent class implementation
		else
		{
			return super.getWorstCaseSize(instructAddress, allocatedIndexes);
		}
	}
	
	private boolean canJr(int instAddress, int addressToGoTo)
	{
		// If its not local or we don't have a link yet, we can't JR
		if (!isLocalLabel || addressToGoTo == CompilerUtils.UNASSIGNED_ADDRESS)
		{
			return false;
		}
		
		// Otherwise its local and linked so we know we will use either
		// JR or JP
		
		// See how far we need to jump. 
		int diff = getJrValue(instAddress, addressToGoTo);
		if (diff > 127 || diff < -128)
		{
			return false;
		}
		
		return true;
	}
	
	private int getJrValue(int instAddress, int addressToGoTo)
	{
		if (addressToGoTo == CompilerUtils.UNASSIGNED_ADDRESS || addressToGoTo == CompilerUtils.UNASSIGNED_LOCAL_ADDRESS)
		{
			return Integer.MAX_VALUE;
		}
		
		// Minus 2 because its relative to the end of the jump
		// instruction (i.e. we jump less far) and we assume JR for this
		return addressToGoTo - instAddress - 2;
	}

	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt, AllocatedIndexes allocatedIndexes) 
	{		
		BankAddress addressToGoTo = getAddressToGoTo(allocatedIndexes);
		if (isLocalLabel)
		{
			// See if we want to JR
			int globalToGoToAddress = RomUtils.convertToGlobalAddress(addressToGoTo.bank, addressToGoTo.addressInBank);
			if (canJr(addressToWriteAt, globalToGoToAddress))
			{
				return writeJr(bytes, addressToWriteAt, (byte) getJrValue(addressToWriteAt, globalToGoToAddress));
			}
			// Otherwise its a normal jump
			else
			{
				return writeJpCall(bytes, addressToWriteAt, addressToGoTo);
			}
		}
		// If its not a local label, just do the parent - don't worry about trying
		// to do JR between blocks
		else
		{
			return super.writeBytes(bytes, addressToWriteAt, allocatedIndexes);
		}
	}
}
