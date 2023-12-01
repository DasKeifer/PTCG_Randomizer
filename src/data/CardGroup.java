package data;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import config.MoveExclusions;
import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EvolutionStage;

public class CardGroup<T extends Card>
{
	private EnumMap<CardId, T> cardsById;

	public CardGroup() 
	{
		cardsById = new EnumMap<>(CardId.class);
	}
	
	private CardGroup(List<T> list) 
	{
		this();
		list.forEach(c -> cardsById.put(c.id, c));
	}
	
	public CardGroup<T> copy(Class<? extends T> cardClass) 
	{
		CardGroup<T> copy = new CardGroup<>();
	    for(T card: cardsById.values())
	    {
	      copy.add(cardClass.cast(card.copy()));
	    }
	    return copy;
	}
	
	public CardGroup<T> recast(Class<? extends T> cardClass) 
	{
		CardGroup<T> recast = new CardGroup<>();
	    for(T card: cardsById.values())
	    {
	    	recast.add(cardClass.cast(card));
	    }
	    return recast;
	}
	
	public CardGroup<Card> upcast() 
	{
		CardGroup<Card> asCard = new CardGroup<>();
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

	public CardGroup<T> withNameIgnoringNumber(String nameNumberIgnored)
	{
		return new CardGroup<>(cardsById.values().stream().filter(
				card -> card.name.matchesIgnoringPotentialNumber(nameNumberIgnored)).collect(Collectors.toList()));
	}
	
	// returns null if error encountered or no number was found
	public static <T extends Card> T fromNameSetBasedOnNumber(
			CardGroup<T> cardsWithSameName, 
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
			return basedOnIndex(cardsWithSameName, cardIndex - 1);
		}
		
		return null;
	}
	
	// Null if index out of bounds
	public static <T extends Card> T basedOnIndex(
			CardGroup<T> cardsWithSameName, 
			int index
	)
	{
		List<T> asList = cardsWithSameName.listOrderedByCardId();
		
		if (index >= asList.size() || index < 0)
		{
			return null;
		}
		
		return asList.get(index);
	}

	public T withId(CardId cardId) 
	{
		return cardsById.get(cardId);
	}
	
	public CardGroup<T> withIds(Set<CardId> cardIds) 
	{
		CardGroup<T> found = new CardGroup<>();
		for (CardId id : cardIds)
		{
			found.add(cardsById.get(id));
		}
		return found;
	}
	
	// TODO later: Move this and some other more logic specific/search
	// functions to a separate class?
	public CardGroup<Card> determineBasicEvolutionOfCard(MonsterCard card)
	{
		CardGroup<Card> basics = new CardGroup<>();
		if (card.stage == EvolutionStage.BASIC)
		{
			basics.add(card);
		}
		else
		{	
			while (card.stage != EvolutionStage.BASIC)
			{
				basics = withNameIgnoringNumber(card.prevEvoName.toString()).upcast();
				if (basics.count() <= 0)
				{
					break;
				}
				
				// If its not a poke, its probably a trainer like mysterious fossil. Assume
				// this is the "basic" monsters
				if (!card.type.isMonsterCard())
				{
					break;
				}
				
				// TODO later: Doesn't work with mysterious fossil - we only check the parent not the child
				// is a poke card
				card = (MonsterCard) basics.listOrderedByCardId().get(0);
			}
		}
		return basics;
	}
	
	public CardGroup<NonMonsterCard> energyCards()
	{
		return new CardGroup<>(cardsById.values().stream()
				.filter(card -> card.type.isEnergyCard())
				.map(card -> (NonMonsterCard)card)
				.collect(Collectors.toList()));
	}

	public CardGroup<MonsterCard> monsterCards()
	{
		return new CardGroup<>(cardsById.values().stream()
				.filter(card -> card.type.isMonsterCard())
				.map(card -> (MonsterCard)card)
				.collect(Collectors.toList()));
	}

	public CardGroup<NonMonsterCard> trainerCards()
	{
		return new CardGroup<>(cardsById.values().stream()
				.filter(card -> card.type.isTrainerCard())
				.map(card -> (NonMonsterCard)card)
				.collect(Collectors.toList()));
	}
	
	public CardGroup<T> ofCardType(CardType cardType)
	{
		return new CardGroup<>(cardsById.values().stream().filter(
				card -> cardType.equals(card.type)).collect(Collectors.toList()));
	}
	
	public List<Move> allMoves()
	{
		return allMovesForRandomization(null);
	}
	
	public List<Move> allMovesForRandomization(MoveExclusions movesToExclude)
	{
		CardGroup<MonsterCard> pokeCards = monsterCards();
		List<Move> moves = new ArrayList<>();
		for (MonsterCard card : pokeCards.iterable())
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
	
	public Stream<T> stream()
	{
		return cardsById.values().stream();
	}

	// No sort needed
	public List<T> listOrderedByCardId()
	{
		// Already sorted by Id
		return new LinkedList<>(cardsById.values());
	}
	
	public List<T> listCustomSort(Comparator<Card> comparator)
	{
		List<T> cardsList = listOrderedByCardId();
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
}
