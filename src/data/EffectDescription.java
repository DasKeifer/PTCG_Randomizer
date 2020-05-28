package data;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.StringUtils;

public class EffectDescription extends RomText
{	
	private static final String NAME_PLACEHOLDER = "" + (char) 0x15; // NACK - just because
	private static final int NUM_POINTERS_IN_FILE = 2;

	public EffectDescription()
	{
		super();
	}
	
	public EffectDescription(EffectDescription toCopy)
	{
		super(toCopy);
	}

	public void readTextFromIds(byte[] bytes, int[] textIdIndexes, RomText cardName, Texts ptrToText, Set<Short> ptrsUsed)
	{
		if (textIdIndexes.length != NUM_POINTERS_IN_FILE)
		{
			throw new IllegalArgumentException("Reading effect description was passed the wrong number of id indexes :" + 
					textIdIndexes.length);
		}
		genericReadTextFromIds(bytes, textIdIndexes, ptrToText, ptrsUsed);
		replaceAll(cardName.getText(), NAME_PLACEHOLDER);
	}

	public void convertToIdsAndWriteText(byte[] bytes, int[] textIdIndexes, RomText cardName, Texts ptrToText)
	{
		if (textIdIndexes.length != NUM_POINTERS_IN_FILE)
		{
			throw new IllegalArgumentException("Writing effect description was passed the wrong number of id indexes :" + 
					textIdIndexes.length);
		}
		replaceAll(NAME_PLACEHOLDER, cardName.getText());
		setTextPreservingNewlines(formatDescription(getText()));
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, ptrToText);
	}
	
	private String formatDescription(String descExpanded)
	{
		String formatted = "";
		String tempText;
		boolean failedFormatting = false;
		boolean hasLineBreak = descExpanded.contains(StringUtils.BLOCK_BREAK);
		
		if (hasLineBreak)
		{		
			// First try to preserve any block breaks
			String[] blocks = descExpanded.split(StringUtils.BLOCK_BREAK);
			for (int blockIndex = 0; blockIndex < blocks.length; blockIndex++)
			{
				tempText = StringUtils.prettyFormatText(blocks[blockIndex],
						RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC);
				if (tempText != null)
				{
					formatted += tempText;
					if (blockIndex < blocks.length - 1)
					{
						formatted += StringUtils.BLOCK_BREAK;
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
			tempText = descExpanded.replace(StringUtils.BLOCK_BREAK, " ");
			formatted = StringUtils.prettyFormatText(tempText,
					RomConstants.MAX_CHARS_PER_LINE, RomConstants.PREFERRED_LINES_PER_EFFECT_DESC,
					RomConstants.MAX_LINES_PER_EFFECT_DESC, NUM_POINTERS_IN_FILE);
			
			if (formatted == null)
			{
				System.out.println("Could not nicely fit effect text over line breaks - attempting to " +
						"split words over lines to get it to fit for \"" + tempText + "\"");
				
				// If all else fails, just pack as tight as possible
				formatted = StringUtils.packFormatText(tempText,
					RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC,
					NUM_POINTERS_IN_FILE);
			}
		}
		
		return formatted;
	}
}
