package gameData;

import constants.CardConstants.*;
import constants.CardDataConstants.*;

public class PokemonCard implements GameData 
{
	CardType type;
	int gfx; // Card art
	int name; // No gameplay impact
	CardRarity rarity;

	// IMPORTANT! in the data the set and pack are stored in one byte:
	// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
	CardSet set;
	BoosterPack pack;
	CardId id; // This is used to calculate the offset of the card data and is used to reference other cards
	byte hp; // TODO: non multiples of 10?
	EvolutionStage stage;
	CardId prevEvolution;
	
	Move move1;
	Move move2;
	
	byte retreatCost; // TODO: max allowed?
	WeaknessResistanceType weakness; // TODO: Allows multiple?
	WeaknessResistanceType resistance; // TODO: Allows multiple?
	int pokemonCategory; // TODO: Investigate - i.e cocoon, hairy bug, etc. Shouldn't need to change
	byte pokedexNumber;
	byte unknownByte1;
	byte level; // TODO: Investigate No gameplay impact?
	int length; //One byte is feet, another is inches // TODO: Investigate No gameplay impact?
	int weight; // TODO: Investigate No gameplay impact?
	int description; // Shouldn't need to change - No gameplay impact
	byte unknownByte2;
	
	@Override
	public void readData(byte[] rom, int startIndex) {
		// TODO:
	}

	@Override
	public void writeData(byte[] rom, int startIndex) {
		// TODO:
	}
}
