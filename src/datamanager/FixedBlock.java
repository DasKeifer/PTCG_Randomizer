package datamanager;

import java.util.Map;

import compiler.CodeSnippit;
import compiler.dynamic.Block;
import datamanager.ConstrainedBlock.AutoCompressOption;
import util.ByteUtils;

public abstract class FixedBlock
{
	int addressStartToReplace;
	int replaceLength; // needed since we want to pad space... or just use replaced bytes?
	Block replaceWith;
	byte[] verifyPreBytes;
	byte[] verifyReplacedBytes;
	byte[] verifyPostBytes;
	
	// TODO: Integrity checking surrounding area and replace
	
	// Write a block of up to the specified size
	// Auto fill with nop (0x00) after
	public FixedBlock(int startAddress, CodeSnippit replaceWith, byte[] bytesToReplace)
	{
		addressStartToReplace = startAddress;
		// TODO copy? this.replaceWith = new CodeSnippit(replaceWith);
		//this.dataToReplace = bytesToReplace.clone();
	}
	
	public FixedBlock(int startAddress, CodeSnippit replaceWith)
	{
		// Assume 0xFF
	}
	
	// Write a relocate block (3 or 4 bytes long)
	// Auto fill with nop (0x00) after
	public FixedBlock(
			String genSnippetId, // Needed?
			int startAddress, 
			String callToBlockId, 
			AutoCompressOption compressOption, 
			boolean mustBeLocal, 
			byte[] bytesToReplace
	)
	{
		addressStartToReplace = startAddress;
		// TODO: generate this.replaceWith = new CodeSnippit(replaceWith);
		//this.dataToReplace = bytesToReplace.clone();
	}
	
	public FixedBlock(int startAddress, AutoCompressOption compressOption, boolean mustBeLocal)
	{
		// Assume 0xFF
	}
	
	public int writeData(byte[] bytes, Map<String, Integer> blockIdsToAddresses)
	{
		Integer address = blockIdsToAddresses.get(replaceWith.getId());
		// Check null?
		
		// TODO: throw or warn?
		if (ByteUtils.compareBytes(bytes, address - verifyPreBytes.length, verifyPreBytes))
		{
			throw new RuntimeException("Pre check failed!");
		}
		else if (ByteUtils.compareBytes(bytes, address + replaceLength, verifyPostBytes))
		{
			throw new RuntimeException("Post check failed!");
		}
		// TODO: do more logic to see if the change was already made? Maybe just warn instead
		// and replace anyways. Greater question for all of these.
		else if (ByteUtils.compareBytes(bytes, address, verifyReplacedBytes))
		{
			throw new RuntimeException("replacement bytes check failed!");
		}
		
		return replaceWith.writeData(bytes, blockIdsToAddresses);
	}
}
