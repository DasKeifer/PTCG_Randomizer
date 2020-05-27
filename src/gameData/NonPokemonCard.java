package gameData;

import java.util.Set;

import constants.RomConstants;
import rom.Cards;
import rom.Texts;
import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	short effectPtr;
	public EffectDescription description = new EffectDescription();

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

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readTextFromIds(cardBytes, descIndexes, name.text, ptrToText, ptrsUsed);
	}
	
	@Override
	public void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, ptrToText);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.convertToIdsAndWriteText(cardBytes, descIndexes, name.text, ptrToText);
	}
}
