package data;

import java.util.Set;

import constants.CardDataConstants.*;
import constants.RomConstants;
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
	public int getCardSizeInBytes() 
	{
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public void readNameAndDataAndConvertIds(byte[] cardBytes, int startIndex, Texts ptrToText, Set<Short> ptrsUsed) 
	{
		readCommonNameAndDataAndConvertIds(cardBytes, startIndex, ptrToText, ptrsUsed);
		
		int index = startIndex + Card.CARD_COMMON_SIZE;
		hp = cardBytes[index++];
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		
		// Read the prev evolution
		prevEvoName.readTextFromIds(cardBytes, index, ptrToText, ptrsUsed);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		index = move1.readNameAndDataAndConvertIds(cardBytes, index, name, ptrToText, ptrsUsed);
		index = move2.readNameAndDataAndConvertIds(cardBytes, index, name, ptrToText, ptrsUsed);
		
		retreatCost = cardBytes[index++];
		weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);

        pokemonCategory.readTextFromIds(cardBytes, index, ptrToText, ptrsUsed);
        index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		pokedexNumber = cardBytes[index++];
		unknownByte1 = cardBytes[index++];
		level = cardBytes[index++];
		length = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		weight = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
        description.readTextFromIds(cardBytes, index, ptrToText, ptrsUsed);
        index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		unknownByte2 = cardBytes[index];
	}

	@Override
	public void convertToIdsAndWriteData(byte[] cardBytes, int startIndex, Texts ptrToText) 
	{
		int index = convertCommonToIdsAndWriteData(cardBytes, startIndex, ptrToText);
		
		cardBytes[index++] = hp;
		cardBytes[index++] = stage.getValue();
		
		prevEvoName.convertToIdsAndWriteText(cardBytes, index, ptrToText);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		index = move1.convertToIdsAndWriteData(cardBytes, index, name, ptrToText);
		index = move2.convertToIdsAndWriteData(cardBytes, index, name, ptrToText);
		
		cardBytes[index++] = retreatCost;
		cardBytes[index++] = weakness.getValue();
		cardBytes[index++] = resistance.getValue();

		pokemonCategory.convertToIdsAndWriteText(cardBytes, index, ptrToText);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		cardBytes[index++] = pokedexNumber;
		cardBytes[index++] = unknownByte1;
		cardBytes[index++] = level;
		ByteUtils.writeAsShort(length, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(weight, cardBytes, index);
		index += 2;

		description.convertToIdsAndWriteText(cardBytes, index, ptrToText);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;
		
		cardBytes[index] = unknownByte2;
	}
}
