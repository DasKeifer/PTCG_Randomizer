package gameData;

import java.util.Set;

import rom.Cards;
import rom.Texts;
import util.ByteUtils;

public abstract class Description 
{
	public static final String BLOCK_BREAK = "" + (char) 0x0C;
	protected static final String NAME_PLACEHOLDER = "" + (char) 0x15; // NACK - just because
	String desc = "";

	public abstract int getNumPtrInFile();
	
	public int getPtrsSizeInFile()
	{
		return getNumPtrInFile() * 2;
	}
	
	protected String prepareDescForFormatting(String cardName)
	{
		// Make sure we remove the typechar from the name
		cardName = Cards.removeEnglishCharTypeCharIfPresent(cardName);

		// Put the card name back in then format it
		return desc.replaceAll(NAME_PLACEHOLDER, cardName);
	}

	protected void genericReadTextFromIds(byte[] bytes, int[] textIdIndexes, String cardName, Texts ptrToText, Set<Short> ptrsUsed)
	{
		desc = "";
		cardName = Cards.removeEnglishCharTypeCharIfPresent(cardName);

		for (int index : textIdIndexes)
		{
			short textPtr = ByteUtils.readAsShort(bytes, index);
			if (textPtr == 0)
			{
				break;
			}
			
			if (!desc.isEmpty())
			{
				desc += BLOCK_BREAK;
			}
			
			desc += ptrToText.getAtId(textPtr);
			ptrsUsed.add(textPtr);
		}
		
		desc = removeNewLinesAndInsertNamePlacholder(desc, cardName);
	}
	
	private String removeNewLinesAndInsertNamePlacholder(String description, String cardName)
	{
		String descUnformatted = description.replaceAll("\n", " ");
		descUnformatted = descUnformatted.replaceAll(cardName, NAME_PLACEHOLDER);
		return descUnformatted;
	}
	
	protected void genericConvertToIdsAndWriteText(byte[] bytes, int[] textIdIndexes, String descForSaving, Texts ptrToText)
	{
		String[] blocks = descForSaving.split(BLOCK_BREAK);
		int expectedBlocks = textIdIndexes.length;
		
		if (blocks.length > expectedBlocks)
		{
			throw new IllegalArgumentException("Too many text blocks passed in! Expected " + 
					expectedBlocks + " got " + blocks.length + ". First block is \"" + blocks[0] + "\"");
		}
		
		// Write each block
		int blockIndex = 0;
		for (String block : blocks)
		{
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
