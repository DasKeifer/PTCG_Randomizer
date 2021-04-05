package randomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EnergyType;
import data.Card;
import data.Cards;
import data.Move;
import data.PokemonCard;
import randomizer.Settings.MoveTypeChanges;
import randomizer.Settings.RandomizationStrategy;
import rom.RomData;
import util.Logger;

public class MoveSetRandomizer {
	private RomData romData;
	private Logger logger;
	private final Cards<PokemonCard> pokeToGetAttacksFrom;
	
	public MoveSetRandomizer(RomData inRomData, Logger inLogger)
	{
		romData = inRomData;
		logger = inLogger;
		
		// Create a copy of the original pokes for easier move randomization if we change card types
		pokeToGetAttacksFrom = romData.allCards.getPokemonCards().copy(PokemonCard.class);
	}

	// TODO: CardDataConstants already has this? Refactor to use that from the Move data
	// Enum used for convenience when randomizing the moves in this class
	private enum RandomizerMoveCategory
	{
		EMPTY, MOVE, ATTACK, POKE_POWER, DAMAGING_ATTACK
	}
	
	public void randomize(long nextSeed, Settings settings)
	{
		boolean changedMoves = false;
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		
		// Get our strats in a convenient location
		RandomizationStrategy attackRandStrat = settings.getAttacks().getRandomizationStrat();
		RandomizationStrategy powerRandStrat = settings.getPokePowers().getRandomizationStrat();
		
		// If we are randomizing or shuffling either category of moves, go ahead and do it
		if (RandomizationStrategy.RANDOM == attackRandStrat || RandomizationStrategy.SHUFFLE == attackRandStrat ||
				RandomizationStrategy.RANDOM == powerRandStrat || RandomizationStrategy.SHUFFLE == powerRandStrat)
		{
			changedMoves = true;
			shuffleOrRandomizePokemonMoves(nextSeed, pokes, settings);
		}
		// nextSeed +=50; not needed currently as this is the last step in randomization here
		
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
			moves = poke.getAllMovesIncludingEmptyOnes();
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
	public static Map<PokemonCard, List<RandomizerMoveCategory>> getMoveTypesPerPokemon(Cards<PokemonCard> pokes, boolean groupPowersAndAttacks)
	{
		Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap = new TreeMap<>(Card.ID_SORTER);
		for (PokemonCard card : pokes.iterable())
		{
			List<RandomizerMoveCategory> moveTypesList = new ArrayList<>();
			for (Move move : card.getAllMovesIncludingEmptyOnes())
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
				// To look into: If we can only have one poke power per card, this should never happen
				// if we can, then maybe have a way to disable this
				if (!setDamagingAttack)
				{
					pokeMoveTypes.set(moveIndex, RandomizerMoveCategory.DAMAGING_ATTACK);
				}
			}
		}
	}

	/* Refactor to align with new approach when support for altering num moves per poke is added
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
		
		// For convenience/optimization
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
	*/

	/******************** Generate Movesets ************************************/
	public void shuffleOrRandomizePokemonMoves(
			long nextSeed,
			Cards<PokemonCard> pokes,
			Settings settings
	)
	{		
		// Get the current pokemon to move categories for each move map (read from ROM for now - in future add more options). 
		Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap = getMoveTypesPerPokemon(pokes, settings.getPokePowers().isIncludeWithMoves());
		nextSeed += 10; // Reserve seed space for when we add randomization to this
		
		// Perform a first pass through the moves assigning at least the attacks (both forced damaging and others) and possible
		// the pokepowers if set that way
		firstPassMoveAssignment(nextSeed, cardMovesMap, pokeToGetAttacksFrom, settings);
		nextSeed += 20;
		
		// Do the second pass for poke powers if they were not handled in the first pass
		secondPassMoveAssignment(nextSeed, cardMovesMap, pokeToGetAttacksFrom, settings);
		// Not needed now since this is the last one currently
		// nextSeed += 20

		// Finally set all the empty moves to be empty
		assignEmptyMovesToPokemon(cardMovesMap);
	}
	
