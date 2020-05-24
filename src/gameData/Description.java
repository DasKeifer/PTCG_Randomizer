package gameData;

import java.util.Set;

import rom.Texts;
import util.ByteUtils;

public class Description 
{
	String desc = "";
	boolean isTwoBlocks;
	
	public int readTextFromIds(byte[] bytes, int startIndex, boolean twoBlocks, Texts ptrToText, Set<Short> ptrsUsed)
	{
		isTwoBlocks = twoBlocks;
		
		short textPtr = ByteUtils.readAsShort(bytes, startIndex);
		if (textPtr == 0)
		{
			desc = "";
			if (isTwoBlocks)
			{
				return startIndex + 4;
			}
			return startIndex + 2;
		}
		
		// TODO remove text formatting
		desc = ptrToText.getAtId(textPtr);
		ptrsUsed.add(textPtr);
		startIndex += 2;
		
		if (isTwoBlocks)
		{
			short textExtendedPtr = ByteUtils.readAsShort(bytes, startIndex);
			if (textExtendedPtr != 0)
			{
				desc += (char) 0x0C + ptrToText.getAtId(textExtendedPtr);
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
			if (desc == null || desc.isEmpty())
			{
				ByteUtils.writeLittleEndian(0, bytes, startIndex, 4);
				return startIndex + 4;
			}
			
			int splitChar = desc.indexOf((char)0x0C);
			if (splitChar != -1)
			{
				short id = ptrToText.insertTextOrGetId(desc.substring(0, splitChar));
				ByteUtils.writeAsShort(id, bytes, startIndex);
				startIndex += 2;
				id = ptrToText.insertTextOrGetId(desc.substring(splitChar + 1));
				ByteUtils.writeAsShort(id, bytes, startIndex);
				startIndex += 2;
			}
			else
			{
				short id = ptrToText.insertTextOrGetId(desc);
				ByteUtils.writeAsShort((short) id, bytes, startIndex);
				startIndex += 2;
				ByteUtils.writeAsShort((short) 0, bytes, startIndex);
				startIndex += 2;
			}
		}
		else
		{
			if (desc == null || desc.isEmpty())
			{
				ByteUtils.writeLittleEndian(0, bytes, startIndex, 2);
				return startIndex + 2;
			}
			
			short id = ptrToText.insertTextOrGetId(desc);
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
