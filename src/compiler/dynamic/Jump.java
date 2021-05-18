package compiler.dynamic;


import java.util.Map;

import compiler.CompilerUtils;
import compiler.SegmentReference;
import compiler.CompilerConstants.InstructionConditions;
import rom.Texts;

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
		
		String labelToJumpTo = args[0];
		InstructionConditions conditions = InstructionConditions.NONE;
		if (args.length == 2)
		{
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
			throw new IllegalArgumentException(supportedArgs + args.toString());
		}

		return new Jump(CompilerUtils.formSegmentLabelArg(labelToJumpTo, rootSegment), conditions);
	}

	@Override
	public void linkData(
			Texts romTexts,
			Map<String, SegmentReference> labelToLocalSegment, 
			Map<String, SegmentReference> labelToSegment
	) 
	{
		if (!labelToGoTo.isEmpty() && toGoTo == null)
		{
			toGoTo = labelToLocalSegment.get(labelToGoTo);
			if (toGoTo != null)
			{
				isLocalLabel = true;
			}
			else
			{
				isLocalLabel = false;
				toGoTo = labelToSegment.get(labelToGoTo);
			}
			if (toGoTo == null)
			{
				throw new IllegalArgumentException("Specified label '" + labelToGoTo + "' was not found!");
			}
		}
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instOffset)
	{
		// Is it a JR or a JP?
		if (isLocalLabel)
		{
			if (canJr(instOffset))
			{
				return 2;
			}
			return 3;
		}
		// Is it a JP or a farjump? Use the parent class implementation
		else
		{
			return super.getWorstCaseSizeOnBank(bank, instOffset);
		}
	}
	
	private boolean canJr(int instAddress)
	{
		// If its not local or we don't have a link yet, we can't JR
		if (!isLocalLabel || toGoTo == null)
		{
			return false;
		}
		
		// Otherwise its local and linked so we know we will use either
		// JR or JP
		
		// See how far we need to jump. 
		int diff = getJrValue(instAddress);
		if (diff > 127 || diff < -128)
		{
			return false;
		}
		
		return true;
	}
	
	private int getJrValue(int instAddress)
	{
		int goToAddr = toGoTo.getAssignedAddress();
		if (goToAddr == CompilerUtils.UNASSIGNED_ADDRESS || goToAddr == CompilerUtils.UNASSIGNED_LOCAL_ADDRESS)
		{
			return Integer.MAX_VALUE;
		}
		
		// Plus 2 because its relative to the
		// end of the jump instruction and we assume JR for this
		return goToAddr - instAddress + 2;
	}

	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt) 
	{		
		if (isLocalLabel)
		{
			// See if we want to JR
			if (canJr(addressToWriteAt))
			{
				return writeJr(bytes, addressToWriteAt, (byte) getJrValue(addressToWriteAt));
			}
			// Otherwise its a normal jump
			else
			{
				return writeJpCall(bytes, addressToWriteAt);
			}
		}
		// If its not a local label, just do the parent - don't worry about trying
		// to do JR between blocks
		else
		{
			return super.writeBytes(bytes, addressToWriteAt);
		}
	}
}
