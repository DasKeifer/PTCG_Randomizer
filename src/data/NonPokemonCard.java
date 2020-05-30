package data;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	short effectPtr;
	public EffectDescription description;

	public NonPokemonCard() 
	{
		super();
		
		description = new EffectDescription();
	}
	
	public NonPokemonCard(NonPokemonCard toCopy) 
	{
		super(toCopy);
		
		effectPtr = toCopy.effectPtr;
		description = new EffectDescription(toCopy.description);
	}
	
	@Override
	public int readDataAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText, Set<Short> textIdsUsed) 
	{
		readCommonNameAndDataAndConvertIds(cardBytes, startIndex, idToText, textIdsUsed);
		int index = startIndex + Card.CARD_COMMON_SIZE;
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(cardBytes, descIndexes, name, idToText, textIdsUsed);
		return index + RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
	}
	
	@Override
	public int convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, idToText);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.convertToIdsAndWriteData(cardBytes, descIndexes, name, idToText);
		return index + RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
	}
}
