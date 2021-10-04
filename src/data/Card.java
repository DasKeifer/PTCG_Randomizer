package data;

import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import data.romtexts.CardName;
import rom.Blocks;
import rom.Cards;
import rom.Texts;
import romAddressing.AssignedAddresses;
import util.ByteUtils;

import java.security.InvalidParameterException;
import java.util.Comparator;

public abstract class Card
{
	public static final int CARD_COMMON_SIZE = 8;
	public static final Comparator<Card> ID_SORTER = new IdSorter();
	public static final Comparator<Card> ROM_SORTER = new RomSorter();
	
	// TODO encapsulate these or make public?
	public CardType type;
	public CardName name;
	short gfx; // Card art
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	public CardId id;
	
	public Card()
	{
		name = createCardName(); 
	}
	
	protected abstract CardName createCardName();
	
	public Card(Card toCopy)
	{
		type = toCopy.type;
		name = new CardName(toCopy.name);
		gfx = toCopy.gfx;
		rarity = toCopy.rarity;
		set = toCopy.set;
		pack = toCopy.pack;
		id = toCopy.id;
	}
	
	public abstract Card copy();
	
	public static int addCardFromBytes(byte[] cardBytes, int startIndex, Texts idToText, Cards<Card> toAddTo)
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

		startIndex = card.readAndConvertIds(cardBytes, startIndex, idToText);
		toAddTo.add(card);
		return startIndex;
	}
	
	public abstract int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idsToText);
	public abstract void finalizeDataForAllocating(Cards<Card> cards, Texts texts, Blocks blocks);
	public abstract int writeData(byte[] cardBytes, int startIndex, AssignedAddresses assignedAddresses);

	public String toString()
	{
		return "Name = " + name.toString() + 
				"\nID = " + id + 
				"\nType = " + type + 
				"\nRarity = " + rarity + 
				"\nSet = " + set + 
				"\nPack = " + pack;
	}
	
	protected int commonReadAndConvertIds(byte[] cardBytes, int startIndex, Texts idsToText) 
	{
		int index = startIndex;
		
		type = CardType.readFromByte(cardBytes[index++]);
		gfx = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		index = name.readDataAndConvertIds(cardBytes, index, idsToText);
		
		rarity = CardRarity.readFromByte(cardBytes[index++]);

		pack = BoosterPack.readFromHexChar(ByteUtils.readUpperHexChar(cardBytes[index])); // no ++ - this reads only half the byte
		set = CardSet.readFromHexChar(ByteUtils.readLowerHexChar(cardBytes[index++]));
		
		id = CardId.readFromByte(cardBytes[index++]);
		
		return index;
	}
	
	protected void commonFinalizeDataForAllocating(Texts texts)
	{
		name.finalizeAndAddTexts(texts);
	}
	
	protected int commonWriteData(byte[] cardBytes, int startIndex) 
	{
		int index = startIndex;
		
		cardBytes[index++] = type.getValue();
		ByteUtils.writeAsShort(gfx, cardBytes, index);
		index += 2;
		
		index = name.writeTextId(cardBytes, index);
		
		cardBytes[index++] = rarity.getValue();

		cardBytes[index++] = ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue());
		
		cardBytes[index++] = id.getValue();
		
		return index;
	}

	 public static class IdSorter implements Comparator<Card>
	 {
		 public int compare(Card c1, Card c2)
	     {   
    		 return ByteUtils.unsignedCompareBytes(c1.id.getValue(), c2.id.getValue());
	     }
	 }

	 // This should be used if we randomize evos so we can shuffle poke to be next to each other
	 public static class RomSorter implements Comparator<Card>
	 {
	     public int compare(Card c1, Card c2)
	     {             
	    	 // If either is an energy or trainer, the natural sort order will work
	    	 if (c1.type.isEnergyCard() || c2.type.isEnergyCard() ||
	    			 c1.type.isTrainerCard() || c2.type.isTrainerCard())
	    	 {
	    		 return ByteUtils.unsignedCompareBytes(c1.id.getValue(), c2.id.getValue());
	    	 }
	    	 
	    	 // Otherwise both are pokemon - sort by pokedex id then cardId if they are the same.
	    	 // This will allow us to  reorder the pokemon as we want
	    	 PokemonCard pc1 = (PokemonCard) c1;
	    	 PokemonCard pc2 = (PokemonCard) c2;
	    	 int pokedexCompare = ByteUtils.unsignedCompareBytes(pc1.pokedexNumber, pc2.pokedexNumber);
	    	 if (pokedexCompare == 0)
	    	 {
	    		 return ByteUtils.unsignedCompareBytes(c1.id.getValue(), c2.id.getValue());
	    	 }
	    	 return pokedexCompare;
	     }
	 }
}
