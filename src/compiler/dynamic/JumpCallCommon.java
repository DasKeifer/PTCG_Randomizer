package compiler.dynamic;


import compiler.CompilerConstants.InstructionConditions;
import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.Segment;
import util.ByteUtils;
import util.RomUtils;

public abstract class JumpCallCommon extends Instruction
{
	InstructionConditions conditions;
	String labelToGoTo;
	protected Segment toGoTo;
	int addressToGoTo;
	
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
		
		toGoTo = null;
		addressToGoTo = CompilerUtils.UNASSIGNED_ADDRESS;
	}
	
	protected JumpCallCommon(int addressToGoTo, InstructionConditions conditions, byte conditionlessInstruct, byte conditionedInstruct, byte farInstuctRstVal)
	{
		this.conditions = conditions;
		labelToGoTo = "";
		this.conditionlessInstruct = conditionlessInstruct;
		this.conditionedInstruct = conditionedInstruct;
		this.farInstuctRstVal = farInstuctRstVal;
		
		toGoTo = null;
		this.addressToGoTo = addressToGoTo;
	}
	
	public static JumpCallCommon create(String[] args, String rootSegment, boolean isJp)
	{	
		final String supportedArgs = " only supports (int gloabalAddressToGoTo), (String labelToGoTo), (InstructionCondition, int gloabalAddressToGoTo) and (InstructionCondition, String labelToGoTo): ";	
		String callOrJpString = "jp/farjp";
		if (isJp)
		{
			callOrJpString = "call/farcall";
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
			throw new IllegalArgumentException(callOrJpString + supportedArgs + "given " + args.toString());
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
				return new Jump(CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment), conditions);
			}
			else
			{
				return new Call(CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment), conditions);
			}
		}
	}
	
	protected int getAddressToGoTo()
	{
		int address = addressToGoTo;
		if (addressToGoTo == CompilerUtils.UNASSIGNED_ADDRESS && toGoTo != null)
		{
			address = toGoTo.getAddress();
		}
		return address;
	}

	@Override
	public int getWorstCaseSizeOnBank(byte bank, int instOffset)
	{
		if (isFarJpCall(bank))
		{
			// If its not assigned, assume the worst
			return getFarJpCallSize();
		}
		
		// local jp/call
		return 3;
	}
	
	protected boolean isFarJpCall(byte bank)
	{
		int address = getAddressToGoTo();
		if (address == CompilerUtils.UNASSIGNED_LOCAL_ADDRESS)
		{
			return false;
		}
		
		if (address != CompilerUtils.UNASSIGNED_ADDRESS &&
				RomUtils.determineBank(address) == bank)
		{
			return false;
		}
		
		return true;
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
	public int writeBytes(byte[] bytes, int blockStartIdx, int writeOffset) 
	{
		int writeIdx = blockStartIdx + writeOffset;
		
		if (isFarJpCall(RomUtils.determineBank(blockStartIdx)))
		{
			int instWriteOffset = 0;
			
			// To do a conditional far jp/call we need to do a JR before it
			if (conditions != InstructionConditions.NONE)
			{
				// Write a local JR to skip the farcall/jp
				instWriteOffset += writeJr(bytes, writeIdx, (byte) 4);
			}
			
			instWriteOffset += writeFarJpCall(bytes, writeIdx + instWriteOffset);
			return instWriteOffset;
		}
		else
		{
			return writeJpCall(bytes, writeIdx);
		}
	}
	
	protected int writeJpCall(byte[] bytes, int indexToAddAt)
	{
		int callAddress = getAddressToGoTo();
		
		// always call
		if (InstructionConditions.NONE == conditions)
		{
			bytes[indexToAddAt] = conditionlessInstruct;
		}
		// Conditional call
		else
		{
			bytes[indexToAddAt] = conditionedInstruct; 
		}
		
		// Now write the local address
		ByteUtils.writeAsShort(RomUtils.convertToLoadedBankOffset(callAddress), bytes, indexToAddAt + 1);
		return 3;
	}

	protected int writeFarJpCall(byte[] bytes, int indexToAddAt)
	{
		int callAddress = getAddressToGoTo();
		
		// This is an "RST" call (id 0xC7). These are special calls loaded into the ROM at the beginning. For
		// this ROM, RST5 (id 0x28) jumps to the "FarCall" function in the home.asm which handles
		// doing the call to any location in the ROM
		bytes[indexToAddAt] = (byte) (0xC7 | farInstuctRstVal); 
		
		byte bank = RomUtils.determineBank(callAddress);
		short localAddress = RomUtils.convertToLoadedBankOffset(bank, callAddress);
		
		// Now write the rest of the address
		bytes[indexToAddAt + 1] = bank;
		ByteUtils.writeAsShort(localAddress, bytes, indexToAddAt + 2);
		return 4;
	}
	
	protected int writeJr(byte[] bytes, int indexToAddAt, byte relAddress)
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
		return 2;
	}	
}
