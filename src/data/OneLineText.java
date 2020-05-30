package data;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;

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

	public int readDataAndConvertIds(byte[] bytes, int textIdIndex, Texts idToText, Set<Short> textIdsUsed)
	{
		int[] textIdIndexes = {textIdIndex};
		genericReadTextFromIds(bytes, textIdIndexes, idToText, textIdsUsed);
		return textIdIndex + RomConstants.TEXT_ID_SIZE_IN_BYTES;
	}

	public int convertToIdsAndWriteData(byte[] bytes, int textIdIndex, Texts idToText)
	{
		int[] textIdIndexes = {textIdIndex};
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, idToText);
		return textIdIndex + RomConstants.TEXT_ID_SIZE_IN_BYTES;
	}
}