	public void firstPassMoveAssignment(
			long nextSeed, 
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap,
			Cards<PokemonCard> originalPokesToTakeMovesFrom,
			Settings settings
	)
	{
		// Get our strat in a convenient location
		RandomizationStrategy attackRandStrat = settings.getAttacks().getRandomizationStrat();
		if (RandomizationStrategy.RANDOM == attackRandStrat || RandomizationStrategy.SHUFFLE == attackRandStrat)
		{
			// Adjust for damaging moves if required
			if (settings.getAttacks().isForceOneDamagingAttack())
			{
				adjustForDamagingMoves(cardMovesMap);
			}
			
			// Determine what we will assign for the first pass
			RandomizerMoveCategory firstPassMoveCat = 
					settings.getPokePowers().isIncludeWithMoves() ? RandomizerMoveCategory.MOVE : RandomizerMoveCategory.ATTACK;
			
			// Do per type if needed otherwise just do them all together
			if (settings.getAttacks().isRandomizationWithinType())
			{
				// Do one energy type at a time
				for (CardType pokeType : CardType.pokemonValues())
				{					
					firstPassMoveAssignmentHelper(nextSeed, 
							getEntrysForType(cardMovesMap, pokeType), 
							getSubsetOfMovePool(
									originalPokesToTakeMovesFrom.getCardsOfCardType(pokeType).getAllMoves(), 
									firstPassMoveCat), 
							firstPassMoveCat, settings);
				}
			}
			else
			{
				firstPassMoveAssignmentHelper(nextSeed, cardMovesMap, originalPokesToTakeMovesFrom.getAllMoves(), firstPassMoveCat, settings);
			}
		}
	}
	
	public void firstPassMoveAssignmentHelper(
			long nextSeed,
			Map<PokemonCard, List<RandomizerMoveCategory>> cardsMoveMap, 
			Collection<Move> allMovePool, 
			RandomizerMoveCategory firstPassMoveCat,
			Settings settings
	)
	{
		Set<Move> currentMovePool = new TreeSet<>(Move.BASIC_SORTER);
		List<Move> unusedMoves = new ArrayList<>();
		
		// If we force a damaging move, do that first
		if (settings.getAttacks().isForceOneDamagingAttack())
		{
			// Get set of damaging moves from the pool
			currentMovePool = getSubsetOfMovePool(allMovePool, RandomizerMoveCategory.DAMAGING_ATTACK);
			
			// Assign the damaging moves
			assignMovesToPokemon(nextSeed, cardsMoveMap, new ArrayList<>(currentMovePool), unusedMoves, RandomizerMoveCategory.DAMAGING_ATTACK, 
					RandomizationStrategy.SHUFFLE == settings.getAttacks().getRandomizationStrat()); // If its shuffle mode or not
		}
		
		// Then add the rest of the moves as appropriate. This will add only the added ones to the unused moves list
		addMovesToPool(allMovePool, firstPassMoveCat, currentMovePool, unusedMoves);
		assignMovesToPokemon(nextSeed, cardsMoveMap, new ArrayList<>(currentMovePool), unusedMoves, firstPassMoveCat, 
				RandomizationStrategy.SHUFFLE == settings.getAttacks().getRandomizationStrat()); // If its shuffle mode or not
	}
	
