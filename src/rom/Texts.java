package rom;

import java.util.HashMap;
import java.util.Map;

import compiler.DataBlock;
import compiler.dynamic.BlockAddress;
import compiler.dynamic.RawBytes;
import constants.RomConstants;
import datamanager.BankPreference;
import datamanager.FixedBlock;
import datamanager.FloatingBlock;

public class Texts 
{
	// TODO: Add text labels here? Then we can treat everything as blocks?
	// Or maybe just assume names and create a special class for/funct for
	// getting the textLabel based on Id?
	private Map<Short, String> textMap;
	private Map<String, Short> reverseMap;

	public Texts()
	{
		textMap = new HashMap<>();
		reverseMap = new HashMap<>();
		
		// Put in the "null pointer" reservation at ID 0
		textMap.put((short) 0, "");
		reverseMap.put("", (short) 0);
	}
	
	public Texts(Texts toCopy)
	{
		textMap = new HashMap<>(toCopy.textMap);
		reverseMap = new HashMap<>(toCopy.reverseMap);
	}
	
	public short insertTextAtNextId(String text)
	{
		short nextId = count();
		textMap.put(nextId, text);
		reverseMap.put(text, nextId);
		return nextId;
	}
	
	public short getId(String text)
	{
		Short id = reverseMap.get(text);
		if (id == null)
		{
			return 0;
		}
		return id;
	}
	
	public short insertTextOrGetId(String text)
	{
		Short id = reverseMap.get(text);
		if (id == null)
		{
			// This takes care of placing in both maps
			id = insertTextAtNextId(text);
		}
		return id;
	}
	
	public String getAtId(short id)
	{
		return textMap.get(id);
	}
	
	public void putAtId(short id, String text)
	{
		textMap.put(id, text);
		reverseMap.put(text, id);
	}
	
	public short count()
	{
		return (short) textMap.size();
	}
	
	public void convertAndAddBlocks(Blocks blocks)
	{
		// Write a null pointer to start because thats how it was in the original rom
		DataBlock textPtrs = new DataBlock("internal_textPointers", new BlockAddress(0, RomConstants.TEXT_POINTER_SIZE_IN_BYTES, 0));
		
		// Create the rest of the text blocks and pointers
		// We intentionally do it like this to ensure there are no gaps which would otherwise
		// cause issues
		String nullTextLabel = "";
		byte[] textTerminator = new byte[] {0};
		int usedCount = 1; // Because we wrote the null pointer already
		short textId = 1;
		for (; usedCount < count(); textId++)
		{	
			// If we don't have the key, link to a null text
			if (!textMap.containsKey(textId))
			{
				// Create the null text if this is the first time needing it
				if (nullTextLabel.isEmpty())
				{
					nullTextLabel = "internal_romTextNull";
					DataBlock nullText = new DataBlock(nullTextLabel, new RawBytes("NULL TEXT".getBytes(), textTerminator));
					blocks.addMoveableBlock(new FloatingBlock((byte) 2, nullText, new BankPreference((byte)1, (byte)0xd, (byte)0x1c)));
				}
				
				textPtrs.appendInstruction(new BlockAddress(nullTextLabel, RomConstants.TEXT_POINTER_SIZE_IN_BYTES, RomConstants.TEXT_POINTER_OFFSET));
				continue;
			}

			// Otherwise we have the key - add the text
			String textLabel = "internal_romText" + textId;
			byte[] stringBytes = getAtId(textId).getBytes();
			textPtrs.appendInstruction(new BlockAddress(textLabel, RomConstants.TEXT_POINTER_SIZE_IN_BYTES, RomConstants.TEXT_POINTER_OFFSET));
			
			// Create the data block from the string bytes and add the trailing null and then add the block for it
			DataBlock text = new DataBlock(textLabel, new RawBytes(stringBytes, textTerminator));
			blocks.addMoveableBlock(new FloatingBlock((byte) 2, text, new BankPreference((byte)1, (byte)0xd, (byte)0x1c)));
			usedCount++;
		}

		blocks.addFixedBlock(new FixedBlock(RomConstants.TEXT_POINTERS_LOC, textPtrs, (textId - 1) * RomConstants.TEXT_POINTER_SIZE_IN_BYTES));  
	}
}
