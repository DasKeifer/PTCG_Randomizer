package gameData;

import java.util.Set;

import rom.Texts;
import util.ByteUtils;

public class Description 
{
	String desc;
	boolean isTwoBlocks;
	
	public int readTextFromIds(byte[] bytes, int startIndex, boolean twoBlocks, Texts ptrToText, Set<Short> ptrsUsed)
	{
		// TODO remove text formatting
		short textPtr = ByteUtils.readAsShort(bytes, startIndex);
		desc = ptrToText.getAtId(textPtr);
		ptrsUsed.add(textPtr);
		startIndex += 2;
		
		isTwoBlocks = twoBlocks;
		if (isTwoBlocks)
		{
			short textExtendedPtr = ByteUtils.readAsShort(bytes, startIndex);
			if (textExtendedPtr != 0)
			{
				desc += (char) 0x06 + ptrToText.getAtId(textExtendedPtr);
				ptrsUsed.add(textExtendedPtr);
			}
			startIndex += 2;
		}
		return startIndex;
	}

	public int convertToIdsAndWriteText(byte[] bytes, int startIndex, Texts ptrToText) 
	{
		// TODO: Auto format text
		if (isTwoBlocks)
		{
			int splitChar = desc.indexOf((char)0x06);
			if (splitChar != -1)
			{
				short id = ptrToText.insertTextAtNextId(desc.substring(0, splitChar));
				ByteUtils.writeAsShort(id, bytes, startIndex);
				startIndex += 2;
				id = ptrToText.insertTextAtNextId(desc.substring(splitChar + 1));
				ByteUtils.writeAsShort(id, bytes, startIndex);
				startIndex += 2;
			}
			else
			{
				short id = ptrToText.insertTextAtNextId(desc);
				ByteUtils.writeAsShort((short) id, bytes, startIndex);
				startIndex += 2;
				ByteUtils.writeAsShort((short) 0, bytes, startIndex);
				startIndex += 2;
			}
		}
		else
		{
			short id = ptrToText.insertTextAtNextId(desc);
			ByteUtils.writeAsShort(id, bytes, startIndex);
			startIndex += 2;
		}
		
		return startIndex;
	}
	
	@Override
	public String toString()
	{
		return desc;
	}
}
