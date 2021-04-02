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
import data.PokemonCard.MoveCategories;

public class Cards<T extends Card>
{
	private Comparator<Card> defaultSorter = new Card.IdSorter();
	private TreeSet<T> cardSet = new TreeSet<>(defaultSorter);

	public Cards() 
	{
		defaultSorter = new Card.IdSorter();
		cardSet = new TreeSet<>(defaultSorter);
	}
	
	public Cards(List<T> list) 
	{
		this();
		cardSet.addAll(list);
	}

	public Cards<T> getCardsWithName(String name)
	{
		return new Cards<>(cardSet.stream().filter(
				card -> name.equals(card.name.getText())).collect(Collectors.toList()));
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
		return getAllMoves(MoveCategories.ALL);
	}
	
	public List<Move> getAllMoves(MoveCategories moveCategory)
	{
		Cards<PokemonCard> pokeCards = getPokemonCards();
		List<Move> moves = new ArrayList<>();
		for (PokemonCard card : pokeCards.iterable())
		{
			for (Move move : card.getAllMoves(moveCategory))
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
		return toSortedList(defaultSorter);
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
