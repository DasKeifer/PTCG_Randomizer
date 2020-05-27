package gameData;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.TextUtils;

public class EffectDescription extends Description
{	
	private static final int NUM_POINTERS_IN_FILE = 2;

	public void readTextFromIds(byte[] bytes, int[] textIdIndexes, String cardName, Texts ptrToText, Set<Short> ptrsUsed)
	{
		if (textIdIndexes.length != NUM_POINTERS_IN_FILE)
		{
			throw new IllegalArgumentException("Reading effect description was passed the wrong number of id indexes :" + 
					textIdIndexes.length);
		}
		genericReadTextFromIds(bytes, textIdIndexes, cardName, ptrToText, ptrsUsed);
	}

	public void convertToIdsAndWriteText(byte[] bytes, int[] textIdIndexes, String cardName, Texts ptrToText)
	{
		if (textIdIndexes.length != NUM_POINTERS_IN_FILE)
		{
			throw new IllegalArgumentException("Writing effect description was passed the wrong number of id indexes :" + 
					textIdIndexes.length);
		}
		String descToWrite = prepareDescForFormatting(cardName);
		descToWrite = formatDescription(descToWrite);
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, descToWrite, ptrToText);
	}
	
	private String formatDescription(String descExpanded)
	{
		String formatted = "";
		String tempText;
		boolean failedFormatting = false;
		boolean hasLineBreak = descExpanded.contains(TextUtils.BLOCK_BREAK);
		
		if (hasLineBreak)
		{		
			// First try to preserve any block breaks
			String[] blocks = descExpanded.split(TextUtils.BLOCK_BREAK);
			for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++)
			{
				tempText = TextUtils.prettyFormatText(blocks[blockIndex],
						RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC);
				if (tempText != null)
				{
					formatted += tempText;
					if (blockIndex < blocks.length - 1)
					{
						formatted += (char)0x0C;
					}
				}
				else
				{
					System.out.println("Existing block breaks could not be used - reformatting. " +
							"Block that was too large: \"" + blocks[blockIndex] + "\"");
					formatted = "";
					failedFormatting = true;
					break;
				}
			}
		}
		
		if (!hasLineBreak || failedFormatting)
		{
			// Next try making our own blocks
			tempText = descExpanded.replace(TextUtils.BLOCK_BREAK, " ");
			formatted = TextUtils.prettyFormatText(tempText,
					RomConstants.MAX_CHARS_PER_LINE, RomConstants.PREFERRED_LINES_PER_EFFECT_DESC,
					RomConstants.MAX_LINES_PER_EFFECT_DESC, NUM_POINTERS_IN_FILE);
			
			if (formatted == null)
			{
				System.out.println("Could not nicely fit effect text over line breaks - attempting to " +
						"split words over lines to get it to fit for \"" + tempText + "\"");
				
				// If all else fails, just pack as tight as possible
				formatted = TextUtils.packFormatText(tempText,
					RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC,
					NUM_POINTERS_IN_FILE);
			}
		}
		
		return formatted;
	}
}
