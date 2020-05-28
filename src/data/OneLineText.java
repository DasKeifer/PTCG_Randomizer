package data;

import java.util.Set;

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

	public void readTextFromIds(byte[] bytes, int textIdIndex, Texts ptrToText, Set<Short> ptrsUsed)
	{
		int[] textIdIndexes = {textIdIndex};
		genericReadTextFromIds(bytes, textIdIndexes, ptrToText, ptrsUsed);
	}

	public void convertToIdsAndWriteText(byte[] bytes, int textIdIndex, Texts ptrToText)
	{
		int[] textIdIndexes = {textIdIndex};
		genericConvertToIdsAndWriteText(bytes, textIdIndexes, ptrToText);
	}
}
