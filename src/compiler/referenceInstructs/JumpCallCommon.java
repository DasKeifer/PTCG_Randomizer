package compiler.referenceInstructs;


import compiler.CompilerConstants.InstructionConditions;
import rom.Texts;
import romAddressing.AssignedAddresses;
import romAddressing.BankAddress;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.Instruction;
import util.ByteUtils;
import util.RomUtils;

public abstract class JumpCallCommon extends Instruction
{
	InstructionConditions conditions;
	String labelToGoTo;
	BankAddress addressToGoTo;
	
	private byte conditionlessInstruct;
	private byte conditionedInstruct;
	private byte farInstuctRstVal;
	
	
	protected JumpCallCommon(String labelToGoTo, InstructionConditions conditions, byte conditionlessInstruct, byte conditionedInstruct, byte farInstuctRstVal)
	{
		this.conditions = conditions;
		this.labelToGoTo = labelToGoTo;
		this.conditionlessInstruct = conditionlessInstruct;
		this.conditionedInstruct = conditionedInstruct;
		this.farInstuctRstVal = farInstuctRstVal;
		
		addressToGoTo = BankAddress.UNASSIGNED;
	}
	
	protected JumpCallCommon(int addressToGoTo, InstructionConditions conditions, byte conditionlessInstruct, byte conditionedInstruct, byte farInstuctRstVal)
	{
		this.conditions = conditions;
		labelToGoTo = "";
		this.conditionlessInstruct = conditionlessInstruct;
		this.conditionedInstruct = conditionedInstruct;
		this.farInstuctRstVal = farInstuctRstVal;
		
		this.addressToGoTo = new BankAddress(addressToGoTo);
	}
	
