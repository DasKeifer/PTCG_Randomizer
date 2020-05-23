package gameData;

import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import rom.IdsToText;
import util.ByteUtils;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;

public abstract class Card
{
	public static final int CARD_COMMON_SIZE = 8;
	
	// TODO encapsulate these or make public
	public CardType type;
	short gfx; // Card art
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	CardId id;
	
	public static void addCardAtIndex(byte[] cardBytes, int startIndex, Map<String, CardVersions> cardsByName, IdsToText ptrToText, Set<Short> ptrsUsed)
	{
		CardType type = CardType.readFromByte(cardBytes[startIndex]);
		
		Card card;
		if(type.isPokemonCard())
		{
			card = new PokemonCard();
		}
		else if (type.isEnergyCard())
		{
			card = new NonPokemonCard();
		}
		else if (type.isTrainerCard())
		{
			card = new NonPokemonCard();
		}
		else
		{
			throw new InvalidParameterException("Failed to determine type of card at index " + 
					startIndex + " that is of type " + type);
		}

		String name = card.readNameAndDataAndConvertIds(cardBytes, startIndex, ptrToText, ptrsUsed);

		if (!cardsByName.containsKey(name))
		{
			cardsByName.put(name, new CardVersions());
		}
		cardsByName.get(name).versions.add(card);
	}
	
	public String toString()
	{
		return "ID = " + id + 
				"\nType = " + type + 
				"\nRarity = " + rarity + 
				"\nSet = " + set + 
				"\nPack = " + pack;
	}
	
	public abstract String readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, IdsToText ptrToText, Set<Short> ptrsUsed);
	public abstract void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, short nameId, IdsToText ptrToText);
	public abstract int getCardSizeInBytes();
	
	protected String readCommonNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, IdsToText ptrToText, Set<Short> ptrsUsed) 
	{
		int index = startIndex;
		
		type = CardType.readFromByte(cardBytes[index++]);
		gfx = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		short nameId = ByteUtils.readAsShort(cardBytes, index);
		String name = ptrToText.getAtId(nameId);
		ptrsUsed.add(nameId);
		index += 2;
		
		rarity = CardRarity.readFromByte(cardBytes[index++]);

		pack = BoosterPack.readFromHexChar(ByteUtils.readUpperHexChar(cardBytes[index])); // no ++ - this reads only half the byte
		set = CardSet.readFromHexChar(ByteUtils.readLowerHexChar(cardBytes[index++]));
		
		id = CardId.readFromByte(cardBytes[index++]);
		
		return name;
	}
	
	protected int convertCommonToIdsAndWriteData(byte[] cardBytes, int startIndex, short nameId, IdsToText ptrToText) 
	{
		int index = startIndex;
		
		cardBytes[index++] = type.getValue();
		ByteUtils.writeAsShort(gfx, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(nameId, cardBytes, index);
		index += 2;
		cardBytes[index++] = rarity.getValue();

		cardBytes[index++] = ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue());
		
		cardBytes[index++] = id.getValue();
		
		return index;
	}
}
