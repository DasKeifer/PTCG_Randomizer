package gameData;

import java.util.Set;

import rom.Texts;
import util.ByteUtils;

public class OneLineText 
{
	public String text = "";

	public void readTextFromIds(byte[] bytes, int textIdIndex, Texts ptrToText, Set<Short> ptrsUsed)
	{
		short textId = ByteUtils.readAsShort(bytes, textIdIndex);
		if (textId == 0)
		{
			text = "";
		}
		else
		{
			text = ptrToText.getAtId(textId);
			ptrsUsed.add(textId);
		}
	}

	public void convertToIdsAndWriteText(byte[] bytes, int textIdIndex, Texts ptrToText)
	{
		if (text.isEmpty())
		{
			ByteUtils.writeAsShort((short) 0, bytes, textIdIndex);
		}
		else
		{
			ByteUtils.writeAsShort(ptrToText.insertTextOrGetId(text), bytes, textIdIndex);
		}
	}
}
