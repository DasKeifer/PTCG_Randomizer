package compiler;

import java.util.List;

import compiler.dynamic.CallJumpCompiler;
import compiler.dynamic.DynamicCompiler;
import compiler.dynamic.RawBytesCompiler;
import compiler.dynamic.TextPtrCompiler;

// Helpful links:
//		https://github.com/gbdev/rgbds/blob/master/src/asm/parser.y
//		https://rgbds.gbdev.io/docs/master/gbz80.7
//		https://gb-archive.github.io/salvage/decoding_gbz80_opcodes/Decoding%20Gamboy%20Z80%20Opcodes.html
public class CodeSnippit 
{
		// TODO: Maybe make a smart class that will hold onto the data and only assign it as saving based on total space avail and what not
		// The class would rejigger and farcall as needed based on available space. That sounds pretty snazzy - lets give it a whirl
		//
		// Thoughts on this:
		// We would need to hold onto pointers for things like text and calls.
		// Calls we would need special logic to determine if we can do them locally or need a farcall
		// We also need some concept of non-bank swappable pointers for the effect commands since those assume a bank and are
		// only given addresses relative to that bank. The addressed location could presumably then farcall something else
		// Text would just be a matter of putting the right value int so that would be easier
		// So we have: Call/jp, ldtx, and bankSpecificPointers
		// Then with that data I think we can rejigger things
		// Effect command would be byte, bankSpecificPointer(B, effectFunctionId), byte, bankSpecificPointer(B, effectFunctionId), ..., byte
		// Effect function would then be byte[], call(bank, address), byte[], etc.
		// 		if it was relocated, the function would just be a farcall to the new loc then a ret
		
		// Function - ID, CallLocation(reqBank, address)[], trueBank, trueAddr, data<bytesOrCalls>
		// Blob - ID, requiredBank, address, data<bytesOrCalls>
		// Text probably doesn't need to be here - it can be part of defining the data/Is handled separately but then we need to remove the text space from here...
		// Hmmm gets tricky
		
		// Process - do all the texts first. Read in all the text, label all that space as free, but as we add text, it gets used up. This probably isn't too much
		// of a change from how we do it now.
		// Then do the other stuff - check if we need to move things around, etc.
		//Map<String, Entry> dataToPlace;
		

	// TODO: Implement this
//	private byte requiredBank;
//	private boolean canRelocateAndJumpTo;
//	private Map<Byte, Byte> bankPriorities;
//	private Set<Byte> excludedBanks;
	// static ID counter?
	
	
	private byte preferredBank;
	private boolean isRequiredToBeInBank;
	private List<DynamicCompiler> data;
	
	public CodeSnippit(byte bank, boolean isBankRequired)
	{
		preferredBank = bank;
		isRequiredToBeInBank = isBankRequired;
	}
	
	// TODO make generic?
	
	public void addTextPtr(short textId)
	{
		data.add(new TextPtrCompiler(textId));
	}
	
	public void addBytes(byte[] bytes)
	{			
		data.add(new RawBytesCompiler(bytes));
	}
	
	public void addCall(byte bankToCall, short addressToCall, boolean isCallNotJp)
	{
//		data.add(new CallJumpCompiler(bankToCall, addressToCall, isCallNotJp));
	}
	
	public byte getPreferredBank()
	{
		return preferredBank;
	}
	
	public boolean isRequiredToBeInPreferredBank() 
	{
		return isRequiredToBeInBank;
	}
	
	public int getSizeOnPreferredBank()
	{
		return getSizeOnBank(preferredBank);
	}
	
	public int getSizeOnBank(byte bank)
	{
		int size = 0;
		for (DynamicCompiler item : data)
		{
			size += item.getSizeOnBank(bank);
		}
		return size;
	}
	
	public int getMaxSizeOnBank(byte bank)
	{
		// TODO:
		return 0;
	}
	
	// Change to write bytes in bank
	public byte[] getBytesForBank(char bank)
	{
		// Work on this more
		return null;
	}
}
