package randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EnergyType;
import data.Cards;
import data.Move;
import data.PokemonCard;
import data.PokemonCard.MoveCategories;
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

	// Enum used for convenience when randomizing the moves in this class
	private enum RandomizerMoveCategory
	{
		EMPTY, MOVE, ATTACK, POKE_POWER, DAMAGING_ATTACK
	}
	
	public void randomize(long nextSeed, Settings settings)
	{
		boolean changedMoves = false;
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		
		// TODO: Moves seems to be generic term. Attacks and PokePowers are specific terms. Do some renaming to align with this
		
		// TODO: Change Shuffle to an option for randomization to use each one once before moving on
		
		// Always do the first one. Switch once the rest of the logic triggers
		RandomizationStrategy moveRandStrat = settings.getAttacks().getRandomizationStrat();
		if (RandomizationStrategy.UNCHANGED != moveRandStrat)
		{
			if (RandomizationStrategy.INVALID == moveRandStrat)
			{
				throw new IllegalArgumentException("INVALID Randomization Strategy recieved for Attacks!");
			}
			else if (RandomizationStrategy.GENERATED == moveRandStrat)
			{
				throw new IllegalArgumentException("GENERATED Randomization Strategy for Attacks is not yet implemented!");
			}
			
			changedMoves = true;
	        shuffleOrRandomizePokemonMoves(nextSeed, pokes, pokes, settings); // TODO pass in original pokes once type randomization is done
		}
		// Otherwise, no randomization being done for Attacks/Movesets
		// Still increment the seed to stay consistent 
		nextSeed++;
		
		// Now randomize poke powers separately if that is selected
		if (!settings.getPokePowers().isIncludeWithMoves())
		{
			moveRandStrat = settings.getPokePowers().getRandomizationStrat();
			if (RandomizationStrategy.UNCHANGED != moveRandStrat)
			{
				if (RandomizationStrategy.INVALID == moveRandStrat)
				{
					throw new IllegalArgumentException("INVALID Randomization Strategy recieved for Poke Powers!");
				}
				else if (RandomizationStrategy.GENERATED == moveRandStrat)
				{
					throw new IllegalArgumentException("GENERATED Randomization Strategy for Poke Powers is not yet implemented!");
				}

				changedMoves = true;
		        shuffleOrRandomizePokemonMoves(nextSeed, pokes, pokes, settings); // TODO pass in original pokes once type randomization is done
			}
			// Otherwise, no randomization being done for Poke Powers
		}
		// Still increment the seed to stay consistent 
		nextSeed++;
		
		// See if we need to tweak the move types at all
		MoveTypeChanges moveTypeChanges = settings.getAttacks().getMoveTypeChanges();
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
			printPokemonMoveSetsTable();
		}
	}

	// TODO Move these outside of this class into a "tweaks" class?
	/******************** Changing Move Type ************************************/
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

	/******************** Determine Number of Moves ************************************/
	public static double[] getNumMovesPercentages(Cards<PokemonCard> pokes, MoveCategories moveCategory)
	{
		int[] numPerCount = new int[PokemonCard.MAX_NUM_MOVES];
		Arrays.fill(numPerCount, 0);
		
		for (PokemonCard card : pokes.iterable())
		{
			numPerCount[card.getNumMoves(moveCategory)] += 1;
		}
		
		return MathUtils.convertNumbersToPercentages(numPerCount);
	}
	
	public static Map<PokemonCard, List<RandomizerMoveCategory>> getMoveTypesPerPokemon(Cards<PokemonCard> pokes, boolean groupPowersAndAttacks)
	{
		Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap = new HashMap<PokemonCard, List<RandomizerMoveCategory>>();
		for (PokemonCard card : pokes.iterable())
		{
			List<RandomizerMoveCategory> moveTypesList = new ArrayList<>();
			for (Move move : card.getAllMoves())
			{
				if (move.isEmpty())
				{
					moveTypesList.add(RandomizerMoveCategory.EMPTY);
				}
				else if (groupPowersAndAttacks)
				{
					moveTypesList.add(RandomizerMoveCategory.MOVE);
				}
				else if (move.isPokePower())
				{
					moveTypesList.add(RandomizerMoveCategory.POKE_POWER);
				}
				else
				{
					moveTypesList.add(RandomizerMoveCategory.ATTACK);
				}
			}
			
			cardMovesMap.put(card, moveTypesList);
		}
		
		return cardMovesMap;
	}

	public void adjustForDamagingMoves(Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap)
	{
		for (List<RandomizerMoveCategory> pokeMoveTypes : cardMovesMap.values())
		{
			boolean setDamagingAttack = false;
			for (int moveIndex = 0; moveIndex < pokeMoveTypes.size(); moveIndex++)
			{
				if (RandomizerMoveCategory.ATTACK == pokeMoveTypes.get(moveIndex) || RandomizerMoveCategory.MOVE == pokeMoveTypes.get(moveIndex))
				{
					pokeMoveTypes.set(moveIndex, RandomizerMoveCategory.DAMAGING_ATTACK);
					setDamagingAttack = true;
					break;
				}
			}

			// If it failed, just set the first empty move to a damaging move
			if (!setDamagingAttack)
			{
				int moveIndex = 0;
				for ( /*already set*/; moveIndex < pokeMoveTypes.size(); moveIndex++)
				{
					if (RandomizerMoveCategory.EMPTY == pokeMoveTypes.get(moveIndex))
					{
						pokeMoveTypes.set(moveIndex, RandomizerMoveCategory.DAMAGING_ATTACK);
						setDamagingAttack = true;
						break;
					}
				}
				
				// If no empty move was found, just set the last index to a damaging attack
				// TODO: If we can only have one poke power per card, this should never happen
				// if we can, then maybe have a way to disable this
				if (!setDamagingAttack)
				{
					pokeMoveTypes.set(moveIndex, RandomizerMoveCategory.DAMAGING_ATTACK);
				}
			}
		}
	}

	// TODO: refactor to align with new approach when support for altering num moves per poke is added
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

	/******************** Generate Movesets ************************************/
	public void shuffleOrRandomizePokemonMoves(
			long nextSeed,
			Cards<PokemonCard> pokes,
			Cards<PokemonCard> originalPokesToTakeMovesFrom,
			Settings settings
	)
	{		
		// If we want to match the move to the poke type,
		if (settings.getMoves(moveCategoryRandomizing).isRandomizationWithinType())
		{
			// Do one energy type at a time
			for (CardType pokeType : CardType.pokemonValues())
			{
				// Get the pokemon of this type and the moves if we are set
				// to match the types
				Cards<PokemonCard> typeCards = pokes.getCardsOfCardType(pokeType);
				List<Move> movePool = originalPokesToTakeMovesFrom.getCardsOfCardType(pokeType).getAllMoves();
				randomizeMoveSets(nextSeed, typeCards, movePool, settings);
			}	
		}
		else
		{
			// Otherwise get all the moves and do them at once
			List<Move> movePool = originalPokesToTakeMovesFrom.getAllMoves();
			randomizeMoveSets(nextSeed, pokes, movePool, settings);
		}
		
	}
	
	// Move pool is separate from pokes to support randomizing card type prior to this
	// Shoot this doesn't work for intermixing powers and moves for types... Need to rethink some
	// Possibly make this slightly lower level - pass in cardMovesMaps and pokepowers, moves, or attacks and perform one pass?
	// 
	public void randomizeMoveSets(long nextSeed, Cards<PokemonCard> pokes, List<Move> movePool, Settings settings)
	{
		// Step 1: Get the current pokemon to move categories for each move map (read from ROM for now - in future add more options). 
		Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap = getMoveTypesPerPokemon(pokes, settings.getPokePowers().isIncludeWithMoves());
		
		// Step 2: adjust for and assign damaging moves
		// First create move list so we can assign damaging moves appropriately
		List<Move> currentMovePool = new ArrayList<>();
		if (settings.getAttacks().isMovesForceOneDamaging())
		{
			// Step 2a: Go through the poke-move map and change a move to a damaging move
			adjustForDamagingMoves(cardMovesMap);

			// Step 2b: Get set of damaging moves from the pokemon
			addMovesToPool(movePool, RandomizerMoveCategory.DAMAGING_ATTACK, currentMovePool);
			
			// Step 2c: Assign the moves
			assignMovesToPokemon(nextSeed, cardMovesMap, currentMovePool, RandomizerMoveCategory.DAMAGING_ATTACK, 
					RandomizationStrategy.SHUFFLE == settings.getAttacks().getRandomizationStrat()); // If its shuffle mode or not
		}
		
		// Always increment for consistency
		nextSeed++;

		
		// TODO: add some logic for shuffle mode so we don't run out of moves. Perhaps in the addMovesToPool function?
		
		// Step 3: Handle assigning the rest of the moves
		if (settings.getPokePowers().isIncludeWithMoves())
		{
			addMovesToPool(movePool, RandomizerMoveCategory.MOVE, currentMovePool); // Non Damaging attacks and poke powers separate for shuffled?
			assignMovesToPokemon(nextSeed, cardMovesMap, currentMovePool, RandomizerMoveCategory.MOVE, 
					RandomizationStrategy.SHUFFLE == settings.getAttacks().getRandomizationStrat()); // If its shuffle mode or not
		}
		else
		{
			addMovesToPool(movePool, RandomizerMoveCategory.ATTACK, currentMovePool); // Non Damaging attacks for shuffled?
			assignMovesToPokemon(nextSeed, cardMovesMap, currentMovePool, RandomizerMoveCategory.ATTACK, 
					RandomizationStrategy.SHUFFLE == settings.getAttacks().getRandomizationStrat()); // If its shuffle mode or not

			// Clear to to get ready to assign poke powers
			currentMovePool.clear();
			addMovesToPool(movePool, RandomizerMoveCategory.POKE_POWER, currentMovePool);
			assignMovesToPokemon(nextSeed + 1, cardMovesMap, currentMovePool, RandomizerMoveCategory.POKE_POWER, 
					RandomizationStrategy.SHUFFLE == settings.getPokePowers().getRandomizationStrat()); // If its shuffle mode or not
		}
		
		// Always increment the same amount
		nextSeed += 2;
		
		// Finally set all the empty moves to be empty
		assignEmptyMovesToPokemon(cardMovesMap);
	}

	public void addMovesToPool(List<Move> possibleMoves, RandomizerMoveCategory moveTypeToAdd, List<Move> moveSubset)
	{
		for (Move move : possibleMoves)
		{
			if (RandomizerMoveCategory.MOVE == moveTypeToAdd && !move.isEmpty() ||
				RandomizerMoveCategory.POKE_POWER == moveTypeToAdd && move.isPokePower() || 
				(move.isAttack() &&
						(RandomizerMoveCategory.ATTACK == moveTypeToAdd ||
								(RandomizerMoveCategory.DAMAGING_ATTACK == moveTypeToAdd && move.damage > 0))))
			{
				moveSubset.add(move);
			}
		}
	}
	
	public void assignMovesToPokemon(
			long seed, 
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap, 
			List<Move> movePool, 
			RandomizerMoveCategory moveTypeAssign,
			boolean shuffle
	)
	{
		// Create and seed the randomizer
		Random rand = new Random(seed);
		
		for (Entry<PokemonCard, List<RandomizerMoveCategory>> cardEntry : cardMovesMap.entrySet())
		{
			for (int moveIndex = 0; moveIndex < cardEntry.getValue().size(); moveIndex++)
			{
				if (cardEntry.getValue().get(moveIndex) == moveTypeAssign)
				{
					//Call helper
					randomizeMoveAtIndex(rand, cardEntry.getKey(), moveIndex, movePool, shuffle);
				}
			}
		}
	}
	
	public void assignEmptyMovesToPokemon(Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap)
	{
		for (Entry<PokemonCard, List<RandomizerMoveCategory>> cardEntry : cardMovesMap.entrySet())
		{
			for (int moveIndex = 0; moveIndex < cardEntry.getValue().size(); moveIndex++)
			{
				if (cardEntry.getValue().get(moveIndex) == RandomizerMoveCategory.EMPTY)
				{
					setMoveEmpty(cardEntry.getKey(), moveIndex);
				}
			}
		}
	}

	private void randomizeMoveAtIndex(
			Random rand,
			PokemonCard poke, 
			int moveIndex,
			List<Move> moves,
			boolean shuffle
	)
	{		
		// Determine which random move to use
		int randMoveIndex = rand.nextInt(moves.size());
	
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

	private static void setMoveEmpty(PokemonCard poke, int moveIndex)
	{
		poke.setMove(Move.EMPTY_MOVE, moveIndex);
	}
	
	/******************** Logging/Debug ************************************/
	public void printPokemonMoveSetsTable()
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
}
