package data;

import constants.RomConstants;
import data.romtexts.CardName;
import data.romtexts.EffectDescription;
import rom.Blocks;
import rom.Cards;
import rom.Texts;
import romAddressing.AssignedAddresses;
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
	protected CardName createCardName()
	{
		return new CardName(false); // not a poke name
	}
	
	@Override
	public int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		commonReadAndConvertIds(cardBytes, startIndex, idToText);
		int index = startIndex + Card.CARD_COMMON_SIZE;
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(cardBytes, descIndexes, name, idToText);
		return index + RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
	}
	
	@Override
	public void finalizeDataForAllocating(Cards<Card> cards, Texts texts, Blocks blocks)
	{
		commonFinalizeDataForAllocating(texts);
		
		description.finalizeAndAddTexts(texts, name.toString());
	}
	
	@Override
	public int writeData(byte[] cardBytes, int startIndex, AssignedAddresses unused) 
	{
		int index = commonWriteData(cardBytes, startIndex);
		
		// write non pokemon specific data
		ByteUtils.writeAsShort(effectPtr, cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.writeTextId(cardBytes, descIndexes);
		return index + RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
	}
}
