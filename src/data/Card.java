package data;

import constants.CardConstants.CardId;
import constants.CardDataConstants.BoosterPack;
import constants.CardDataConstants.CardRarity;
import constants.CardDataConstants.CardSet;
import constants.CardDataConstants.CardType;
import data.romtexts.CardName;
import rom.Cards;
import rom.Texts;
import rom_packer.Blocks;
import rom_packer.HybridBlock;
import rom_packer.MovableBlock;
import gbc_framework.utils.ByteUtils;

import java.security.InvalidParameterException;
import java.util.Comparator;

import compiler.CodeBlock;
import compiler.RawBytePacker;

public abstract class Card
{
	public static final int CARD_COMMON_SIZE = 8;
	public static final Comparator<Card> ID_SORTER = new IdSorter();
	public static final Comparator<Card> ROM_SORTER = new RomSorter();
	
	private int readFromAddress;
	
	// TODO later: encapsulate these or make public?
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
		readFromAddress = -1;
	}
	
	protected abstract CardName createCardName();
	
	public Card(Card toCopy)
	{
		readFromAddress = toCopy.readFromAddress;
		type = toCopy.type;
		name = new CardName(toCopy.name);
		gfx = toCopy.gfx;
		rarity = toCopy.rarity;
		set = toCopy.set;
		pack = toCopy.pack;
		id = toCopy.id;
	}
	
	public abstract Card copy();
	
	public static int addCardFromBytes(byte[] cardBytes, int startIndex, Texts idToText, CardGroup<Card> toAddTo)
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
	public abstract void finalizeAndAddData(Cards cards, Texts texts, Blocks blocks);
	protected abstract CodeBlock convertToCodeBlock();
	public abstract int getSize();

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
		readFromAddress = startIndex;
		
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
	
	protected void commonFinalizeAndAddData(Texts texts)
	{
		name.finalizeAndAddTexts(texts);
	}
	
	public HybridBlock convertToHybridBlock()
	{
		return new HybridBlock(
				new MovableBlock(convertToCodeBlock(), 0, (byte) 0xC, (byte) 0xD),
				readFromAddress);
	}
	
	protected CodeBlock convertCommonDataToCodeBlock() 
	{
		RawBytePacker bytes = new RawBytePacker();
		bytes.append(type.getValue());
		bytes.append(ByteUtils.shortToLittleEndianBytes(gfx));
		bytes.append(ByteUtils.shortToLittleEndianBytes(name.getTextId()));
		bytes.append(
				rarity.getValue(),
				ByteUtils.packHexCharsToByte(pack.getValue(), set.getValue()),
				id.getValue()
		);

		CodeBlock block = new CodeBlock("internal_card_" + name.toString() + "_" + 
				ByteUtils.unsignedByteAsShort(id.getValue()));
		block.appendInstruction(bytes.createRawByteInsruct());
		return block;
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
