package rom;

import java.util.HashMap;
import java.util.Map;

import compiler.reference_instructs.BlockGlobalAddress;
import compiler.static_instructs.RawBytes;
import constants.PtcgRomConstants;
import gbc_rom_packer.MoveableBlock;
import gbc_rom_packer.ReplacementBlock;

public class Texts 
{
	// TODO later: Add text labels here? Then we can treat everything as blocks?
	// Or maybe just assume names and create a special class for/funct for
	// getting the textLabel based on Id?
	// TODO later: Add some snazzy logic to leave most texts in place and only move ones that
	// would be overwritten by adding more pointers
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
		ReplacementBlock textPtrs = new ReplacementBlock("internal_textPointers", PtcgRomConstants.TEXT_POINTERS_LOC);
		blocks.addFixedBlock(textPtrs);
		textPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0, (byte) 0));
		
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
					// TODO later: determine/set actual range - needs to at least be larger than the text pointer offset
					MoveableBlock nullText = new MoveableBlock(nullTextLabel, 1, (byte)0xd, (byte)0x1c);
					blocks.addMoveableBlock(nullText);
					nullText.appendInstruction(new RawBytes("NULL TEXT".getBytes(), textTerminator));
				}
				
				textPtrs.appendInstruction(new BlockGlobalAddress(nullTextLabel, PtcgRomConstants.TEXT_POINTER_OFFSET));
				continue;
			}

			// Otherwise we have the key - add the text
			String textLabel = "internal_romText" + textId;
			byte[] stringBytes = getAtId(textId).getBytes();
			textPtrs.appendInstruction(new BlockGlobalAddress(textLabel, PtcgRomConstants.TEXT_POINTER_OFFSET));
			
			// Create the data block from the string bytes and add the trailing null and then add the block for it
			// TODO later: determine/set actual range - needs to at least be larger than the text pointer offset
			MoveableBlock text = new MoveableBlock(textLabel, 1, (byte)0xd, (byte)0x1c);
			blocks.addMoveableBlock(text);
			text.appendInstruction(new RawBytes(stringBytes, textTerminator));
			usedCount++;
		}

		// Not -1 because we write the 0th id "000000" pointer
		// TODO later: Make this or something overwrite any partial text that may have been leftover
		textPtrs.setReplaceLength(textId * PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES);  
	}
}
