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
