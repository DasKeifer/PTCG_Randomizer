package gameData;

import java.util.Set;

import constants.RomConstants;
import rom.Cards;
import rom.Texts;
import util.ByteUtils;

public class Description 
{
	private static final String namePlaceholder = "" + (char) 0x15; // NACK - just because
	
	String desc = "";
	boolean isEffectDesc;
	
	public int readTextFromIds(byte[] bytes, int startIndex, String cardName, boolean inIsEffectDesc, Texts ptrToText, Set<Short> ptrsUsed)
	{		
		isEffectDesc = inIsEffectDesc;
		cardName = Cards.removeEnglishCharTypeCharIfNotSet(cardName);
		
		short textPtr = ByteUtils.readAsShort(bytes, startIndex);
		if (textPtr == 0)
		{
			desc = "";
			if (isEffectDesc)
			{
				return startIndex + 4;
			}
			return startIndex + 2;
		}
		
		// Read and store formatted. When we go to save we will make sure its in a good format
		desc = ptrToText.getAtId(textPtr);
		ptrsUsed.add(textPtr);
		startIndex += 2;
		
		if (isEffectDesc)
		{
			short textExtendedPtr = ByteUtils.readAsShort(bytes, startIndex);
			if (textExtendedPtr != 0)
			{
				desc += (char) 0x0C + ptrToText.getAtId(textExtendedPtr);
				ptrsUsed.add(textExtendedPtr);
			}
			startIndex += 2;
		}
		
		desc = desc.replaceAll(cardName, namePlaceholder);
		
		return startIndex;
	}

	public int convertToIdsAndWriteText(byte[] bytes, int startIndex, String cardName, Texts ptrToText) 
	{
		cardName = Cards.removeEnglishCharTypeCharIfNotSet(cardName);
		
		// Create a temp string with the name inserted in
		String descForSaving = desc.replaceAll(namePlaceholder, cardName);
		
		boolean needsReformatting = false;
		
		String[] blocks = descForSaving.split((char)0x0C + "");
		if (isEffectDesc && blocks.length > 2)
		{
			System.out.println("Too many page breaks (" + blocks.length + ") in effect description! Reformatting!");
			needsReformatting = true;
		}
		else if (!isEffectDesc && blocks.length > 1)
		{
			System.out.println("Too many page breaks (" + blocks.length + ") in card text! Reformatting!");
			needsReformatting = true;
		}
		
		if (!needsReformatting)
		{
			int totalLines = 0;
			String[] lines;
			for (String block : blocks)
			{
				lines = block.split("\n");
				for (String line : lines)
				{
					if (lines.length > RomConstants.MAX_CHARS_PER_LINE)
					{
						System.out.println("Too many characters (" + line.length() + ") in card text! Reformatting!");
						needsReformatting = true;
						break;
					}
				}
				if (needsReformatting)
				{
					break;
				}
			}

			if (!needsReformatting && isEffectDesc && totalLines > RomConstants.MAX_LINES_PER_EFFECT_DESC)
			{
				System.out.println("Too many lines (" + totalLines + ") in card text! Reformatting!");
				needsReformatting = true;
			}
			else if (!needsReformatting && !isEffectDesc && totalLines > RomConstants.MAX_LINES_PER_POKE_DESC)
			{
				System.out.println("Too many lines (" + totalLines + ") in effect description! Reformatting!");
				needsReformatting = true;
			}
		}

		if (!needsReformatting)
		{
			return writeFormattedDesc(bytes, startIndex, blocks, ptrToText);
		}
		else
		{
			// TODO format at return
			throw new IllegalArgumentException("NOT IMPLEMENTED");
		}
	}
	
	private int writeFormattedDesc(byte[] bytes, int startIndex, String[] blocks, Texts ptrToText)
	{
		// Write first block
		short id = ptrToText.insertTextOrGetId(blocks[0]);
		ByteUtils.writeAsShort(id, bytes, startIndex);
		startIndex += 2;
		
		if (isEffectDesc)
		{
			//write second block if it exists or else
			if (blocks.length > 1 && blocks[1] != null && !blocks[1].isEmpty())
			{
				id = ptrToText.insertTextOrGetId(blocks[0]);
				ByteUtils.writeAsShort(id, bytes, startIndex);
			}
			else
			{
				ByteUtils.writeAsShort((short) 0, bytes, startIndex);
			}
			startIndex += 2;
		}
		return startIndex;
	}
	
	@Override
	public String toString()
	{
		return desc;
	}
}
