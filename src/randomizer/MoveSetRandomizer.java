package randomizer;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EnergyType;
import data.Cards;
import data.Move;
import data.PokemonCard;
import randomizer.Settings.MoveTypeChanges;
import randomizer.Settings.RandomizationStrategy;
import rom.RomData;
import util.Logger;
import util.MathUtils;

public class MoveSetRandomizer {
	private RomData romData;
	private Logger logger;
	
	public MoveSetRandomizer(RomData inRomData, Logger inLogger)
	{
		romData = inRomData;
		logger = inLogger;
	}
	
	public void randomize(long nextSeed, Settings settings)
	{
		boolean changedMoves = false;
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		
		// TODO get from settings so we can randomize it. For now we just keep
		// the same card values
		Map<CardId, Integer> numMovesPerPokemon = getNumMovesPerPokemon(pokes);

		RandomizationStrategy moveRandStrat = settings.getMoves().getMovesStrat();
		boolean powersWithMoves = settings.getPokePowers().isIncludeWithMoves();
		
		if (RandomizationStrategy.UNCHANGED != moveRandStrat)
		{
			if (RandomizationStrategy.INVALID == moveRandStrat)
			{
				throw new IllegalArgumentException("INVALID Randomization Strategy recieved for Poke Moves!");
			}
			else if (RandomizationStrategy.GENERATED == moveRandStrat)
			{
				throw new IllegalArgumentException("GENERATED Randomization Strategy for Poke Moves is not yet implemented!");
			}
			// Shuffle or Randomize
			changedMoves = true;
			
			// TODO: Optionally include PokePowers
	        shuffleOrRandomizePokemonMoves(nextSeed++,
	        		RandomizationStrategy.SHUFFLE == moveRandStrat, // Shuffle not Random
	        		pokes, numMovesPerPokemon, settings.getMoves().isMovesAttacksWithinType(), 
	        		1); // Num non poke power moves
		}
		// else no randomization being done for moves - nothing to do here
		
		// If Poke Powers weren't included with the moves, we need to do them separately now
		if (!powersWithMoves)
		{
			RandomizationStrategy powersRandStrat = settings.getPokePowers().getMovesPokePowerStrat();			
			if (RandomizationStrategy.UNCHANGED != powersRandStrat)
			{
				if (RandomizationStrategy.INVALID == powersRandStrat)
				{
					throw new IllegalArgumentException("INVALID Randomization Strategy recieved for Poke Powers!");
				}
				else if (RandomizationStrategy.GENERATED == powersRandStrat)
				{
					throw new IllegalArgumentException("GENERATED Randomization Strategy for Poke Powers is not a planned feature!");
				}
				// TODO: Shuffle or Randomize
				changedMoves = true;
			}
		}
		
		// See if we need to tweak the move types at all
		MoveTypeChanges moveTypeChanges = settings.getMoves().getMoveTypeChanges();
		if (MoveTypeChanges.UNCHANGED != moveTypeChanges)
		{
			if (MoveTypeChanges.INVALID == moveTypeChanges)
			{
				throw new IllegalArgumentException("INVALID Move Type Changes recieved!");
			}

			changedMoves = true;
			if (MoveTypeChanges.MATCH_CARD_TYPE == moveTypeChanges)
			{
				makeAllMovesMatchCardType();
			}
			// Make all Colorless
			else
			{
				makeAllMovesColorless();
			}
		}
		
		// If we changed anything, print out the new data
		if (changedMoves)
		{
			// Sort the moves now so they will log nicely
			for (PokemonCard card : pokes.iterable())
			{
				card.sortMoves();
			}
			printPokemonMovesTable();
		}
	}

	public void makeAllMovesMatchCardType()
	{
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		
		// Do one energy type at a time
		for (CardType pokeType : CardType.pokemonValues())
		{				
			// Determine the number of moves per pokemon for this type
			changeAllMovesTypes(pokes.getCardsOfCardType(pokeType), 
					pokeType.convertToEnergyType());
		}	
	}
	
	public void makeAllMovesColorless()
	{
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		changeAllMovesTypes(pokes, EnergyType.COLORLESS);
	}
	
