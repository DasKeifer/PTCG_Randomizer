package randomizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import data.Card;
import data.Cards;
import data.Move;
import data.PokemonCard;
import randomizer.Settings.RandomizationStrategy;
import rom.Texts;
import util.Logger;
import util.MathUtils;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	static final String SEED_LOG_EXTENSION = ".seed.txt";
	static final String LOG_FILE_EXTENSION = ".log.txt";
	static final long SEED = 42;
	
	private Logger logger;
	static Random rand = new Random(SEED);	
	
	RomData romData;
	
	public Randomizer()
	{
		logger = new Logger();
		rand = new Random(SEED);
	}
	
	public void openRom(File romFile)
	{
		try 
		{
			romData = RomHandler.readRom(romFile);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void randomizeAndSaveRom(File romFile, Settings settings) throws IOException
	{
		String romBasePath = romFile.getPath();
		romBasePath = romBasePath.substring(0, romBasePath.lastIndexOf('.'));
		
		if (settings.isLogSeed())
		{
			FileWriter seedFile = new FileWriter(romBasePath + SEED_LOG_EXTENSION);
			try
			{
				seedFile.write(Long.toString(SEED));
			}
			finally
			{
				seedFile.close();
			}
		}
		
		if (settings.isLogDetails())
		{
			logger.open(romBasePath + LOG_FILE_EXTENSION);
		}
		
		randomize(settings);

		logger.close();
		
		try {
			RomHandler.writeRom(romData, romFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//public static void main(String[] args) throws IOException //Temp
	public void randomize(Settings settings)
	{
		List<Card> venu = romData.allCards.getCardsWithName("Venusaur").toList();
		venu.get(1).name.setTextAndDeformat("Test-a-saur");
		
		randomizeMovesAndPowers(settings);

		
		test(romData.allCards.getCardsWithName("Metapod"));
		
		// Temp hack to add more value cards to a pack
		// 11 is the most we can do
		for (int i = 0; i < 16; i ++)
		{
			System.out.println(romData.rawBytes[0x1e4d4 + i]);
			if (i % 4 == 1)
			{
				romData.rawBytes[0x1e4d4 + i] = 5;
			}
			else if (i % 4 == 2)
			{
				romData.rawBytes[0x1e4d4 + i] = 4;
			}
			else if (i % 4 == 3)
			{
				romData.rawBytes[0x1e4d4 + i] = 2;
			}
			else
			{
				romData.rawBytes[0x1e4d4 + i] = 0;
			}
		}
	}
	
	public void randomizeMovesAndPowers(Settings settings)
	{		
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();
		
		// TODO get from settings
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
			// TODO: Optionally include PokePowers
	        shuffleOrRandomizePokemonMoves(
	        		RandomizationStrategy.SHUFFLE == moveRandStrat, // Shuffle not Random
	        		pokes, numMovesPerPokemon, settings.getMoves().isMovesAttacksWithinType(), 
	        		1); // Num non poke power moves
		}
		// else No randomization being done for moves - nothign to do here
		
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
			}
		}
	}
	
	public static void test(Cards<Card> cards)
	{
		for (Card card : cards.iterable())
		{
			System.out.println(card.toString() + "\n");
		}
	}
	
	public static void test(Texts allText)
	{
		for (short i = 1; i < 20; i++) //String text : allText)
		{
			System.out.println(allText.getAtId(i));
		}
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
		Map<CardId, Integer> cardMovesMap = new HashMap<>();
		for (PokemonCard card : pokes.iterable())
		{
			cardMovesMap.put(card.id, card.getNumMoves());
		}
		return cardMovesMap;
	}
	
	public static Map<CardId, Integer> getRandNumMovesPerPokemon(
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
		Map<CardId, Integer> cardMovesMap = new HashMap<>();
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

	public static Map<CardId, Integer> getRandNumMovesPerPokemonByType(
			Cards<PokemonCard> pokes, 
			Map<CardType, double[]> percentWithNumMovesByType
	)
	{
		Map<CardId, Integer> numMovesPerPoke = new HashMap<>();
		
		// Do one energy type at a time
		for (CardType pokeType : CardType.pokemonValues())
		{				
			// Determine the number of moves per pokemon for this type
			numMovesPerPoke.putAll(
					getRandNumMovesPerPokemon(
							pokes.getCardsOfCardType(pokeType), 
							percentWithNumMovesByType.get(pokeType)));
		}	
		
		return numMovesPerPoke;
	}

	public static void shuffleOrRandomizePokemonMoves(
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
				shuffleOrRandomizePokemonMovesHelper(shuffle, typeCards, numMovesPerPoke, numNonPokePower, typeMove);
			}	
		}
		else
		{
			// Otherwise get all the moves and do them at once
			List<Move> typeMove = pokes.getAllMoves();
			shuffleOrRandomizePokemonMovesHelper(shuffle, pokes, numMovesPerPoke, numNonPokePower, typeMove);
		}
		
	}
	
	private static void shuffleOrRandomizePokemonMovesHelper(
			boolean shuffle,
			Cards<PokemonCard> pokes,
			Map<CardId, Integer> numMovesPerPoke,
			int numNonPokePower, 
			List<Move> moves)
	{
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
					shuffleOrRandomizeMoveAtIndex(poke, moveIndex, moves, shuffle, numNonPokePower > moveIndex);
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
	
	private static void shuffleOrRandomizeMoveAtIndex(
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

	public String getFileExtension() 
	{
		return ".gbc";
	}
}

