package gameData;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.TextUtils;

public class PokeDescription extends Description
{		
	public void readTextFromIds(byte[] bytes, int textIdIndex, String cardName, Texts ptrToText, Set<Short> ptrsUsed)
	{
		int[] textIdIndexes = {textIdIndex};
		genericReadTextFromIds(bytes, textIdIndexes, cardName, ptrToText, ptrsUsed);
	}

	public void convertToIdsAndWriteText(byte[] bytes, int textIdIndex, String cardName, Texts ptrToText)
	{
		int[] textIdIndexes = {textIdIndex};
		String descToWrite = prepareDescForFormatting(cardName);
		descToWrite = formatDescription(descToWrite);
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, descToWrite, ptrToText);
	}
	
	private String formatDescription(String descExpanded)
	{
		if (descExpanded.contains(TextUtils.BLOCK_BREAK))
		{
			System.out.println("No block breaks are allowed in pokemon descriptions! Replacing with spaces and formatting");
			descExpanded = descExpanded.replaceAll(TextUtils.BLOCK_BREAK, " ");
		}

		// TODO If its 7 lines instead of 6 we need some extra character o somethin
		String formatted = TextUtils.prettyFormatText(descExpanded, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
		
		// If it failed, pack the description
		if (formatted == null)
		{
			formatted = TextUtils.packFormatText(descExpanded, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
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
