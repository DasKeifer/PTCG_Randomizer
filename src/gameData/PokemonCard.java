package gameData;

import constants.CardDataConstants.*;
import util.ByteUtils;

public class PokemonCard extends Card 
{
	public static final int TOTAL_SIZE_IN_BYTES = 65;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	byte hp; // TODO: non multiples of 10?
	EvolutionStage stage;
	short prevEvoName;
	
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
	short description; // Shouldn't need to change - No gameplay impact
	byte unknownByte2;
	
	@Override
	public String toString()
	{
		return super.toString() + 
				"\nPokedex Number = " + pokedexNumber + 
				"\nHP = " + hp +
				"\nStage = " + stage +
				"\nPrevEvolition = " + prevEvoName +
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
	public int readData(byte[] cardBytes, int startIndex) 
	{
		int index = readCommonData(cardBytes, startIndex);
		
		hp = cardBytes[index++];
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		prevEvoName = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		move1 = new Move();
		index = move1.readData(cardBytes, index);
		move2 = new Move();
		index = move2.readData(cardBytes, index);
		
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
		description = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		unknownByte2 = cardBytes[index++];
		
		return index;
	}

	@Override
	public int writeData(byte[] cardBytes, int startIndex) 
	{
		int index = writeCommonData(cardBytes, startIndex);
		
		cardBytes[index++] = hp;
		cardBytes[index++] = stage.getValue();
		ByteUtils.writeAsShort(prevEvoName, cardBytes, index);
		index += 2;
		
		index = move1.writeData(cardBytes, index);
		index = move2.writeData(cardBytes, index);
		
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
		ByteUtils.writeAsShort(description, cardBytes, index);
		index += 2;
		cardBytes[index++] = unknownByte2;
		
		return index;
	}
}