	public void printPokemonMovesTable()
	{
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		
		final int idIndex = 0;
		final int nameIndex = 1;
		final int movesStartIndex = 2;
		final int numIndexes = 8;
		String[] titles = {" ID ", " Name ", " Move 1 ", " Cost ", " Damage ", " Move 2 ", " Cost ", " Damage "};
		
		// Determine length of each of the columns columns
		int[] fieldsMaxLengths = new int[numIndexes];
		
		// Length of the titles
		Logger.findMaxStringLengths(fieldsMaxLengths, titles);
		
		// Lengths for each pokemon card
		String[] fields = new String[numIndexes];
		for (PokemonCard card : pokes.iterable())
		{
			fields[idIndex] = card.id.toString();
			fields[nameIndex] = card.name.toString();
			
			int index = movesStartIndex;
			for (Move move : card.getAllMoves())
			{
				fields[index++] = move.name.toString();
				fields[index++] = move.getEnergyCostString(true, ", "); // true = Abbreviated types
				fields[index++] = move.getDamageString();
			}

			Logger.findMaxStringLengths(fieldsMaxLengths, fields);
		}

		// Create the format string for each row
		String rowFormat = Logger.createTableFormatString(fieldsMaxLengths, "-", "-", "-", "", "", "-");

		// Determine total line length and create a separator line
		int totalLength = numIndexes + 1; // for the "|"
		for (int lengthIdx = 0; lengthIdx < numIndexes; lengthIdx++)
		{
			totalLength += fieldsMaxLengths[lengthIdx];
		}
		String separator = Logger.createSeparatorLine(totalLength);
		
		// Create a title row
		String tableName = Logger.createTableTitle("Pokemon Modified Movesets", totalLength);
		
		// Print header
		logger.println(separator);
		logger.println(tableName);
		logger.printf(rowFormat, (Object[])titles);
		logger.println(separator);
		
		// Log each row
		String[] rowData = new String[numIndexes];
		for (PokemonCard card : pokes.iterable())
		{
			rowData[idIndex] = card.id.toString();
			rowData[nameIndex] = card.name.toString();

			int index = movesStartIndex;
			for (Move move : card.getAllMoves())
			{
				if (move.isEmpty())
				{
					rowData[index++] = "-";
					rowData[index++] = "-";
					rowData[index++] = "-";
				}
				else
				{
					rowData[index++] = move.name.toString();
					rowData[index++] = move.getEnergyCostString(true, ", "); // true = abbreviated types
					rowData[index++] = move.getDamageString();
				}
			}
			logger.printf(rowFormat, (Object[])rowData);
		}
		
		// Print a final separator
		logger.println(separator);
	}
	
	public static double[] getNumMovesPercentages(Cards<PokemonCard> pokes)
	{
		int[] numPerCount = new int[PokemonCard.MAX_NUM_MOVES];
		Arrays.fill(numPerCount, 0);
		
		for (PokemonCard card : pokes.iterable())
		{
			numPerCount[card.getNumMoves()] += 1;
		}
		
		return MathUtils.convertNumbersToPercentages(numPerCount);
	}
	
	public static Map<CardId, Integer> getNumMovesPerPokemon(Cards<PokemonCard> pokes)
	{
		Map<CardId, Integer> cardMovesMap = new EnumMap<>(CardId.class);
		for (PokemonCard card : pokes.iterable())
		{
			cardMovesMap.put(card.id, card.getNumMoves());
		}
		return cardMovesMap;
	}
	
	public Map<CardId, Integer> getRandNumMovesPerPokemon(
			Random rand,
			Cards<PokemonCard> pokes, 
			double[] percentWithNumMoves
	)
	{
		// Plus one since 0 moves is an option
		int numMovesPossibilities = PokemonCard.MAX_NUM_MOVES + 1;
		
		// Sanity check
		if (percentWithNumMoves.length != numMovesPossibilities)
		{
			throw new IllegalArgumentException("Passed percentages for numbers of moves length (" + 
						percentWithNumMoves.length + " is not the expected number of " + numMovesPossibilities);
		}
		
		// FOr convenience/optimization
		int numCards = pokes.count();
		
		// Determine how many cards will have what number of moves
		int[] numCardsWithNumMoves = MathUtils.convertPercentageToIntValues(percentWithNumMoves, numCards);
	
		// Start by defaulting all to max number
		Map<CardId, Integer> cardMovesMap = new EnumMap<>(CardId.class);
		for (PokemonCard card : pokes.iterable())
		{
			cardMovesMap.put(card.id, PokemonCard.MAX_NUM_MOVES);
		}
		
		CardId randCardId;
		List<PokemonCard> pokeList = pokes.toList();
		// We use max num moves since we default everyone to MAX NUM already, we don't need to
		// check that case
		for (int numMoves = 0; numMoves < PokemonCard.MAX_NUM_MOVES; numMoves++)
		{
			// For each card that should have this number of moves
			for (int count = 0; count < numCardsWithNumMoves[numMoves]; count++)
			{
				// Get a random card and remove it from the available pool then assign
				// the number of moves to it
				randCardId = pokeList.remove(rand.nextInt(pokeList.size())).id;
				cardMovesMap.put(randCardId, numMoves);
			}
		}
		
		return cardMovesMap;
	}

