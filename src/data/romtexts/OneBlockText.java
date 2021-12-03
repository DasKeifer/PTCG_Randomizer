package data.romtexts;


import constants.PtcgRomConstants;
import rom.Texts;

public class OneBlockText extends RomText
{
	public OneBlockText(int maxCharsPerLine, int maxLines) 
	{
		super(maxCharsPerLine, maxLines, maxLines, 1); // prefer the max lines, 1 block
	}
	
	public OneBlockText(String text, int maxCharsPerLine, int maxLines)
	{
		this(maxCharsPerLine, maxLines);
		setText(text);
	}
	
	public OneBlockText(OneBlockText toCopy) 
	{
		super(toCopy);
	}
	
	public int readDataAndConvertIds(byte[] bytes, int textIdIndex, Texts idToText)
	{
		int[] textIdIndexes = {textIdIndex};
		readDataAndConvertIds(bytes, textIdIndexes, idToText);
		return textIdIndex + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES;
	}

	public int writeTextId(byte[] bytes, int textIdIndex)
	{
		int[] textIdIndexes = {textIdIndex};
		writeTextId(bytes, textIdIndexes);
		return textIdIndex + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES;
	}

	public short getTextId() 
	{
		return getTextIds().get(0);
	}
}
