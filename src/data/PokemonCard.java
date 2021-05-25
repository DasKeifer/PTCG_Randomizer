package data;

import java.util.ArrayList;
import java.util.List;

import constants.CardDataConstants.*;
import data.romtexts.CardName;
import data.romtexts.PokeCategory;
import data.romtexts.PokeDescription;
import rom.Blocks;
import rom.Texts;
import util.ByteUtils;

public class PokemonCard extends Card 
{
	public static final int TOTAL_SIZE_IN_BYTES = 65;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	public static final int MAX_NUM_MOVES = 2;
	
	byte hp; // TODO: non multiples of 10?
	EvolutionStage stage;
	CardName prevEvoName;
	
	private Move[] moves;
	
	byte retreatCost; // TODO: max allowed?
	WeaknessResistanceType weakness; // TODO: Allows multiple? Yes
	WeaknessResistanceType resistance; // TODO: Allows multiple? Yes
	public PokeCategory pokemonCategory; // TODO: Investigate? Any gameplay impact?
	public byte pokedexNumber;
	byte unknownByte1; // TODO: Always 0?
	byte level; // TODO: Investigate No gameplay impact?
	short length; //TODO: One byte is feet, another is inches - separate them // TODO: Investigate No gameplay impact?
	short weight; // TODO: Investigate No gameplay impact?
	PokeDescription description;
	byte unknownByte2; // TODO: At least somewhat tracks with evo stage in asm files - 19 for first stage, 16 for second stage, 0 for final stage?

