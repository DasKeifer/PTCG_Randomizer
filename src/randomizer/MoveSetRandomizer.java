package randomizer;

import java.util.ArrayList;
import java.util.Arrays;
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
import util.MathUtils;

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
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap = getMoveTypesPerPokemon(nextSeed, pokes, settings);
			nextSeed += 10; // Reserve seed space for when we add randomization to this

			shuffleOrRandomizePokemonMoves(nextSeed, cardMovesMap, settings);
		}
		// nextSeed += 80; not needed currently as this is the last step in randomization here
		
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

	/***************** Determine Number an categories of Moves ****************************/
	public Map<PokemonCard, List<RandomizerMoveCategory>> getMoveTypesPerPokemon(
			long nextSeed, 
			Cards<PokemonCard> pokes,
			Settings settings)
	{
		Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap;
		if (settings.isMovesRandomNumberOfAttacks())
		{
			// In the future we will have more options for how to distribute these
			// We may also want to go with straight numbers instead of fool around with percentages
			double[] percentWithNumOverallMoves = getNumOfCategoryPercentages(pokes, RandomizerMoveCategory.MOVE);
			double[] percentWithNumPokepowers = getNumOfCategoryPercentages(pokes, RandomizerMoveCategory.POKE_POWER);
			cardMovesMap = getRandMoveTypesPerPoke(
					nextSeed, pokes, percentWithNumOverallMoves, 
					percentWithNumPokepowers, settings.getPokePowers().isIncludeWithMoves());
		}
		else
		{
			// Get the current pokemon to move categories for each move map (read from ROM for now - in future add more options). 
			cardMovesMap = getRomsMoveTypesPerPokemon(pokes, settings.getPokePowers().isIncludeWithMoves());
		}
		return cardMovesMap;
	}
	
	public static Map<PokemonCard, List<RandomizerMoveCategory>> getRomsMoveTypesPerPokemon(Cards<PokemonCard> pokes, boolean groupPowersAndAttacks)
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

	public static double[] getNumOfCategoryPercentages(Cards<PokemonCard> pokes, RandomizerMoveCategory moveCategory)
	{
		// Create and initialize to zero
		// +1 since 0 is a valid option
		int[] numPerCount = new int[PokemonCard.MAX_NUM_MOVES + 1];
		Arrays.fill(numPerCount, 0);
		
		for (PokemonCard card : pokes.iterable())
		{
			int numFound = 0;
			for (Move move : card.getAllMovesIncludingEmptyOnes())
			{
				if ((RandomizerMoveCategory.ATTACK == moveCategory && move.isAttack()) ||
						(RandomizerMoveCategory.POKE_POWER == moveCategory && move.isPokePower()) ||
						(RandomizerMoveCategory.MOVE == moveCategory && !move.isEmpty()) ||
						(RandomizerMoveCategory.DAMAGING_ATTACK == moveCategory && move.doesDamage()) ||
						(RandomizerMoveCategory.EMPTY == moveCategory && move.isEmpty()))
				{
					numFound++;
				}
			}
			numPerCount[numFound] += 1;
		}
		
		// Convert raw numbers to percentages
		return MathUtils.convertNumbersToPercentages(numPerCount);
	}
	
	public static Map<PokemonCard, List<RandomizerMoveCategory>> getRandMoveTypesPerPoke(
			long nextSeed,
			Cards<PokemonCard> pokes, 
			double[] percentWithNumOverallMoves, 
			double[] percentWithNumPokepowers,
			boolean groupPowersAndAttacks 
	)
	{
		getRandMoveTypesPerPokeArgCheck(percentWithNumOverallMoves, percentWithNumPokepowers);
		
		// Determine how many cards will have what overall number of moves and pokepowers
		int[] numCardsWithNumOverallMoves = MathUtils.convertPercentageToIntValues(percentWithNumOverallMoves, pokes.count());
		int[] numCardsWithNumPokePowers = MathUtils.convertPercentageToIntValues(percentWithNumPokepowers, pokes.count());

		// For each number of moves, go through and assign cards to values
		Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap = new TreeMap<>(Card.ID_SORTER);
		List<PokemonCard> unassignedPokes = pokes.toList();
		Random attacksRand = new Random(nextSeed++);
		Random powersRand = new Random(nextSeed); // ++ not needed since this is the last increment for now
		for (int numMoves = 0; numMoves <= PokemonCard.MAX_NUM_MOVES; numMoves++)
		{			
			// Create the default list that will be assigned for cards with this number of moves
			List<RandomizerMoveCategory> defaultMovesSet = getRandMoveTypesPerPokeCreateMovelist(numMoves, groupPowersAndAttacks);
			
			// For each card that should have this number of moves
			for (int cardCount = 0; cardCount < numCardsWithNumOverallMoves[numMoves]; cardCount++)
			{
				// Get a random card and remove it from the available pool then assign
				// the number of moves to it
				List<RandomizerMoveCategory> storedMoves = new ArrayList<>(defaultMovesSet);
				cardMovesMap.put(unassignedPokes.remove(attacksRand.nextInt(unassignedPokes.size())), storedMoves);
				
				// Now see about adjusting the list for poke powers
				if (!groupPowersAndAttacks)
				{
					getRandMoveTypesPerPokeAdjustForPokePowers(powersRand, storedMoves, numMoves, numCardsWithNumPokePowers);
				}
			}
		}
		
		return cardMovesMap;
	}
	
	private static void getRandMoveTypesPerPokeArgCheck(
			double[] percentWithNumOverallMoves, 
			double[] percentWithNumPokepowers
	)
	{		
		// Plus one since 0 moves is an option
		int numMovesPossibilities = PokemonCard.MAX_NUM_MOVES + 1;

		// Check the lengths
		if (percentWithNumOverallMoves.length != numMovesPossibilities)
		{
			throw new IllegalArgumentException("Passed percentages for numbers of overall moves length (" + 
					percentWithNumOverallMoves.length + " is not the expected number of " + numMovesPossibilities);
		}
		if (percentWithNumPokepowers.length != numMovesPossibilities)
		{
			throw new IllegalArgumentException("Passed percentages for numbers of poke powers length (" + 
					percentWithNumPokepowers.length + " is not the expected number of " + numMovesPossibilities);
		}
		
		// Ensure the pokepower numbers align with the total moves
		int movesSum = 0;
		int pokePowerSum = 0;
		for (int numMoves = 0; numMoves <= PokemonCard.MAX_NUM_MOVES; numMoves++)
		{			
			movesSum += percentWithNumOverallMoves[numMoves];
			pokePowerSum += percentWithNumPokepowers[numMoves];
			
			if (movesSum > pokePowerSum)
			{
				throw new IllegalArgumentException("Passed percentages for numbers of poke powers and number of "
						+ "moves do not allign! The sum of all percentages up to a given number of pokepowers must be "
						+ "higher for the moves than for the moves. For " + numMoves + ", the poke powers "
						+ "are lower at " + pokePowerSum + " than the moves at " + movesSum);
			}
		}
	}

	private static void getRandMoveTypesPerPokeAdjustForPokePowers(
			Random powersRand,
			List<RandomizerMoveCategory> moveCats,
			int numberOfMoves,
			int[] numCardsWithNumPokePowers)
	{
		// First found out how may cards are left with allowable numbers of poke powers
		int numPossibilities = sumThroughIndex(numCardsWithNumPokePowers, numberOfMoves);

		// Now select how many poke powers this card will have
		int numPowers = 0;
		int selected = powersRand.nextInt(numPossibilities);
		for (/*already set*/; numPowers <= numberOfMoves; numPowers++)
		{
			selected -= numCardsWithNumPokePowers[numPowers];
			if (selected < 0)
			{
				// Decrement the count since we just assigned one
				numCardsWithNumPokePowers[numPowers]--;
				break;
			}
		}
		
		// Finally change them to poke powers
		for (int pokePowerCount = 0; pokePowerCount < numPowers; pokePowerCount++)
		{
			moveCats.set(pokePowerCount, RandomizerMoveCategory.POKE_POWER);
		}
	}
	
	private static List<RandomizerMoveCategory> getRandMoveTypesPerPokeCreateMovelist(
			int numMovesToAdd,
			boolean groupPowersAndAttacks
	)
	{
		List<RandomizerMoveCategory> defaultMovesSet = new ArrayList<>();
		for (int count = 0; count < PokemonCard.MAX_NUM_MOVES; count++)
		{
			if (count < numMovesToAdd)
			{
				defaultMovesSet.add(groupPowersAndAttacks ? RandomizerMoveCategory.MOVE : RandomizerMoveCategory.ATTACK);
			}
			else
			{
				defaultMovesSet.add(RandomizerMoveCategory.EMPTY);
			}
		}
		return defaultMovesSet;
	}
	
	public static void adjustForDamagingMoves(long seed, Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap)
	{
		// In the future, only if the setting is set
		Random rand = new Random(seed);
		List<PokemonCard> donorPokes = cardMovesMap.entrySet().stream().filter(map -> numAttacks(map.getValue()) > 1)
				.map(Entry::getKey).collect(Collectors.toList());
		
		// Go through each poke and make sure we set the damaging attack
		for (List<RandomizerMoveCategory> pokeMoveTypes : cardMovesMap.values())
		{
			// If they already have a damaging attack we are good
			if (pokeMoveTypes.contains(RandomizerMoveCategory.DAMAGING_ATTACK))
			{
				continue;
			}
			
			// if they have an attack or move then we can just replace it
			int indexFound = getIndexOfAttackNotReservedForDamaging(pokeMoveTypes);
			
			if (indexFound >= 0)
			{
				pokeMoveTypes.set(indexFound, RandomizerMoveCategory.DAMAGING_ATTACK);
			}
			else
			{
				// If it failed, call the function to handle what to reassign to a damaging move
				adjustForDamagingMovesNoAttacks(pokeMoveTypes, rand, donorPokes, cardMovesMap);
			}
		}
	}

	private static void adjustForDamagingMovesNoAttacks(
			List<RandomizerMoveCategory> pokeMoveTypes, 
			Random rand, 
			List<PokemonCard> donorPokes, 
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap)
	{
		// In the future add more options on how to handle these cases
		// If it has no moves, force one
		if (!pokeMoveTypes.contains(RandomizerMoveCategory.POKE_POWER)) 
		{
			pokeMoveTypes.set(0, RandomizerMoveCategory.DAMAGING_ATTACK);
		}
		// at least one one poke power
		else
		{
			// Check if we have a donor poke to swap a move with
			if (!donorPokes.isEmpty())
			{
				int randIndex = rand.nextInt(donorPokes.size());
				List<RandomizerMoveCategory> donorPokeMoves = cardMovesMap.get(donorPokes.get(randIndex));
				int moveIndex = getIndexOfAttackNotReservedForDamaging(donorPokeMoves);
				if (moveIndex >= 0)
				{
					donorPokeMoves.set(moveIndex, RandomizerMoveCategory.POKE_POWER);
					donorPokes.remove(randIndex);
				}
				// else error!
			}
			
			// regardless, set the pokepower to a damaging attack
			pokeMoveTypes.set(pokeMoveTypes.indexOf(RandomizerMoveCategory.POKE_POWER), RandomizerMoveCategory.DAMAGING_ATTACK);
			
		}
	}

	// Helper functions
	private static int sumThroughIndex(int[] numbers, int lastIndex)
	{
		int sum = 0;
		for (int index = 0; index <= lastIndex; index++)
		{
			sum += numbers[index];
		}
		return sum;
	}
	
	private static int numAttacks(List<RandomizerMoveCategory> moves)
	{
		int occurs = 0;
		for (RandomizerMoveCategory moveCat : moves)
		{
			if (RandomizerMoveCategory.ATTACK == moveCat ||
					RandomizerMoveCategory.MOVE == moveCat ||
					RandomizerMoveCategory.DAMAGING_ATTACK == moveCat)
			{
				occurs++;
			}
		}
		return occurs;
	}
	
	private static int getIndexOfAttackNotReservedForDamaging(List<RandomizerMoveCategory> moves)
	{
		for (int moveIndex = 0; moveIndex < moves.size(); moveIndex++)
		{
			if (RandomizerMoveCategory.ATTACK == moves.get(moveIndex) ||
					RandomizerMoveCategory.MOVE == moves.get(moveIndex))
			{
				return moveIndex;
			}
		}
		
		return -1;
	}

	/************************** Generate Movesets ****************************************/
	public void shuffleOrRandomizePokemonMoves(
			long nextSeed,
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap,
			Settings settings
	)
	{				
		// Perform a first pass through the moves assigning at least the attacks (both forced damaging and others) and possible
		// the pokepowers if set that way
		firstPassMoveAssignment(nextSeed, cardMovesMap, pokeToGetAttacksFrom, settings);
		nextSeed += 30;

		// Do the second pass for poke powers if they were not handled in the first pass
		secondPassMoveAssignment(nextSeed, cardMovesMap, pokeToGetAttacksFrom, settings);
		// Not needed now since this is the last one currently
		// nextSeed += 30

		// Finally set all the empty moves to be empty
		assignEmptyMovesToPokemon(cardMovesMap);
	}
	
	public void firstPassMoveAssignment(
			long nextSeed, 
			Map<PokemonCard, List<RandomizerMoveCategory>> cardMovesMap,
			Cards<PokemonCard> pokesToGetMovesFrom,
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
				adjustForDamagingMoves(nextSeed, cardMovesMap);
			}
			nextSeed += 10; // Leave some room for additions
			
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
									pokesToGetMovesFrom.getCardsOfCardType(pokeType).getAllMoves(), 
									firstPassMoveCat), 
							firstPassMoveCat, settings);
					nextSeed += 3; // helper uses two seeds - leave one for expansion, 24 total
				}
			}
			else
			{
				firstPassMoveAssignmentHelper(nextSeed, cardMovesMap, pokesToGetMovesFrom.getAllMoves(), firstPassMoveCat, settings);
				// nextSeed += 3 * CardType.pokemonValues().size(); not needed at the moment - 24 total
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
		// always increment for consistency
		nextSeed++;
		
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
					nextSeed += 3; // +3 to be consistent with first pass
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
				// nextSeed += 3 * CardType.pokemonValues().size(); not needed at the moment
			}
		}
	}
	
	public Map<PokemonCard, List<RandomizerMoveCategory>> getEntrysForType(Map<PokemonCard, List<RandomizerMoveCategory>> allPokes, CardType pokeType)
	{
		return allPokes.entrySet().stream().filter(map -> pokeType == map.getKey().type)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
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
				(RandomizerMoveCategory.ATTACK == moveTypeToAdd  && move.isAttack()) ||
				(RandomizerMoveCategory.DAMAGING_ATTACK == moveTypeToAdd && move.doesDamage()))
			{
				// If it was added and we are keeping track of moves (shuffled mode) then
				// add it to the list of moves that haven't been used
				if (moveSubpool.add(move) && unusedMoves != null)
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
