package gameData;

import java.util.Set;

import rom.Texts;
import util.ByteUtils;
import util.TextUtils;

public abstract class Description 
{
	protected static final String NAME_PLACEHOLDER = "" + (char) 0x15; // NACK - just because
	String desc = "";
	
	protected String prepareDescForFormatting(String cardName)
	{
		// Make sure we remove the typechar from the name
		cardName = GameTextUtils.removeEnglishCharTypeCharIfPresent(cardName);

		// Put the card name back in then format it
		return desc.replaceAll(NAME_PLACEHOLDER, cardName);
	}

	protected void genericReadTextFromIds(byte[] bytes, int[] textIdIndexes, String cardName, Texts ptrToText, Set<Short> ptrsUsed)
	{
		desc = "";
		cardName = GameTextUtils.removeEnglishCharTypeCharIfPresent(cardName);

		for (int index : textIdIndexes)
		{
			short textPtr = ByteUtils.readAsShort(bytes, index);
			if (textPtr == 0)
			{
				break;
			}
			
			if (!desc.isEmpty())
			{
				desc += TextUtils.BLOCK_BREAK;
			}
			
			desc += ptrToText.getAtId(textPtr);
			ptrsUsed.add(textPtr);
		}
		
		desc = removeCharTypesNewLinesAndInsertNamePlacholder(desc, cardName);
	}
	
	private String removeCharTypesNewLinesAndInsertNamePlacholder(String description, String cardName)
	{
		String descUnformatted = description.replaceAll("\n", " ");
		descUnformatted = descUnformatted.replaceAll(cardName, NAME_PLACEHOLDER);
		return GameTextUtils.removeAllEnglishCharTypeChars(descUnformatted);
	}
	
	protected void genericConvertToIdsAndWriteText(byte[] bytes, int[] textIdIndexes, String descForSaving, Texts ptrToText)
	{
		String[] blocks = descForSaving.split(TextUtils.BLOCK_BREAK);
		int expectedBlocks = textIdIndexes.length;
		int blockIndex = 0;
		
		if (!descForSaving.isEmpty())
		{
			if (blocks.length > expectedBlocks)
			{
				throw new IllegalArgumentException("Too many text blocks passed in! Expected " + 
						expectedBlocks + " got " + blocks.length + ". First block is \"" + blocks[0] + "\"");
			}
			
			// Write each block
			for (String block : blocks)
			{
				block = GameTextUtils.addEnglishCharTypeCharIfNotSet(block);
				if (block == null || block.isEmpty())
				{
					ByteUtils.writeAsShort((short)0, bytes, textIdIndexes[blockIndex]);
				}
				else
				{
					short id = ptrToText.insertTextOrGetId(block);
					ByteUtils.writeAsShort(id, bytes, textIdIndexes[blockIndex]);
				}
				blockIndex++;
			}
		}
		
		// In case only one block was passed when we need to write two or something
		// to that effect
		if (blockIndex < expectedBlocks)
		{
			for (;blockIndex < expectedBlocks; blockIndex++)
			{
				ByteUtils.writeAsShort((short)0, bytes, textIdIndexes[blockIndex]);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return desc;
	}
}