	public void secondPassMoveAssignment(
			long nextSeed, 
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap,
			Cards<PokemonCard> originalPokesToTakeMovesFrom,
			Settings settings
	)
	{
		// Get our strat in a convenient location
		RandomizationStrategy powerRandStrat = settings.getPokePowers().getRandomizationStrat();
		if (!settings.getPokePowers().isIncludeWithMoves() &&
				(RandomizationStrategy.RANDOM == powerRandStrat || RandomizationStrategy.SHUFFLE == powerRandStrat))
		{			
			if (settings.getPokePowers().isRandomizationWithinType())
			{
				// Do one energy type at a time
				for (CardType pokeType : CardType.pokemonValues())
				{
					// Now assign the pokepowers
					assignMovesToPokemon(nextSeed, 
							getEntrysForType(cardMovesMap, pokeType), 
							new ArrayList<>(getSubsetOfMovePool(
									originalPokesToTakeMovesFrom.getCardsOfCardType(pokeType).getAllMoves(), 
									RandomizerMoveCategory.POKE_POWER)), 
							new ArrayList<>(), // Empty list since this is separate from other move randomization
							RandomizerMoveCategory.POKE_POWER,
							RandomizationStrategy.SHUFFLE == powerRandStrat); // If its shuffle mode or not
				}
			}
			else
			{
				// Now assign the pokepowers
				assignMovesToPokemon(nextSeed, 
						cardMovesMap, 
						new ArrayList<>(getSubsetOfMovePool(originalPokesToTakeMovesFrom.getAllMoves(), RandomizerMoveCategory.POKE_POWER)), 
						new ArrayList<>(), // Empty list since this is separate from other move randomization
						RandomizerMoveCategory.POKE_POWER, 
						RandomizationStrategy.SHUFFLE == powerRandStrat); // If its shuffle mode or not
			}
		}
	}
	
	public Map<PokemonCard, List<RandomizerMoveCategory>> getEntrysForType(Map<PokemonCard, List<RandomizerMoveCategory>> allPokes, CardType pokeType)
	{
		return allPokes.entrySet().stream().filter(map -> pokeType == map.getKey().type)
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		
	}
	
	public Set<Move> getSubsetOfMovePool(Collection<Move> possibleMoves, RandomizerMoveCategory moveTypeToAdd)
	{
		Set<Move> moveSubpool = new TreeSet<>(Move.BASIC_SORTER);
		addMovesToPool(possibleMoves, moveTypeToAdd, moveSubpool, null);
		return moveSubpool;
	}
	
	public void addMovesToPool(Collection<Move> possibleMoves, RandomizerMoveCategory moveTypeToAdd, Set<Move> moveSubpool, List<Move> unusedMoves)
	{
		for (Move move : possibleMoves)
		{
			if ((RandomizerMoveCategory.MOVE == moveTypeToAdd && !move.isEmpty()) ||
				(RandomizerMoveCategory.POKE_POWER == moveTypeToAdd && move.isPokePower()) || 
				(move.isAttack() &&
						(RandomizerMoveCategory.ATTACK == moveTypeToAdd ||
								(RandomizerMoveCategory.DAMAGING_ATTACK == moveTypeToAdd && move.doesDamage()))))
			{
				boolean success = moveSubpool.add(move);
				if (success && unusedMoves != null)
				{
					unusedMoves.add(move);
				}
			}
		}
	}
	
	public void assignMovesToPokemon(
			long seed, 
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap, 
			List<Move> movePool, 
			List<Move> unusedMovePool, 
			RandomizerMoveCategory moveTypeAssign,
			boolean shuffle
	)
	{
		// Create and seed the randomizer
		Random rand = new Random(seed);
		System.out.print("seed " + seed);
		
		for (Entry<PokemonCard, List<RandomizerMoveCategory>> cardEntry : cardMovesMap.entrySet())
		{
			for (int moveIndex = 0; moveIndex < cardEntry.getValue().size(); moveIndex++)
			{
				if (cardEntry.getValue().get(moveIndex) == moveTypeAssign)
				{					
					// Call helper
					randomizeMoveAtIndex(rand, cardEntry.getKey(), moveIndex, movePool, unusedMovePool, shuffle);
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
			List<Move> unusedMoves,
			boolean shuffle
	)
	{		
		// Ensure the unusedMoves is not empty if doing shuffling
		if (shuffle && unusedMoves.isEmpty())
		{
			unusedMoves.addAll(moves);
		}
	
		// If its shuffle, use the unused moves list and remove it
		if (shuffle)
		{
			int randMoveIndex = rand.nextInt(unusedMoves.size());
			poke.setMove(unusedMoves.remove(randMoveIndex), moveIndex);
		}
		// Otherwise use the main list and leave it
		else
		{
			int randMoveIndex = rand.nextInt(moves.size());
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
			for (Move move : card.getAllMovesIncludingEmptyOnes())
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
			for (Move move : card.getAllMovesIncludingEmptyOnes())
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
