package gameData;

import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import util.IoUtils;

import java.security.InvalidParameterException;

public abstract class Card implements GameData
{
	public static final int CARD_COMMON_SIZE = 8;
	
	CardType type;
	short gfx; // Card art
	short name; // No gameplay impact
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	CardId id; // This is used to calculate the offset of the card data and is used to reference other cards
	
	public static Card createCardAtIndex(byte[] cardBytes, int startIndex)
	{
		CardType type = CardType.readFromByte(cardBytes[startIndex]);
		
		if(type.isPokemonCard())
		{
			return new PokemonCard();
		}
		else if (type.isEnergyCard())
		{
			return new NonPokemonCard();
		}
		else if (type.isTrainerCard())
		{
			return new NonPokemonCard();
		}
		else
		{
			throw new InvalidParameterException("Failed to determine type of card at index " + 
					startIndex + " that is of type " + type);
		}
	}
	
	protected int readCommonData(byte[] cardBytes, int startIndex) 
	{
		int index = startIndex;
		
		type = CardType.readFromByte(cardBytes[index++]);
		gfx = IoUtils.readShort(cardBytes, index);
		index += 2;
		name = IoUtils.readShort(cardBytes, index);
		index += 2;
		rarity = CardRarity.readFromByte(cardBytes[index++]);
		
		set = CardSet.readFromByte(cardBytes[index]); // no ++, this reads only half the byte
		pack = BoosterPack.readFromByte(cardBytes[index++]);
		
		id = CardId.readFromByte(cardBytes[index++]);
		
		return index;
	}
	
	protected int writeCommonData(byte[] cardBytes, int startIndex) 
	{
		int index = startIndex;
		
		cardBytes[index++] = type.getValue();
		IoUtils.writeShort(gfx, cardBytes, index);
		index += 2;
		IoUtils.writeShort(name, cardBytes, index);
		index += 2;
		cardBytes[index++] = rarity.getValue();

		cardBytes[index] = set.getValue(); // no ++, this reads only half the byte
		cardBytes[index++] += pack.getValue();
		
		cardBytes[index++] = id.getValue();
		
		return index;
	}
}
