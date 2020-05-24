package gameData;

import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import rom.Cards;
import rom.Texts;
import util.ByteUtils;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class Card
{
	public static final int CARD_COMMON_SIZE = 8;
	
	// Used only temporarily when writing to the rom
	protected String name;
	
	// TODO encapsulate these or make public
	public CardType type;
	short gfx; // Card art
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	CardId id;
	
	public static void addCardAtIndex(byte[] cardBytes, int startIndex, Cards cardsByName, Texts ptrToText, Set<Short> ptrsUsed)
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

		String name = card.readNameAndDataAndConvertIds(cardBytes, startIndex, cardsByName, ptrToText, ptrsUsed);

		cardsByName.addCard(name, card);
	}
	
	public String toString()
	{
		return "ID = " + id + 
				"\nType = " + type + 
				"\nRarity = " + rarity + 
				"\nSet = " + set + 
				"\nPack = " + pack;
	}
	
	public abstract String readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Cards cards, Texts ptrToText, Set<Short> ptrsUsed);
	public abstract void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText);
	public abstract int getCardSizeInBytes();
	
	protected String readCommonNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Texts ptrToText, Set<Short> ptrsUsed) 
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
	
	protected int convertCommonToIdsAndWriteData(byte[] cardBytes, int startIndex, String name, Texts ptrToText) 
	{
		int index = startIndex;
		
		cardBytes[index++] = type.getValue();
		ByteUtils.writeAsShort(gfx, cardBytes, index);
		index += 2;
		
		ByteUtils.writeAsShort(ptrToText.insertTextOrGetId(name), cardBytes, index);
		index += 2;
		
		cardBytes[index++] = rarity.getValue();

		cardBytes[index++] = ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue());
		
		cardBytes[index++] = id.getValue();
		
		return index;
	}
	
	 static class CardSorter implements Comparator<Card>
	 {
	     public int compare(Card c1, Card c2)
	     {
	    	 if (c1.type != c2.type)
	    	 {
	    		 if (c1.type.getValue() < c2.type.getValue())
	    		 {
	    			 return -1;
	    		 }
	    		 return 1;
	    	 }
	    	 
	    	 if (c1.id.getValue() < c2.id.getValue())
	    	 {
	    		 return -1;
	    	 }
	         return 1;
	     }
	 }
	
	public List<Card> flattenAndSortCards(Cards cards)
	{
		List<Card> flatCards = new LinkedList<>();
		for (CardVersions versions : cards.getCards())
		{
			for (Card card : versions.versions)
			{
				card.name = versions.name;
				flatCards.add(card);
			}
		}
		
		Collections.sort(flatCards, new CardSorter());
		
		return flatCards;
	}
}
