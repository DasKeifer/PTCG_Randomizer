package data;

import java.util.Set;

import constants.CardDataConstants.*;
import rom.Texts;
import util.ByteUtils;

public class PokemonCard extends Card 
{
	public static final int TOTAL_SIZE_IN_BYTES = 65;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	byte hp; // TODO: non multiples of 10?
	EvolutionStage stage;
	OneLineText prevEvoName;
	
	public Move move1;
	Move move2;
	
	byte retreatCost; // TODO: max allowed?
	WeaknessResistanceType weakness; // TODO: Allows multiple?
	WeaknessResistanceType resistance; // TODO: Allows multiple?
	OneLineText pokemonCategory; // TODO: Investigate
	public byte pokedexNumber;
	byte unknownByte1; // TODO: Always 0?
	byte level; // TODO: Investigate No gameplay impact?
	short length; //TODO: One byte is feet, another is inches - separate them // TODO: Investigate No gameplay impact?
	short weight; // TODO: Investigate No gameplay impact?
	PokeDescription description;
	 // TODO: At least somewhat tracks with evo stage in asm files - 19 for first stage, 16 for second stage, 0 for final stage?
	byte unknownByte2;

	public PokemonCard()
	{
		super();
		
		prevEvoName = new OneLineText();
		move1 = new Move();
		move2 = new Move();
		pokemonCategory = new OneLineText();
		description = new PokeDescription();
	}
	
	public PokemonCard(PokemonCard toCopy)
	{
		super(toCopy);
		
		hp = toCopy.hp;
		stage = toCopy.stage;
		prevEvoName = new OneLineText(toCopy.prevEvoName);
		move1 = new Move(toCopy.move1);
		move2 = new Move(toCopy.move2);;
		retreatCost = toCopy.retreatCost;
		weakness = toCopy.weakness;
		resistance = toCopy.resistance;
		pokemonCategory = new OneLineText(toCopy.pokemonCategory);
		pokedexNumber = toCopy.pokedexNumber;
		unknownByte1 = toCopy.unknownByte1;
		level = toCopy.level;
		length = toCopy.length;
		weight = toCopy.weight;
		description = new PokeDescription(toCopy.description);
		unknownByte2 = toCopy.unknownByte2;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + 
				"\nPokedex Number = " + pokedexNumber + 
				"\nDesciption = " + description.toString() + 
				"\nHP = " + hp +
				"\nStage = " + stage + 
				"\nPrevEvolution = " + prevEvoName.toString() +
				"\nRetreatCost = " + retreatCost +
				"\nWeakness = " + weakness +
				"\nResistance = " + resistance  +
				"\nMoves\n" + move1.toString() + "\n" + move2.toString();
	}
	
	@Override
	public int readDataAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText, Set<Short> textIdsUsed) 
	{
		readCommonNameAndDataAndConvertIds(cardBytes, startIndex, idToText, textIdsUsed);
		
		int index = startIndex + Card.CARD_COMMON_SIZE;
		hp = cardBytes[index++];
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		
		// Read the prev evolution
		index = prevEvoName.readDataAndConvertIds(cardBytes, index, idToText, textIdsUsed);
		
		index = move1.readDataAndConvertIds(cardBytes, index, name, idToText, textIdsUsed);
		index = move2.readDataAndConvertIds(cardBytes, index, name, idToText, textIdsUsed);
		
		retreatCost = cardBytes[index++];
		weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);

		index = pokemonCategory.readDataAndConvertIds(cardBytes, index, idToText, textIdsUsed);
		
		pokedexNumber = cardBytes[index++];
		unknownByte1 = cardBytes[index++];
		level = cardBytes[index++];
		length = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		weight = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		index = description.readDataAndConvertIds(cardBytes, index, idToText, textIdsUsed);
		
		unknownByte2 = cardBytes[index++];
		
		return index;
	}

	@Override
	public int convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, idToText);
		
		cardBytes[index++] = hp;
		cardBytes[index++] = stage.getValue();
		
		index = prevEvoName.convertToIdsAndWriteData(cardBytes, index, idToText);
		
		index = move1.convertToIdsAndWriteData(cardBytes, index, name, idToText);
		index = move2.convertToIdsAndWriteData(cardBytes, index, name, idToText);
		
		cardBytes[index++] = retreatCost;
		cardBytes[index++] = weakness.getValue();
		cardBytes[index++] = resistance.getValue();

		index = pokemonCategory.convertToIdsAndWriteData(cardBytes, index, idToText);
		
		cardBytes[index++] = pokedexNumber;
		cardBytes[index++] = unknownByte1;
		cardBytes[index++] = level;
		ByteUtils.writeAsShort(length, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(weight, cardBytes, index);
		index += 2;

		index = description.convertToIdsAndWriteData(cardBytes, index, idToText);
		
		cardBytes[index++] = unknownByte2;
		return index;
	}
}
