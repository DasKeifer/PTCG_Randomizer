package rom;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

import config.MoveExclusions;
import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EvolutionStage;
import data.Card;
import data.Move;
import data.NonPokemonCard;
import data.PokemonCard;

public class Cards<T extends Card>
{
	private TreeSet<T> cardSet = new TreeSet<>(Card.ID_SORTER);

	public Cards() 
	{
		cardSet = new TreeSet<>(Card.ID_SORTER);
	}
	
	private Cards(List<T> list) 
	{
		this();
		cardSet.addAll(list);
	}
	
	public Cards<T> copy(Class<? extends T> cardClass) 
	{
		Cards<T> copy = new Cards<>();
	    for(T card: cardSet)
	    {
	      copy.add(cardClass.cast(card.copy()));
	    }
	    return copy;
	}
	
	public Cards<T> recast(Class<? extends T> cardClass) 
	{
		Cards<T> recast = new Cards<>();
	    for(T card: cardSet)
	    {
	    	recast.add(cardClass.cast(card));
	    }
	    return recast;
	}
	
	public Cards<Card> upcast() 
	{
		Cards<Card> asCard = new Cards<>();
	    for(T card: cardSet)
	    {
	    	asCard.add(card);
	    }
	    return asCard;
	}

	public Cards<T> getCardsWithNameIgnoringNumber(String nameNumberIgnored)
	{
		return new Cards<>(cardSet.stream().filter(
				card -> card.name.matchesIgnoringPotentialNumber(nameNumberIgnored)).collect(Collectors.toList()));
	}
	
	// returns null if error encountered or no number was found
	public static <T extends Card> T getCardFromNameSetBasedOnNumber(
			Cards<T> cardsWithSameName, 
			String numberOrNameNumberIgnored
	)
	{
		// All will have the same name so just choose the first
		int cardIndex = -1;
		try
		{
			Integer.parseInt(numberOrNameNumberIgnored);
		}
		catch (NumberFormatException nfe)
		{
			cardIndex = cardsWithSameName.cardSet.first().name.getCardNumFromNameIfMatches(numberOrNameNumberIgnored);
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
		List<T> asList = cardsWithSameName.toList();
		
		if (index >= asList.size() || index < 0)
		{
			return null;
		}
		
		return asList.get(index);
	}

	public T getCardWithId(CardId cardId) 
	{
		Optional<T> foundCard = cardSet.stream().filter(card -> card.id == cardId).findFirst();
		if (foundCard.isPresent())
		{
			return foundCard.get();
		}
		
		return null;
	}
	
	public Cards<T> getCardsWithIds(Set<CardId> cardIds) 
	{
		return new Cards<>(cardSet.stream().filter(
				card -> cardIds.contains(card.id)).collect(Collectors.toList()));
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
				card = (PokemonCard) basics.toList().get(0);
			}
		}
		return basics;
	}
	
	public Cards<NonPokemonCard> getEnergyCards()
	{
		return new Cards<>(cardSet.stream()
				.filter(card -> card.type.isEnergyCard())
				.map(card -> (NonPokemonCard)card)
				.collect(Collectors.toList()));
	}

	public Cards<PokemonCard> getPokemonCards()
	{
		return new Cards<>(cardSet.stream()
				.filter(card -> card.type.isPokemonCard())
				.map(card -> (PokemonCard)card)
				.collect(Collectors.toList()));
	}

	public Cards<NonPokemonCard> getTrainerCards()
	{
		return new Cards<>(cardSet.stream()
				.filter(card -> card.type.isTrainerCard())
				.map(card -> (NonPokemonCard)card)
				.collect(Collectors.toList()));
	}
	
	public Cards<T> getCardsOfCardType(CardType cardType)
	{
		return new Cards<>(cardSet.stream().filter(
				card -> cardType.equals(card.type)).collect(Collectors.toList()));
	}
	
	public List<Move> getAllMovesForRandomization(MoveExclusions movesToExclude)
	{
		Cards<PokemonCard> pokeCards = getPokemonCards();
		List<Move> moves = new ArrayList<>();
		for (PokemonCard card : pokeCards.iterable())
		{
			for (Move move : card.getAllMovesIncludingEmptyOnes())
			{
				if (!move.isEmpty() && !movesToExclude.isMoveRemovedFromPool(card.id, move))
				{
					moves.add(move);
				}
			}
		}
		return moves;
	}
	
	public Collection<T> iterable()
	{
		return cardSet;
	}
	
	public List<T> toList()
	{
		return new LinkedList<>(cardSet);
	}

	public List<T> toSortedList()
	{
		return toSortedList(Card.ID_SORTER);
	}
	
	public List<T> toSortedList(Comparator<Card> comparator)
	{
		List<T> cardsList = toList();
		Collections.sort(cardsList, comparator);
		return cardsList;
	}
	
	public void add(T card)
	{
		cardSet.add(card);
	}

	public int count()
	{
		return cardSet.size();
	}
	
	// TODO later: Maybe move out of here since its a bit awkward here?
	public void finalizeDataForAllocating(Texts texts, Blocks blocks)
	{
		for (Card card : toList())
		{
			card.finalizeDataForAllocating(this.upcast(), texts, blocks);
		}
	}
}