	public static boolean useRootSegment(String[] args, boolean isJp)
	{
		String callOrJpString = "call/farcall";
		if (isJp)
		{
			callOrJpString = "jr/jp/farjp";
		}
		
		String labelOrAddrToGoTo = args[0];
		if (args.length == 2)
		{
			labelOrAddrToGoTo = args[1];
		}
		else if (args.length != 1)
		{
			throw new IllegalArgumentException(callOrJpString + " only supports 1 or 2 args: given " + Arrays.toString(args));
		}
		
		// If its a subsegment it uses the root segment
		return CompilerUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo);
	}
	
	
	public static JumpCallCommon create(String[] args, String rootSegment, boolean isJp)
	{	
		final String supportedArgs = " only supports (int gloabalAddressToGoTo), (String labelToGoTo), (InstructionCondition, int gloabalAddressToGoTo) and (InstructionCondition, String labelToGoTo): ";	
		String callOrJpString = "call/farcall";
		if (isJp)
		{
			callOrJpString = "jp/farjp";
		}
		
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
				throw new IllegalArgumentException(callOrJpString + supportedArgs + iae.getMessage());	
			}
		}
		else if (args.length != 1)
		{
			throw new IllegalArgumentException(callOrJpString + supportedArgs + "given " + Arrays.toString(args));
		}

		// See if its a hex address
		try 
		{
			if (isJp)
			{
				return new Jump(CompilerUtils.parseGlobalAddrArg(labelOrAddrToGoTo), conditions);
			}
			else
			{
				return new Call(CompilerUtils.parseGlobalAddrArg(labelOrAddrToGoTo), conditions);
			}
		}
		// Otherwise it should be a label
		catch (IllegalArgumentException iae)
		{
			if (isJp)
			{
				if (CompilerUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
				{	
					return new Jump(CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment), conditions);
				}
				return new Jump(labelOrAddrToGoTo, conditions);
			}
			else
			{
				if (CompilerUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
				{	
					return new Call(CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment), conditions);
				}
				return new Call(labelOrAddrToGoTo, conditions);
			}
		}
	}
	
	protected BankAddress getAddressToGoTo(AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		BankAddress address = addressToGoTo;
		if (address == BankAddress.UNASSIGNED)
		{
			// Try and get it from the temp indexes first
			if (tempAssigns != null)
			{
				address = tempAssigns.getTry(labelToGoTo);
			}
			
			if (address == BankAddress.UNASSIGNED)
			{
				address = assignedAddresses.getTry(labelToGoTo);
				if (address == BankAddress.UNASSIGNED)
				{
					return BankAddress.UNASSIGNED;
				}
			}
		}
		return address;
	}

	@Override
	public void extractTexts(Texts texts)
	{
		// No texts in this instruct
	}
	
	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		if (isFarJpCall(instructAddress, getAddressToGoTo(assignedAddresses, tempAssigns)))
		{
			// If its not assigned, assume the worst
			return getFarJpCallSize();
		}
		
		// local jp/call
		return 3;
	}
	
	protected static boolean isFarJpCall(BankAddress instructAddress, BankAddress toGoTo)
	{
		// If either bank is unassigned, assume the worst
		if (instructAddress.bank == BankAddress.UNASSIGNED_BANK || toGoTo.bank == BankAddress.UNASSIGNED_BANK)
		{
			return true;
		}
		
		// If its assigned a specific address and its in the same bank or its in the home bank
		if (isInBankOrHomeBank(instructAddress.bank, toGoTo.bank))
		{
			return false;
		}
		
		return true;
	}
	
	protected static boolean isInBankOrHomeBank(byte instructBank, byte toGoToBank)
	{
		return toGoToBank == 0 || toGoToBank == instructBank;
	}
	
	protected int getFarJpCallSize()
	{
		// To do a conditional far jp/call we need to do a JR before it
		if (conditions != InstructionConditions.NONE)
		{
			return 6;
		}
		return 4;
	}
	
	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses) 
	{
		int writeIdx = addressToWriteAt;
		BankAddress toGoToAddress = getAddressToGoTo(assignedAddresses, null);
		if (!addressToGoTo.isFullAddress())
		{
			if (labelToGoTo != null)
			{
				throw new IllegalAccessError("JumpCallCommon tried to write address for " + labelToGoTo + " but it is not fully assigned: " + addressToGoTo.toString());
			}
			throw new IllegalAccessError("JumpCallCommon tried to write specific address but it is not fully assigned: " + addressToGoTo.toString());
		}
		
		if (isFarJpCall(new BankAddress(addressToWriteAt), toGoToAddress))
		{			
			// To do a conditional far jp/call we need to do a JR before it
			if (conditions != InstructionConditions.NONE)
			{
				// Write a local JR to skip the farcall/jp
				writeIdx += writeJr(bytes, writeIdx, (byte) 4);
			}
			
			writeIdx += writeFarJpCall(bytes, writeIdx, toGoToAddress);
			return writeIdx - addressToWriteAt;
		}
		else
		{
			return writeJpCall(bytes, writeIdx, toGoToAddress);
		}
	}
	
	protected int writeJpCall(byte[] bytes, int indexToAddAt, BankAddress addressToGoTo)
	{		
		// always call
		if (InstructionConditions.NONE == conditions)
		{
			bytes[indexToAddAt] = conditionlessInstruct;
		}
		// Conditional call
		else
		{
			bytes[indexToAddAt] = (byte) (conditionedInstruct | (conditions.getValue() << 3)); 
		}
		
		// Now write the local address
		ByteUtils.writeAsShort(RomUtils.convertFromBankOffsetToLoadedOffset(addressToGoTo.bank, addressToGoTo.addressInBank), bytes, indexToAddAt + 1);
		return 3;
	}

	protected int writeFarJpCall(byte[] bytes, int indexToAddAt, BankAddress addressToGoTo)
	{		
		// This is an "RST" call (id 0xC7). These are special calls loaded into the ROM at the beginning. For
		// this ROM, RST5 (id 0x28) jumps to the "FarCall" function in the home.asm which handles
		// doing the call to any location in the ROM
		bytes[indexToAddAt] = (byte) (0xC7 | farInstuctRstVal); 
		
		// Now write the rest of the address
		bytes[indexToAddAt + 1] = addressToGoTo.bank;
		ByteUtils.writeAsShort(RomUtils.convertFromBankOffsetToLoadedOffset(addressToGoTo.bank, addressToGoTo.addressInBank), bytes, indexToAddAt + 2);
		return 4;
	}
	
	protected int writeJr(byte[] bytes, int indexToAddAt, byte relAddress)
	{
		writeJr(bytes, indexToAddAt, conditions, relAddress);
		return 2;
	}	
	
	public static void writeJr(byte[] bytes, int indexToAddAt, InstructionConditions conditions, byte relAddress)
	{
		if (InstructionConditions.NONE == conditions)
		{
			bytes[indexToAddAt] = 0x18;
		}
		else
		{
			bytes[indexToAddAt] = (byte) (0x20 | (conditions.getValue() << 3));
		}
		bytes[indexToAddAt + 1] = relAddress;
	}
}
