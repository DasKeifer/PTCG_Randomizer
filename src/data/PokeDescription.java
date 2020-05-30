package data;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.StringUtils;

public class PokeDescription extends RomText
{		
	public PokeDescription() 
	{
		super();
	}
	
	public PokeDescription(PokeDescription toCopy) 
	{
		super(toCopy);
	}

	public int readDataAndConvertIds(byte[] bytes, int textIdIndex, Texts idToText, Set<Short> textIdsUsed)
	{
		int[] textIdIndexes = {textIdIndex};
		genericReadTextFromIds(bytes, textIdIndexes, idToText, textIdsUsed);
		return textIdIndex + RomConstants.TEXT_ID_SIZE_IN_BYTES;
	}

	public int convertToIdsAndWriteData(byte[] bytes, int textIdIndex, Texts idToText)
	{
		int[] textIdIndexes = {textIdIndex};
		setTextPreservingNewlines(format(getText()));
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, idToText);
		return textIdIndex + RomConstants.TEXT_ID_SIZE_IN_BYTES;
	}
	
	private String format(String descExpanded)
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
