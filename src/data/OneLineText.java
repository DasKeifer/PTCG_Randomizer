package data;

import java.util.ArrayList;
import java.util.List;

import constants.RomConstants;
import rom.Texts;
import util.StringUtils;

public class OneLineText extends RomText
{
	public OneLineText()
	{
		super();
	}
	
	public OneLineText(OneLineText toCopy)
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
		
		return StringUtils.contains(text, "\n") || StringUtils.contains(text, StringUtils.BLOCK_BREAK);
	}

	@Override
	protected List<String> formatText(String text)
	{
		List<String> output = new ArrayList<>();
		output.add(text);
		return output;
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
