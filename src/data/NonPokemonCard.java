package data;

import constants.RomConstants;
import rom.Texts;
import util.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	private short effectPtr;
	private EffectDescription description;

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
	
	public NonPokemonCard copy()
	{
		return new NonPokemonCard(this);
	}
	
	@Override
	public int readDataAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		readCommonNameAndDataAndConvertIds(cardBytes, startIndex, idToText);
		int index = startIndex + Card.CARD_COMMON_SIZE;
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(cardBytes, descIndexes, name, idToText);
		return index + RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
	}
	
	@Override
	public void finalizeAndAddTexts(Texts idsToText)
	{
		finalizeAndAddCommonTexts(idsToText);
		
		description.finalizeAndAddTexts(idsToText, name.toString());
	}
	
	@Override
	public int convertToIdsAndWriteData(byte[] cardBytes, int startIndex) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.writeTextId(cardBytes, descIndexes);
		return index + RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
	}
}
