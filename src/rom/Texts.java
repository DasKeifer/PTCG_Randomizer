package rom;

import java.util.HashMap;
import java.util.Map;

import compiler.CodeBlock;
import compiler.reference_instructs.BlockGlobalAddress;
import compiler.static_instructs.RawBytes;
import constants.CharMapConstants;
import constants.PtcgRomConstants;
import rom_packer.Blocks;
import rom_packer.HybridBlock;
import rom_packer.MovableBlock;
import rom_packer.ReplacementBlock;

public class Texts 
{
	// TODO later: Add text labels here? Then we can treat everything as blocks?
	// Or maybe just assume names and create a special class for/funct for
	// getting the textLabel based on Id?
	private Map<Short, String> textMap;
	private Map<String, Short> reverseMap;
	private Map<Short, Integer> idToAddressMap;

	public Texts()
	{
		textMap = new HashMap<>();
		reverseMap = new HashMap<>();
		idToAddressMap = new HashMap<>();
		
		// Put in the "null pointer" reservation at ID 0
		textMap.put((short) 0, "");
		reverseMap.put("", (short) 0);
	}
	
	public Texts(Texts toCopy)
	{
		textMap = new HashMap<>(toCopy.textMap);
		reverseMap = new HashMap<>(toCopy.reverseMap);
		idToAddressMap = new HashMap<>(toCopy.idToAddressMap);
	}

	public short insertTextAtNextId(String text)
	{
		return insertTextAtNextId(text, -1);
	}
	
	public short insertTextAtNextId(String text, int defaultAddress)
	{
		short nextId = count();
		textMap.put(nextId, text);
		reverseMap.put(text, nextId);
		if (defaultAddress >= 0)
		{
			idToAddressMap.put(nextId, defaultAddress);
		}
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
	
	// TODO: Not correctly adding new blocks -- see call for family period issue
	public void convertAndAddBlocks(Blocks blocks)
	{
		// Write a null pointer to start because thats how it was in the original rom
		CodeBlock textPtrs = new CodeBlock("internal_textPointers");
		textPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0, (byte) 0));
		
		// Create the rest of the text blocks and pointers
		// We intentionally do it like this to ensure there are no gaps which would otherwise
		// cause issues
		String nullTextLabel = "";
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
					createAndAddTextBlock(textId, nullTextLabel, blocks);
				}
				
				textPtrs.appendInstruction(new BlockGlobalAddress(nullTextLabel, PtcgRomConstants.TEXT_POINTER_OFFSET));
				continue;
			}

			// Otherwise we have the key - add the text
			String textLabel = "internal_romText" + textId;
			textPtrs.appendInstruction(new BlockGlobalAddress(textLabel, PtcgRomConstants.TEXT_POINTER_OFFSET));
			
			// Create and add the text as appropriate (i.e. a hybrid if it was read from the rom or a movable
			// if its a new block)
			createAndAddTextBlock(textId, textLabel, blocks);
			usedCount++;
		}

		// Create the fixed block. Since its all fixed size, we can just pass the length of the block
		blocks.addFixedBlock(new ReplacementBlock(textPtrs, PtcgRomConstants.TEXT_POINTERS_LOC, textPtrs.getWorstCaseSize()));
	}
	
	private void createAndAddTextBlock(short textId, String textLabel, Blocks blocks)
	{
		byte[] stringBytes = getAtId(textId).getBytes();
		
		CodeBlock text = new CodeBlock(textLabel);
		text.appendInstruction(new RawBytes(stringBytes));
		text.appendInstruction(new RawBytes((byte) CharMapConstants.TEXT_END_CHAR));
		MovableBlock block = new MovableBlock(text, 1, (byte)0xd, (byte)0x1c);
		
		int origAddress = idToAddressMap.getOrDefault(textId, -1); // TODO: Minor Const?
		if (origAddress >= 0)
		{
			blocks.addHybridBlock(new HybridBlock(block, origAddress));	
		}
		else
		{
			blocks.addMovableBlock(block);
		}
	}
}