	public PokemonCard()
	{
		super();
		
		prevEvoName = new CardName(true); // Pokename
		moves = new Move[MAX_NUM_MOVES];
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex] = new Move();
		}
		pokemonCategory = new PokeCategory();
		description = new PokeDescription();
	}
	
	public PokemonCard(PokemonCard toCopy)
	{
		super(toCopy);
		
		hp = toCopy.hp;
		stage = toCopy.stage;
		prevEvoName = new CardName(toCopy.prevEvoName);
		moves = new Move[MAX_NUM_MOVES];
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex] = new Move(toCopy.moves[moveIndex]);
		}
		retreatCost = toCopy.retreatCost;
		weakness = toCopy.weakness;
		resistance = toCopy.resistance;
		pokemonCategory = new PokeCategory(toCopy.pokemonCategory);
		pokedexNumber = toCopy.pokedexNumber;
		unknownByte1 = toCopy.unknownByte1;
		level = toCopy.level;
		length = toCopy.length;
		weight = toCopy.weight;
		description = new PokeDescription(toCopy.description);
		unknownByte2 = toCopy.unknownByte2;
	}
	
	@Override
	protected CardName createCardName()
	{
		return new CardName(true); // a pokename
	}
	
	public PokemonCard copy()
	{
		return new PokemonCard(this);
	}

	public List<Move> getAllMovesIncludingEmptyOnes()
	{
		ArrayList<Move> movesList = new ArrayList<>();
		for (Move move : moves)
		{
			movesList.add(new Move(move));
		}
		return movesList;
	}
	
	public int getNumMoves()
	{
		int numMoves = 0;
		for (Move move : moves)
		{
			if (!move.isEmpty())
			{
				numMoves++;
			}
		}
		return numMoves;
	}
	
	public void setMove(Move move, int moveSlot)
	{
		try
		{
			moves[moveSlot] = new Move(move);
		}
		catch (IndexOutOfBoundsException ioobe)
		{
			throw new IllegalArgumentException("Bad move slot " + moveSlot + "was passed!");
		}
	}
	
	public void setMoves(List<Move> newMoves)
	{
		if (newMoves.size() != moves.length)
		{
			throw new IllegalArgumentException("Bad number of moves (" + newMoves.size() + ") was passed!");
		}
		
		for (int moveIndex = 0; moveIndex < moves.length; moveIndex++)
		{
			moves[moveIndex] = new Move(newMoves.get(moveIndex));
		}
	}
	
	public void sortMoves()
	{
		Move tempMove;
		boolean needsSwap;
		for (int moveIndex = 0; moveIndex < moves.length - 1; moveIndex++)
		{
			needsSwap = false;
			// Move empty moves to the end
			if (moves[moveIndex].isEmpty() || moves[moveIndex + 1].isEmpty())
			{
				if (moves[moveIndex].isEmpty() && !moves[moveIndex + 1].isEmpty() )
				{
					needsSwap = true;
				}
			}
			// Move poke powers first
			else if (!moves[moveIndex].isPokePower() && moves[moveIndex + 1].isPokePower())
			{
				needsSwap = true;
			}
			else
			{
				int numColorless1 = moves[moveIndex].getCost(EnergyType.COLORLESS);
				int numColorless2 = moves[moveIndex + 1].getCost(EnergyType.COLORLESS);
				int numNonColorless1 = moves[moveIndex].getNonColorlessEnergyCosts();
				int numNonColorless2 = moves[moveIndex + 1].getNonColorlessEnergyCosts();
				
				// Move higher total energies last
				if (numColorless1 + numNonColorless1 > numColorless2 + numNonColorless2)
				{
					needsSwap = true;
				}
				else if (numColorless1 + numNonColorless1 == numColorless2 + numNonColorless2)
				{
					// If equal num, move more non-colorless last
					if (numNonColorless1 > numNonColorless2)
					{
						needsSwap = true;
					}
					else if (numNonColorless1 == numNonColorless2)
					{
						// If equal move higher damage last
						if (moves[moveIndex].damage > moves[moveIndex + 1].damage)
						{
							needsSwap = true;
						}
						// If equal, moves with effects last
						else if (moves[moveIndex].damage == moves[moveIndex + 1].damage &&
								!moves[moveIndex].description.isEmpty() && moves[moveIndex + 1].description.isEmpty())
						{
							needsSwap = true;
						}
					}
				}
			}
				
			if (needsSwap)
			{
				tempMove = moves[moveIndex];
				moves[moveIndex] = moves[moveIndex + 1];
				moves[moveIndex + 1] = tempMove;
				moveIndex = 0; // restart sort loop
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString() + 
				"\nPokedex Number = " + pokedexNumber + 
				"\nDesciption = " + description.toString() + 
				"\nHP = " + hp +
				"\nStage = " + stage + 
				"\nPrevEvolution = " + prevEvoName.toString() +
				"\nRetreatCost = " + retreatCost +
				"\nWeakness = " + weakness +
				"\nResistance = " + resistance  +
				"\nMoves");

		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			builder.append("\n" + moves[moveIndex].toString());
		}
		return builder.toString();
	}
	
	@Override
	public int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		commonReadAndConvertIds(cardBytes, startIndex, idToText);
		
		int index = startIndex + Card.CARD_COMMON_SIZE;
		hp = cardBytes[index++];
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		
		// Read the prev evolution
		index = prevEvoName.readDataAndConvertIds(cardBytes, index, idToText);

		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			index = moves[moveIndex].readDataAndConvertIds(cardBytes, index, name, idToText);
		}
		
		retreatCost = cardBytes[index++];
		weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);

		index = pokemonCategory.readDataAndConvertIds(cardBytes, index, idToText);
		
		pokedexNumber = cardBytes[index++];
		unknownByte1 = cardBytes[index++];
		level = cardBytes[index++];
		length = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		weight = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		index = description.readDataAndConvertIds(cardBytes, index, idToText);
		
		unknownByte2 = cardBytes[index++];
		
		return index;
	}
	
	@Override
	public void finalizeDataForAllocating(Texts texts, Blocks blocks)
	{
		commonFinalizeDataForAllocating(texts);
		
		prevEvoName.finalizeAndAddTexts(texts);
		pokemonCategory.finalizeAndAddTexts(texts);
		description.finalizeAndAddTexts(texts);

		sortMoves();
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex].finalizeDataForAllocating(texts, blocks, name.toString());
		}
	}

	@Override
	public int writeData(byte[] cardBytes, int startIndex) 
	{
		int index = commonWriteData(cardBytes, startIndex);
		
		cardBytes[index++] = hp;
		cardBytes[index++] = stage.getValue();
		
		index = prevEvoName.writeTextId(cardBytes, index);

		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			index = moves[moveIndex].writeData(cardBytes, index);
		}
		
		cardBytes[index++] = retreatCost;
		cardBytes[index++] = weakness.getValue();
		cardBytes[index++] = resistance.getValue();

		index = pokemonCategory.writeTextId(cardBytes, index);
		
		cardBytes[index++] = pokedexNumber;
		cardBytes[index++] = unknownByte1;
		cardBytes[index++] = level;
		ByteUtils.writeAsShort(length, cardBytes, index);
		index += 2;
		ByteUtils.writeAsShort(weight, cardBytes, index);
		index += 2;

		index = description.writeTextId(cardBytes, index);
		
		cardBytes[index++] = unknownByte2;
		return index;
	}
}
