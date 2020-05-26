package gameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferencesFactory;

import constants.RomConstants;
import rom.Cards;
import rom.Texts;
import util.ByteUtils;
import util.TextUtils;

public class EffectDescription extends Description
{	
	private static final int NUM_POINTERS_IN_FILE = 2;

	@Override
	public String readTextFromIdsClassSpecific(byte[] bytes, int startIndex, String cardName, Texts ptrToText, Set<Short> ptrsUsed)
	{		
		// If its null, no need to continue
		short textPtr = ByteUtils.readAsShort(bytes, startIndex);
		if (textPtr == 0)
		{
			return "";
		}
		
		// Read and store formatted. When we go to save we will make sure its in a good format
		String tempDesc = ptrToText.getAtId(textPtr);
		ptrsUsed.add(textPtr);
		startIndex += 2;
		
		short textExtendedPtr = ByteUtils.readAsShort(bytes, startIndex);
		if (textExtendedPtr != 0)
		{
			tempDesc += BLOCK_BREAK + ptrToText.getAtId(textExtendedPtr);
			ptrsUsed.add(textExtendedPtr);
		}
		startIndex += 2;
		
		return tempDesc;
	}

	@Override
	public void convertToIdsAndWriteTextClassSpecific(byte[] bytes, int startIndex, String descForSaving, Texts ptrToText) 
	{			
		boolean needsReformatting = false;
		
		String[] blocks = descForSaving.split(BLOCK_BREAK);
		if (blocks.length > 2)
		{
			System.out.println("Too many page breaks (" + blocks.length + ") in effect description! Reformatting!");
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
					line = Cards.removeEnglishCharTypeCharIfPresent(line);
					if (line.length() > RomConstants.MAX_CHARS_PER_LINE)
					{
						System.out.println("Too many characters (" + line.length() + ") in card text \"" + line + "\" - Reformatting!");
						needsReformatting = true;
						break;
					}
				}
				if (needsReformatting)
				{
					break;
				}
			}

			if (!needsReformatting && totalLines > RomConstants.MAX_LINES_PER_EFFECT_DESC)
			{
				System.out.println("Too many lines (" + totalLines + ") in effect description! Reformatting!");
				needsReformatting = true;
			}
		}

		if (!needsReformatting)
		{
			writeFormattedDesc(bytes, startIndex, blocks, ptrToText);
		}
		else
		{
			// TODO format at return
			throw new IllegalArgumentException("NOT IMPLEMENTED");
		}
	}
	
	private String formatDescription(String cardName)
	{
		String formatted = "";
		
		// Put the card name back in
		String descExpanded = desc.replaceAll(NAME_PLACEHOLDER, cardName);

		// First try to preserve any block breaks
		String[] blocks = desc.split(BLOCK_BREAK);
		boolean failedFormatting = false;
		String formattedBlock;
		for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++)
		{
			formattedBlock = TextUtils.prettyFormatText(blocks[blockIndex], RomConstants.MAX_CHARS_PER_LINE, );
			if (formattedBlock != null)
			{
				formatted += formattedBlock;
				if (blockIndex < blocks.length - 1)
				{
					formatted += (char)0x0C;
				}
				else
				{
					failedFormatting = true;
				}
			}
		}
		
		if (failedFormatting)
		{
			// Next try making our own blocks
			
			// If all else fails, just pack as tight as possible
			return null;
		}
		return formatted;
	}

	@Override
	public int getNumPtrInFile() 
	{
		return NUM_POINTERS_IN_FILE;
	}
}
