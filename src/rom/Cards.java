package rom;

import java.util.List;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

import data.Card;

public class Cards 
{
	private TreeSet<Card> cardsByName = new TreeSet<>(new Card.NameIdSorter());
	
	public List<Card> getCardsWithName(String name)
	{
		return cardsByName.stream().filter(
				card -> name.equals(card.name.getText())).collect(Collectors.toList());
	}
	
	public List<Card> getCards()
	{
		return new LinkedList<>(cardsByName);
	}
	
	public void add(Card card)
	{
		cardsByName.add(card);
	}

	public int count()
	{
		return cardsByName.size();
	}
}
