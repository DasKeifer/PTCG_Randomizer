package data;

import java.util.List;

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
	
	@Override
	protected boolean needsReformatting(List<String> text) 
	{
		if (text.size() > 1)
		{
			return true;
		}
		else if (text.isEmpty())
		{
			return false;
		}
		
		return !StringUtils.isFormattedValidly(
				text.get(0), RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
	}

	@Override
	protected List<String> formatText(String text) 
	{
		List<String> formatted = StringUtils.prettyFormatText(text, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
		
		// If it failed, pack the description
		if (formatted.isEmpty())
		{
			formatted = StringUtils.packFormatText(text, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_POKE_DESC);
			if (formatted.isEmpty())
			{
				System.out.println("Failed to nicely pack poke description \"" + 
						StringUtils.createAbbreviation(text, 25) + "\" so words were split across lines to make it fit");
			}
			else
			{
				throw new IllegalArgumentException("Could not successfully format poke description \"" +
						StringUtils.createAbbreviation(text, 25) + "\"");
			}
		}

		return formatted;
	}
	
	public int readDataAndConvertIds(byte[] bytes, int textIdIndex, Texts idToText)
	{
		int[] textIdIndexes = {textIdIndex};
		genericReadTextFromIds(bytes, textIdIndexes, idToText);
		return textIdIndex + RomConstants.TEXT_ID_SIZE_IN_BYTES;
	}

	public int writeTextId(byte[] bytes, int textIdIndex)
	{
		int[] textIdIndexes = {textIdIndex};
		genericWriteTextIds(bytes, textIdIndexes);
		return textIdIndex + RomConstants.TEXT_ID_SIZE_IN_BYTES;
	}
}
