package gameData;

import java.util.Map;
import java.util.Set;

import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	short effectPtr;
	String description;

	@Override
	public int getCardSizeInBytes() 
	{
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public String readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Map<Short, String> ptrToText, Set<Short> ptrsUsed) 
	{
		String name = readCommonNameAndDataAndConvertIds(cardBytes, startIndex, ptrToText, ptrsUsed);
		int index = startIndex + Card.CARD_COMMON_SIZE;
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		short descriptionPtr = ByteUtils.readAsShort(cardBytes, index);
		description = ptrToText.get(descriptionPtr);
		ptrsUsed.add(descriptionPtr);
		index += 2;
		short descriptionExtendedPtr = ByteUtils.readAsShort(cardBytes, index);
		if (descriptionExtendedPtr != 0)
		{
			description += (char)0x0C + ptrToText.get(descriptionExtendedPtr);
			ptrsUsed.add(descriptionExtendedPtr);
		}
		
		return name;
	}
	
	@Override
	public void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, short nameId, Map<Short, String> ptrToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, nameId, ptrToText);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;
		

		int splitChar = description.indexOf(0x0C);
		if (splitChar != -1)
		{
			ptrToText.put((short) (ptrToText.size() + 1), description.substring(0, splitChar));
			ByteUtils.writeAsShort((short) ptrToText.size(), cardBytes, index);
			index += 2;
			ptrToText.put((short) (ptrToText.size() + 1), description.substring(splitChar + 1));
			ByteUtils.writeAsShort((short) ptrToText.size(), cardBytes, index);
		}
		else
		{
			ptrToText.put((short) (ptrToText.size() + 1), description);
			ByteUtils.writeAsShort((short) ptrToText.size(), cardBytes, index);
			index += 2;
			ByteUtils.writeAsShort((short) 0, cardBytes, index);
		}
	}
}
