package gameData;

import java.util.Set;

import constants.CardDataConstants.*;
import rom.Cards;
import rom.Texts;
import util.ByteUtils;

public class PokemonCard extends Card 
{
	public static final int TOTAL_SIZE_IN_BYTES = 65;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	byte hp; // TODO: non multiples of 10?
	EvolutionStage stage;
	CardVersions prevEvo;
	
	Move move1;
	Move move2;
	
	byte retreatCost; // TODO: max allowed?
	WeaknessResistanceType weakness; // TODO: Allows multiple?
	WeaknessResistanceType resistance; // TODO: Allows multiple?
	short pokemonCategory; // TODO: Investigate - i.e cocoon, hairy bug, etc. Shouldn't need to change
	byte pokedexNumber;
	byte unknownByte1;
	byte level; // TODO: Investigate No gameplay impact?
	short length; //TODO: One byte is feet, another is inches - separate them // TODO: Investigate No gameplay impact?
	short weight; // TODO: Investigate No gameplay impact?
	Description description = new Description();
	byte unknownByte2;
	
	@Override
	public String toString()
	{
		String string = super.toString() + 
				"\nPokedex Number = " + pokedexNumber + 
				"\nDesciption = " + description + 
				"\nHP = " + hp +
				"\nStage = " + stage + 
				"\nPrevEvolution = ";
		if (prevEvo != null)
		{
			string += prevEvo.name;
		}
		else
		{
			string += "None";
		}
		
		string += "\nRetreatCost = " + retreatCost +
				"\nWeakness = " + weakness +
				"\nResistance = " + resistance  +
				"\nMoves\n" + move1.toString() + "\n" + move2.toString();
		return string;
	}

	@Override
	public int getCardSizeInBytes() 
	{
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public String readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Cards cards, Texts ptrToText, Set<Short> ptrsUsed) 
	{
		String name = readCommonNameAndDataAndConvertIds(cardBytes, startIndex, ptrToText, ptrsUsed);
		
		int index = startIndex + Card.CARD_COMMON_SIZE;
		hp = cardBytes[index++];
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		
		// Read the prev evolution
		short id = ByteUtils.readAsShort(cardBytes, index);
		if (id != 0)
		{
			prevEvo = cards.getCardsWithName(ptrToText.getAtId(id));
		}
		index += 2;
		
		move1 = new Move();
		index = move1.readNameAndDataAndConvertIds(cardBytes, index, ptrToText, ptrsUsed);
		move2 = new Move();
		index = move2.readNameAndDataAndConvertIds(cardBytes, index, ptrToText, ptrsUsed);;
		
		retreatCost = cardBytes[index++];
		weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		pokemonCategory = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		pokedexNumber = cardBytes[index++];
		unknownByte1 = cardBytes[index++];
		level = cardBytes[index++];
		length = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		weight = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
        index = description.readTextFromIds(cardBytes, index, false, ptrToText, ptrsUsed); // one block of text
		
		unknownByte2 = cardBytes[index++];
		
		return name;
	}

	@Override
	public void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, name, ptrToText);
		
		cardBytes[index++] = hp;
		cardBytes[index++] = stage.getValue();
		
		if (prevEvo != null)
		{
			ByteUtils.writeAsShort(ptrToText.insertTextOrGetId(prevEvo.name), cardBytes, index);
		}
		else
		{
			ByteUtils.writeAsShort((short) 0, cardBytes, index);
		}
		index += 2;
		
		index = move1.convertToIdsAndWriteData(cardBytes, index, ptrToText);
		index = move2.convertToIdsAndWriteData(cardBytes, index, ptrToText);
		
		cardBytes[index++] = retreatCost;
		cardBytes[index++] = weakness.getValue();
		cardBytes[index++] = resistance.getValue();
		ByteUtils.writeAsShort(pokemonCategory, cardBytes, index);
		index += 2;
		cardBytes[index++] = pokedexNumber;
		cardBytes[index++] = unknownByte1;
		cardBytes[index++] = level;
		ByteUtils.writeAsShort(length, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(weight, cardBytes, index);
		index += 2;
		
		index = description.convertToIdsAndWriteText(cardBytes, index, ptrToText);
		
		cardBytes[index++] = unknownByte2;
	}
}
