package gameData;

import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import util.ByteUtils;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;

public abstract class Card implements GameData
{
	public static final int CARD_COMMON_SIZE = 8;

	// Internal pointers used when reading and storing data to the rom
	protected short namePtr;
	
	// TODO encapsulate these or make public
	public CardType type;
	short gfx; // Card art
	// TODO: Maybe we don't store it here but it the higher level?
	public String name;
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	CardId id;
	
	public static Card createCardAtIndex(byte[] cardBytes, int startIndex, Map<Short, String> ptrToText, Set<Short> ptrsUsed)
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

		card.readData(cardBytes, startIndex);
		card.convertPointers(ptrToText, ptrsUsed);
		
		return card;
	}
	
	public String toString()
	{
		return "ID = " + id + 
				"\nType = " + type + 
				"\nName = " + name + 
				"\nRarity = " + rarity + 
				"\nSet = " + set + 
				"\nPack = " + pack;
	}
	
	public abstract int getCardSizeInBytes();
	
	protected void convertCommonPointers(Map<Short, String> ptrToText, Set<Short> ptrsUsed) 
	{
		name = ptrToText.get(namePtr);
		ptrsUsed.add(namePtr);
	}
	
	protected int readCommonData(byte[] cardBytes, int startIndex) 
	{
		int index = startIndex;
		
		type = CardType.readFromByte(cardBytes[index++]);
		gfx = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		namePtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		rarity = CardRarity.readFromByte(cardBytes[index++]);

		pack = BoosterPack.readFromHexChar(ByteUtils.readUpperHexChar(cardBytes[index])); // no ++ - this reads only half the byte
		set = CardSet.readFromHexChar(ByteUtils.readLowerHexChar(cardBytes[index++]));
		
		id = CardId.readFromByte(cardBytes[index++]);
		
		return index;
	}
	
	protected int writeCommonData(byte[] cardBytes, int startIndex) 
	{
		int index = startIndex;
		
		cardBytes[index++] = type.getValue();
		ByteUtils.writeAsShort(gfx, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(namePtr, cardBytes, index);
		index += 2;
		cardBytes[index++] = rarity.getValue();

		cardBytes[index++] = ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue());
		
		cardBytes[index++] = id.getValue();
		
		return index;
	}
}
