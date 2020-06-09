package randomizer;

import java.io.File;
import java.io.IOException;
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
import rom.Texts;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	static final long SEED = 42;
	static Random rand = new Random(SEED);
	
	RomData romData;
	
	public void openRom(File romFile)
	{
		try {
			romData = RomHandler.readRom(romFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveRom(File romFile)
	{
		try {
			RomHandler.writeRom(romData, romFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//public static void main(String[] args) throws IOException //Temp
	public void randomize() throws IOException
	{
		List<Card> venu = romData.allCards.getCardsWithName("Venusaur").toList();
		venu.get(1).name.setTextAndDeformat("Test-a-saur");
		
		Cards<PokemonCard> pokes = romData.allCards.getPokemonCards();

		//double[] numWithMoves = {0.05, 0.35, 0.60};
		Map<CardId, Integer> numMovesPerPokemon = getNumMovesPerPokemon(pokes);
		shuffleOrRandomizePokemonMoves(false, pokes, numMovesPerPokemon, true, 1);
		
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
		int numCards = pokes.count();
		int numCardsRemaining = numCards;
		int[] numCardsWithNumMoves = new int[PokemonCard.NUM_MOVES + 1];
		for (int numMoves = 0; numMoves < PokemonCard.NUM_MOVES + 1; numMoves++)
		{
			if (percentWithNumMoves[numMoves] * numCards <= numCardsRemaining)
			{
				numCardsWithNumMoves[numMoves] = (int) (percentWithNumMoves[numMoves] * numCards);
				numCardsRemaining -= numCardsWithNumMoves[numMoves];
			}
			else 
			{
				numCardsWithNumMoves[numMoves] = numCardsRemaining;
				numCardsRemaining = 0;
			}
			
			System.out.println(numCardsWithNumMoves[numMoves]);
		}
	
		Map<CardId, Integer> cardMovesMap = new HashMap<>();	
		for (PokemonCard card : pokes.iterable())
		{
			cardMovesMap.put(card.id, PokemonCard.NUM_MOVES);
		}
		
		int maxTries = 10000;
		int triesCount = 0;
		CardId randCardId;
		List<PokemonCard> pokeList = pokes.toList();
		for (int numMoves = 0; numMoves < PokemonCard.NUM_MOVES; numMoves++)
		{
			for (int count = 0; count < numCardsWithNumMoves[numMoves]; count++)
			{
				randCardId = pokeList.get(rand.nextInt(numCards)).id;
				triesCount = 0;
				while (cardMovesMap.get(randCardId) != PokemonCard.NUM_MOVES)
				{
					randCardId = pokeList.get(rand.nextInt(numCards)).id;
					
					// Prevent hanging just in case something very bad happens in our calculations
					if (triesCount++ > maxTries)
					{
						throw new RuntimeException("Ran out of attempts while randomizing/shuffling moves");
					}
				}
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
		for (int moveIndex = 0; moveIndex < PokemonCard.NUM_MOVES; moveIndex++)
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

