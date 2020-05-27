package data;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.StringUtils;

public class PokeDescription extends RomText
{		
	public void readTextFromIds(byte[] bytes, int textIdIndex, Texts ptrToText, Set<Short> ptrsUsed)
	{
		int[] textIdIndexes = {textIdIndex};
		genericReadTextFromIds(bytes, textIdIndexes, ptrToText, ptrsUsed);
	}

	public void convertToIdsAndWriteText(byte[] bytes, int textIdIndex, Texts ptrToText)
	{
		int[] textIdIndexes = {textIdIndex};
		setTextPreservingNewlines(formatDescription(getText()));
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, ptrToText);
	}
	
	private String formatDescription(String descExpanded)
	{
		if (descExpanded.contains(StringUtils.BLOCK_BREAK))
		{
			System.out.println("No block breaks are allowed in pokemon descriptions! Replacing with spaces and formatting");
			descExpanded = descExpanded.replaceAll(StringUtils.BLOCK_BREAK, " ");
		}

		String formatted = StringUtils.prettyFormatText(descExpanded, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
		
		// If it failed, pack the description
		if (formatted == null)
		{
			formatted = StringUtils.packFormatText(descExpanded, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
			if (formatted != null)
			{
				System.out.println("Failed to nicely pack poke description \"" + descExpanded + "\" so words were split across lines to make it fit");
			}
			else
			{
				throw new IllegalArgumentException("Could not successfully format poke description \"" + descExpanded + "\"");
			}
		}
		
		return formatted;
	}
}
