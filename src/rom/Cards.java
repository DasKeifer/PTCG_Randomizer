package rom;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

import config.MoveExclusions;
import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EvolutionStage;
import data.Card;
import data.Move;
import data.NonPokemonCard;
import data.PokemonCard;
import rom_packer.Blocks;

public class Cards<T extends Card>
{
	private EnumMap<CardId, T> cardsById;

	public Cards() 
	{
		cardsById = new EnumMap<>(CardId.class);
	}
	
	private Cards(List<T> list) 
	{
		this();
		list.forEach(c -> cardsById.put(c.id, c));
	}
	
	public Cards<T> copy(Class<? extends T> cardClass) 
	{
		Cards<T> copy = new Cards<>();
	    for(T card: cardsById.values())
	    {
	      copy.add(cardClass.cast(card.copy()));
	    }
	    return copy;
	}
	
	public Cards<T> recast(Class<? extends T> cardClass) 
	{
		Cards<T> recast = new Cards<>();
	    for(T card: cardsById.values())
	    {
	    	recast.add(cardClass.cast(card));
	    }
	    return recast;
	}
	
	public Cards<Card> upcast() 
	{
		Cards<Card> asCard = new Cards<>();
	    for(T card : cardsById.values())
	    {
	    	asCard.add(card);
	    }
	    return asCard;
	}
	
	public T first()
	{
		return cardsById.values().iterator().next();
	}

	public Cards<T> getCardsWithNameIgnoringNumber(String nameNumberIgnored)
	{
		return new Cards<>(cardsById.values().stream().filter(
				card -> card.name.matchesIgnoringPotentialNumber(nameNumberIgnored)).collect(Collectors.toList()));
	}
	
	// returns null if error encountered or no number was found
	public static <T extends Card> T getCardFromNameSetBasedOnNumber(
			Cards<T> cardsWithSameName, 
			String numberOrNameWithNumber
	)
	{
		int cardIndex = -1;
		// Assume its a number
		try
		{
			cardIndex = Integer.parseInt(numberOrNameWithNumber);
		}
		// If not then assume its a name with a number
		catch (NumberFormatException nfe)
		{
			// All will have the same name so just choose the first
			cardIndex = cardsWithSameName.first().name.getCardNumFromNameIfMatches(numberOrNameWithNumber);
		}
		
		// If we found an index (0 means no name, negative means failed to match name), return based on the index
		if (cardIndex > 0)
		{
			// If we found an index, try to get it shifting it to 0 based
			return getCardBasedOnIndex(cardsWithSameName, cardIndex - 1);
		}
		
		return null;
	}
	
	// Null if index out of bounds
	public static <T extends Card> T getCardBasedOnIndex(
			Cards<T> cardsWithSameName, 
			int index
	)
	{
		List<T> asList = cardsWithSameName.toListOrderedByCardId();
		
		if (index >= asList.size() || index < 0)
		{
			return null;
		}
		
		return asList.get(index);
	}

	public T getCardWithId(CardId cardId) 
	{
		return cardsById.get(cardId);
	}
	
	public Cards<T> getCardsWithIds(Set<CardId> cardIds) 
	{
		Cards<T> found = new Cards<>();
		for (CardId id : cardIds)
		{
			found.add(cardsById.get(id));
		}
		return found;
	}
	
	public Cards<Card> getBasicEvolutionOfCard(PokemonCard card)
	{
		Cards<Card> basics = new Cards<>();
		if (card.stage == EvolutionStage.BASIC)
		{
			basics.add(card);
		}
		else
		{	
			while (card.stage != EvolutionStage.BASIC)
			{
				basics = getCardsWithNameIgnoringNumber(card.prevEvoName.toString()).upcast();
				if (basics.count() <= 0)
				{
					break;
				}
				
				// If its not a poke, its probably a trainer like mysterious fossil. Assume
				// this is the "basic" pokemon
				if (!card.type.isPokemonCard())
				{
					break;
				}
				
				// TODO later: Doesn't work with mysterious fossil - we only check the parent not the child
				// is a poke card
				card = (PokemonCard) basics.toListOrderedByCardId().get(0);
			}
		}
		return basics;
	}
	
	public Cards<NonPokemonCard> getEnergyCards()
	{
		return new Cards<>(cardsById.values().stream()
				.filter(card -> card.type.isEnergyCard())
				.map(card -> (NonPokemonCard)card)
				.collect(Collectors.toList()));
	}

	public Cards<PokemonCard> getPokemonCards()
	{
		return new Cards<>(cardsById.values().stream()
				.filter(card -> card.type.isPokemonCard())
				.map(card -> (PokemonCard)card)
				.collect(Collectors.toList()));
	}

	public Cards<NonPokemonCard> getTrainerCards()
	{
		return new Cards<>(cardsById.values().stream()
				.filter(card -> card.type.isTrainerCard())
				.map(card -> (NonPokemonCard)card)
				.collect(Collectors.toList()));
	}
	
	public Cards<T> getCardsOfCardType(CardType cardType)
	{
		return new Cards<>(cardsById.values().stream().filter(
				card -> cardType.equals(card.type)).collect(Collectors.toList()));
	}
	
	public List<Move> getAllMoves()
	{
		return getAllMovesForRandomization(null);
	}
	
	public List<Move> getAllMovesForRandomization(MoveExclusions movesToExclude)
	{
		Cards<PokemonCard> pokeCards = getPokemonCards();
		List<Move> moves = new ArrayList<>();
		for (PokemonCard card : pokeCards.iterable())
		{
			for (Move move : card.getAllMovesIncludingEmptyOnes())
			{
				if (!move.isEmpty() && (movesToExclude == null || !movesToExclude.isMoveRemovedFromPool(card.id, move)))
				{
					moves.add(move);
				}
			}
		}
		return moves;
	}

	// TODO later: encapsulate safer to prevent editing outside class?
	public Collection<T> iterable()
	{
		return cardsById.values();
	}

	// No sort needed
	public List<T> toListOrderedByCardId()
	{
		// Already sorted by Id
		return new LinkedList<>(cardsById.values());
	}
	
	public List<T> toListCustomSort(Comparator<Card> comparator)
	{
		List<T> cardsList = toListOrderedByCardId();
		Collections.sort(cardsList, comparator);
		return cardsList;
	}
	
	public void add(T card)
	{
		cardsById.put(card.id, card);
	}

	public int count()
	{
		return cardsById.size();
	}
	
	// TODO later: Maybe move out of here since its a bit awkward here?
	public void finalizeDataForAllocating(Texts texts, Blocks blocks)
	{
		for (Card card : toListOrderedByCardId())
		{
			card.finalizeDataForAllocating(this.upcast(), texts, blocks);
		}
	}
}
