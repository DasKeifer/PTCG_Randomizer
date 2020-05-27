package gameData;

import constants.RomConstants;
import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import rom.Cards;
import rom.Texts;
import util.ByteUtils;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.Set;

public abstract class Card
{
	public static final int CARD_COMMON_SIZE = 8;
	
	// TODO encapsulate these or make public
	public CardType type;
	public OneLineText name = new OneLineText();
	short gfx; // Card art
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	public CardId id;
	
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

		card.readNameAndDataAndConvertIds(cardBytes, startIndex, cardsByName, ptrToText, ptrsUsed);
		cardsByName.add(card);
	}
	
	public String toString()
	{
		return "ID = " + id + 
				"\nType = " + type + 
				"\nRarity = " + rarity + 
				"\nSet = " + set + 
				"\nPack = " + pack;
	}
	
	public abstract void readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Cards cards, Texts ptrToText, Set<Short> ptrsUsed);
	public abstract void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText);
	public abstract int getCardSizeInBytes();
	
	protected void readCommonNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Texts ptrToText, Set<Short> ptrsUsed) 
	{
		int index = startIndex;
		
		type = CardType.readFromByte(cardBytes[index++]);
		gfx = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		name.readTextFromIds(cardBytes, index, ptrToText, ptrsUsed);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		rarity = CardRarity.readFromByte(cardBytes[index++]);

		pack = BoosterPack.readFromHexChar(ByteUtils.readUpperHexChar(cardBytes[index])); // no ++ - this reads only half the byte
		set = CardSet.readFromHexChar(ByteUtils.readLowerHexChar(cardBytes[index++]));
		
		id = CardId.readFromByte(cardBytes[index]);
	}
	
	protected int convertCommonToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText) 
	{
		int index = startIndex;
		
		cardBytes[index++] = type.getValue();
		ByteUtils.writeAsShort(gfx, cardBytes, index);
		index += 2;
		
		name.convertToIdsAndWriteText(cardBytes, index, ptrToText);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		cardBytes[index++] = rarity.getValue();

		cardBytes[index++] = ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue());
		
		cardBytes[index++] = id.getValue();
		
		return index;
	}

	 public static class NameIdSorter implements Comparator<Card>
	 {
	     public int compare(Card c1, Card c2)
	     {
	    	 int val = c1.name.text.compareTo(c2.name.text);
	    	 if (val == 0)
	    	 {
	    		 return ByteUtils.unsignedCompareShorts(c1.id.getValue(), c2.id.getValue());
	    	 }
	    	 return val;
	     }
	 }

	 public static class IdSorter implements Comparator<Card>
	 {
		 public int compare(Card c1, Card c2)
	     {   
    		 return ByteUtils.unsignedCompareShorts(c1.id.getValue(), c2.id.getValue());
	     }
	 }
	 public static class RomSorter implements Comparator<Card>
	 {
		 // This is used if we randomize evos so we can shuffle poke to be next to each other
	     public int compare(Card c1, Card c2)
	     {             
	    	 // If either is an energy or trainer, the natural sort order will work
	    	 if (c1.type.isEnergyCard() || c2.type.isEnergyCard() ||
	    			 c1.type.isTrainerCard() || c2.type.isTrainerCard())
	    	 {
	    		 return ByteUtils.unsignedCompareShorts(c1.id.getValue(), c2.id.getValue());
	    	 }
	    	 
	    	 // Otherwise both are pokemon - sort by pokedex id then cardId if they are the same.
	    	 // This will allow us to  reorder the pokemon as we want
	    	 PokemonCard pc1 = (PokemonCard) c1;
	    	 PokemonCard pc2 = (PokemonCard) c2;
	    	 int pokedexCompare = ByteUtils.unsignedCompareShorts(pc1.pokedexNumber, pc2.pokedexNumber);
	    	 if (pokedexCompare == 0)
	    	 {
	    		 return ByteUtils.unsignedCompareShorts(c1.id.getValue(), c2.id.getValue());
	    	 }
	    	 return pokedexCompare;
	     }
	 }
}
