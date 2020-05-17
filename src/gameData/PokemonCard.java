package gameData;

import constants.CardConstants.*;
import constants.CardDataConstants.*;
import util.IoUtils;

public class PokemonCard extends Card 
{
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = 65 - CARD_COMMON_SIZE;
	
	byte hp; // TODO: non multiples of 10?
	EvolutionStage stage;
	CardId prevEvolution;
	
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
	public int readData(byte[] cardBytes, int startIndex) 
	{
		int index = readCommonData(cardBytes, startIndex);
		
		hp = cardBytes[index++];
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		prevEvolution = CardId.readFromByte(cardBytes[index++]);
		
		move1 = new Move();
		index = move1.readData(cardBytes, index);
		move2 = new Move();
		index = move2.readData(cardBytes, index);
		
		retreatCost = cardBytes[index++];
		weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		pokemonCategory = IoUtils.readShort(cardBytes, index);
		index += 2;
		pokedexNumber = cardBytes[index++];
		unknownByte1 = cardBytes[index++];
		level = cardBytes[index++];
		length = IoUtils.readShort(cardBytes, index);
		index += 2;
		weight = IoUtils.readShort(cardBytes, index);
		index += 2;
		description = IoUtils.readShort(cardBytes, index);
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
		cardBytes[index++] = prevEvolution.getValue();
		
		index = move1.writeData(cardBytes, index);
		index = move2.writeData(cardBytes, index);
		
		cardBytes[index++] = retreatCost;
		cardBytes[index++] = weakness.getValue();
		cardBytes[index++] = resistance.getValue();
		IoUtils.writeShort(pokemonCategory, cardBytes, index);
		index += 2;
		cardBytes[index++] = pokedexNumber;
		cardBytes[index++] = unknownByte1;
		cardBytes[index++] = level;
		IoUtils.writeShort(length, cardBytes, index);
		index += 2;
		IoUtils.writeShort(weight, cardBytes, index);
		index += 2;
		IoUtils.writeShort(description, cardBytes, index);
		index += 2;
		cardBytes[index++] = unknownByte2;
		
		return index;
	}
}
