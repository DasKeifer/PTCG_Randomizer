package compiler.tmp;

import compiler.CompilerConstants;
import compiler.CompilerConstants.InstructionConditions;
import compiler.CompilerConstants.Register;
import compiler.CompilerConstants.RegisterPair;
import util.ByteUtils;

public class BasicInstructionCompiler 
{	
	// Make objects for each of these?
	// Then when we make the snippit, we save the objects and only write them when we finalize our output?
	
	// Flow control
	

	
	public static int writeJr(byte[] bytes, int indexToAddAt, byte relAddress)
	{
		return writeJr(bytes, indexToAddAt, relAddress, InstructionConditions.NONE);
	}
	
	public static int writeJr(byte[] bytes, int indexToAddAt, byte relAddress, InstructionConditions jumpConditions)
	{
		if (InstructionConditions.NONE == jumpConditions)
		{
			bytes[indexToAddAt] = 0x18;
		}
		else
		{
			bytes[indexToAddAt] = (byte) (0x20 | (jumpConditions.getValue() << 3));
		}
		bytes[indexToAddAt + 1] = relAddress;
		return indexToAddAt + JR_BYTE_LENGTH;
	}
	
	public static int writeJp(byte[] bytes, int indexToAddAt, short localAddress)
	{
		return writeJp(bytes, indexToAddAt, localAddress, InstructionConditions.NONE);
	}
	
	public static int writeJp(byte[] bytes, int indexToAddAt, short localAddress, InstructionConditions jumpConditions)
	{
		if (InstructionConditions.NONE == jumpConditions)
		{
			bytes[indexToAddAt] = (byte) 0xC3;
		}
		// Only jump if the conditions are met
		else
		{
			bytes[indexToAddAt] = (byte) (0xC2 | (jumpConditions.getValue() << 3));
		}
		ByteUtils.writeAsShort(localAddress, bytes, indexToAddAt + 1);
		return indexToAddAt + JP_BYTE_LENGTH;
	}
	
	public static int writeCall(byte[] bytes, int indexToAddAt, short localAddress)
	{
		return writeCall(bytes, indexToAddAt, localAddress, InstructionConditions.NONE);
	}
	
	public static int writeCall(byte[] bytes, int indexToAddAt, short localAddress, InstructionConditions callConditions)
	{
		// always call
		if (InstructionConditions.NONE == callConditions)
		{
			bytes[indexToAddAt] = (byte) 0xCD;
		}
		// Conditional call
		else
		{
			bytes[indexToAddAt] = (byte) (0xC4 | (callConditions.getValue() << 3)); 
		}
		
		// Now write the rest of the address
		ByteUtils.writeAsShort(localAddress, bytes, indexToAddAt + 1);
		return indexToAddAt + CALL_BYTE_LENGTH;
	}

	// TODO implement this
//	public static int writeBank1Call()
//	{
//		
//	}
	
	public static int writeFarcall(byte[] bytes, int indexToAddAt, byte bank, short address)
	{
		// This is an "RST" call (id 0xC7). These are special calls loaded into the ROM at the beginning. For
		// this ROM, RST5 (id 0x28) jumps to the "FarCall" function in the home.asm which handles
		// doing the call to any location in the ROM
		bytes[indexToAddAt] = (byte) 0xC7 | 0x28; 
		
		// Now write the rest of the address
		bytes[indexToAddAt + 1] = bank;
		ByteUtils.writeAsShort(address, bytes, indexToAddAt + 2);
		return indexToAddAt + FARCALL_BYTE_LENGTH;
	}
}
