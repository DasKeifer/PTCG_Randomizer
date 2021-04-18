package data;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

import constants.CardDataConstants.CardType;

public class Cards<T extends Card>
{
	private TreeSet<T> cardSet = new TreeSet<>(Card.ID_SORTER);

	public Cards() 
	{
		cardSet = new TreeSet<>(Card.ID_SORTER);
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
	
	private Cards(List<T> list) 
	{
		this();
		cardSet.addAll(list);
	}

	public Cards<T> getCardsWithName(String name)
	{
		return new Cards<>(cardSet.stream().filter(
				card -> name.equals(card.name.toString())).collect(Collectors.toList()));
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

	public List<Move> getAllMoves()
	{
		Cards<PokemonCard> pokeCards = getPokemonCards();
		List<Move> moves = new ArrayList<>();
		for (PokemonCard card : pokeCards.iterable())
		{
			for (Move move : card.getAllMovesIncludingEmptyOnes())
			{
				if (!move.isEmpty())
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
}