	public Map<CardId, Integer> getRandNumMovesPerPokemonByType(
			long seed,
			Cards<PokemonCard> pokes, 
			Map<CardType, double[]> percentWithNumMovesByType
	)
	{
		Map<CardId, Integer> numMovesPerPoke = new EnumMap<>(CardId.class);

		// Create and seed random generator
		Random rand = new Random(seed);
		
		// Do one energy type at a time
		for (CardType pokeType : CardType.pokemonValues())
		{				
			// Determine the number of moves per pokemon for this type
			numMovesPerPoke.putAll(
					getRandNumMovesPerPokemon(
							rand,
							pokes.getCardsOfCardType(pokeType), 
							percentWithNumMovesByType.get(pokeType)));
		}	
		
		return numMovesPerPoke;
	}

	public void shuffleOrRandomizePokemonMoves(
			long nextSeed,
			boolean shuffle,
			Cards<PokemonCard> pokes,
			Map<CardId, Integer> numMovesPerPoke,
			boolean withinTypes,
			int numNonPokePower
	)
	{		
		// If we want to match the move to the poke type,
		if (withinTypes)
		{
			// Do one energy type at a time
			for (CardType pokeType : CardType.pokemonValues())
			{
				// Get the pokemon of this type and the moves if we are set
				// to match the types
				Cards<PokemonCard> typeCards = pokes.getCardsOfCardType(pokeType);
				List<Move> typeMove = typeCards.getAllMoves();
				shuffleOrRandomizePokemonMovesHelper(nextSeed, shuffle, typeCards, numMovesPerPoke, numNonPokePower, typeMove);
			}	
		}
		else
		{
			// Otherwise get all the moves and do them at once
			List<Move> typeMove = pokes.getAllMoves();
			shuffleOrRandomizePokemonMovesHelper(nextSeed, shuffle, pokes, numMovesPerPoke, numNonPokePower, typeMove);
		}
		
	}
	
	private void shuffleOrRandomizePokemonMovesHelper(
			long seed,
			boolean shuffle,
			Cards<PokemonCard> pokes,
			Map<CardId, Integer> numMovesPerPoke,
			int numNonPokePower, 
			List<Move> moves)
	{
		// Create and seed the randomizer
		Random rand = new Random(seed);
		
		// Assign moves one at a time so if we are shuffling and run out of 
		// non-poke powers, they will be more spread still
		for (int moveIndex = 0; moveIndex < PokemonCard.MAX_NUM_MOVES; moveIndex++)
		{
			// For each poke
			for (PokemonCard poke : pokes.iterable())
			{
				// See if we need to assign a move or set it to empty
				if (numMovesPerPoke.get(poke.id) > moveIndex)
				{
					shuffleOrRandomizeMoveAtIndex(rand, poke, moveIndex, moves, shuffle, numNonPokePower > moveIndex);
				}
				else
				{
					setMoveEmpty(poke, moveIndex);
				}
			}
		}
	}
	
	private static void setMoveEmpty(PokemonCard poke, int moveIndex)
	{
		poke.setMove(Move.EMPTY_MOVE, moveIndex);
	}
	
	private void shuffleOrRandomizeMoveAtIndex(
			Random rand,
			PokemonCard poke, 
			int moveIndex,
			List<Move> moves,
			boolean shuffle,
			boolean forceNonPokePower
	)
	{		
		// Determine which random move to use
		int randMoveIndex = rand.nextInt(moves.size());
		if (forceNonPokePower)
		{
			int maxTries = 10000;
			int tries = 0;
			while (moves.get(randMoveIndex).isPokePower())
			{
				randMoveIndex = rand.nextInt(moves.size());
				if (tries > maxTries)
				{
					System.err.println("Failed to find a non pokemon power - using a poke power in its place");
					break;
				}
			}
		}
	
		// If its shuffle, delete it so its not reused
		if (shuffle)
		{
			poke.setMove(moves.remove(randMoveIndex), moveIndex);
		}
		// Otherwise leave it - it can be reused
		else
		{
			poke.setMove(moves.get(randMoveIndex), moveIndex);
		}
	}

	private void changeAllMovesTypes(Cards<PokemonCard> pokes, EnergyType type)
	{
		List<Move> moves;
		byte nonColorlessCost;
		byte colorlessCost;
		for (PokemonCard poke : pokes.iterable())
		{
			moves = poke.getAllMoves();
			for (Move move : moves)
			{
				// Get the current data and then clear it
				colorlessCost = move.getCost(EnergyType.COLORLESS);
				nonColorlessCost = move.getNonColorlessEnergyCosts();
				move.clearCosts();
				
				// If we are setting to colorless, we need to add the
				// two together
				if (type == EnergyType.COLORLESS)
				{
					move.setCost(EnergyType.COLORLESS, (byte) (colorlessCost + nonColorlessCost));
				}
				// Otherwise set the colorless back and set the non colorless
				// to the new type
				else
				{
					move.setCost(EnergyType.COLORLESS, colorlessCost);
					move.setCost(type, nonColorlessCost);
				}
			}
			
			// Copy the moves back over
			poke.setMoves(moves);
		}
	}
}
