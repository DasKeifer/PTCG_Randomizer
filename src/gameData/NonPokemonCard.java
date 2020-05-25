package gameData;

import java.util.Set;

import rom.Cards;
import rom.Texts;
import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	short effectPtr;
	public Description description = new Description();

	@Override
	public int getCardSizeInBytes() 
	{
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public void readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Cards cards, Texts ptrToText, Set<Short> ptrsUsed) 
	{
		readCommonNameAndDataAndConvertIds(cardBytes, startIndex, ptrToText, ptrsUsed);
		int index = startIndex + Card.CARD_COMMON_SIZE;
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		description.readTextFromIds(cardBytes, index, name, true, ptrToText, ptrsUsed); // effect description
	}
	
	@Override
	public void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, ptrToText);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;
		
		description.convertToIdsAndWriteText(cardBytes, index, name, ptrToText);
	}
}
