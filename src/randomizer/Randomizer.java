package randomizer;

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
	static final long seed = 42;
	static Random rand = new Random(seed);
	
	public static void main(String[] args) throws IOException //Temp
	{
		RomData rom = RomHandler.readRom();
		List<Card> venu = rom.allCards.getCardsWithName("Venusaur").toList();
		venu.get(1).name.setTextAndDeformat("Test-a-saur");
		
		double[] numWithMoves = {0, 0.33, 0.67};
		Cards<PokemonCard> pokes = rom.allCards.getPokemonCards();
		randomizeCards(pokes, false, 1, numWithMoves, false);
		
		RomHandler.writeRom(rom);
	}
	
	public static void test(List<Card> cards)
	{
		for (Card card : cards)
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
			
	public static void randomizeCards(
			Cards<PokemonCard> pokes,
			boolean matchTypes,
			int numNonPokePower,
			double[] percentWithNumMoves,
			boolean percentsPerType
	)
	{		
		Map<CardId, Integer> numMovesPerPoke = null;		
		if (percentsPerType)
		{
			numMovesPerPoke = new HashMap<>();
			
			// Do one energy type at a time
			for (CardType pokeType : CardType.pokemonValues())
			{				
				// Determine the number of moves per pokemon for this type
				numMovesPerPoke.putAll(
						getNumMovesPerCard(
								pokes.getCardsOfCardType(pokeType), 
								percentWithNumMoves));
			}	
		}
		else
		{
			// Otherwise do them all together
			numMovesPerPoke = getNumMovesPerCard(pokes, percentWithNumMoves);
		}
		
		// If we want to match the move to the poke type,
		if (matchTypes)
		{
			// Do one energy type at a time
			for (CardType pokeType : CardType.pokemonValues())
			{
				// Get the pokemon of this type and the moves if we are set
				// to match the types
				Cards<PokemonCard> typeCards = pokes.getCardsOfCardType(pokeType);
				List<Move> typeMove = typeCards.getAllMoves();
				shuffleOrRandomizeMoves(false, typeCards, numMovesPerPoke, numNonPokePower, typeMove);
			}	
		}
		else
		{
			// Otherwise get all the moves and do them at once
			List<Move> typeMove = pokes.getAllMoves();
			shuffleOrRandomizeMoves(false, pokes, numMovesPerPoke, numNonPokePower, typeMove);
		}
		
	}
	
	private static Map<CardId, Integer> getNumMovesPerCard(
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
	
	private static void shuffleOrRandomizeMoves(
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
}

