package data;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Cards 
{
	private Comparator<Card> defaultSorter = new Card.IdSorter();
	private TreeSet<Card> cardSet = new TreeSet<>(defaultSorter);

	public Cards() 
	{
		defaultSorter = new Card.IdSorter();
		cardSet = new TreeSet<>(defaultSorter);
	}
	
	public Cards(List<Card> collect) 
	{
		this();
		cardSet.addAll(collect);
	}

	public Cards getCardsWithName(String name)
	{
		return new Cards(cardSet.stream().filter(
				card -> name.equals(card.name.getText())).collect(Collectors.toList()));
	}
	
	public List<Card> toList()
	{
		return new LinkedList<>(cardSet);
	}

	public List<Card> toSortedList()
	{
		return toSortedList(defaultSorter);
	}
	
	public List<Card> toSortedList(Comparator<Card> comparator)
	{
		List<Card> cardsList = toList();
		Collections.sort(cardsList, comparator);
		return cardsList;
	}
	
	public void add(Card card)
	{
		cardSet.add(card);
	}

	public int count()
	{
		return cardSet.size();
	}
}