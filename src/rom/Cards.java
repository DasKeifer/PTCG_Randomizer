package rom;

import java.util.List;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

import gameData.Card;

public class Cards 
{
	private TreeSet<Card> cardsByName = new TreeSet<>(new Card.NameIdSorter());
	
	public List<Card> getCardsWithName(String name)
	{
		final String nameWithChar = addEnglishCharTypeCharIfNotSet(name);
		return cardsByName.stream().filter(
				card -> nameWithChar.equals(card.name)).collect(Collectors.toList());
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
	
	private static String addEnglishCharTypeCharIfNotSet(String name)
	{
		if (name.getBytes()[0] != ((char)0x06))
		{
			name = (char)0x06 + name;
		}
		return name;
	}
	
	public static String removeEnglishCharTypeCharIfPresent(String name)
	{
		if (name.startsWith("" + (char)0x06))
		{
			name = name.substring(1);
		}
		return name;
	}
}
