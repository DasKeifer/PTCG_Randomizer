package rom;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

import data.Card;

public class Cards 
{
	private Comparator<Card> sorter = new Card.IdSorter();
	private TreeSet<Card> cardSet = new TreeSet<>(sorter);
	
	public List<Card> getCardsWithName(String name)
	{
		return cardSet.stream().filter(
				card -> name.equals(card.name.getText())).collect(Collectors.toList());
	}
	
	public List<Card> getCards()
	{
		return new LinkedList<>(cardSet);
	}
	
	public List<Card> getSortedCards()
	{
		List<Card> cardsList = getCards();
		Collections.sort(cardsList, sorter);
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
