package rom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gameData.Card;
import gameData.CardVersions;

public class Cards 
{
	private Map<String, CardVersions> cardsByName = new HashMap<>();
	
	public CardVersions getCardsWithName(String name)
	{
		name = addEnglishCharTypeCharIfNotSet(name);
		addNameIfNotExists(name);
		return cardsByName.get(name);
	}
	
	public void addCard(String name, Card card)
	{
		name = addEnglishCharTypeCharIfNotSet(name);
		addNameIfNotExists(name);
		cardsByName.get(name).versions.add(card);
	}
	
	private String addEnglishCharTypeCharIfNotSet(String name)
	{
		if (name.getBytes()[0] != ((char)0x06))
		{
			name = (char)0x06 + name;
		}
		return name;
	}
	
	private void addNameIfNotExists(String name)
	{
		if (!cardsByName.containsKey(name))
		{
			CardVersions versions = new CardVersions();
			versions.name = name;
			cardsByName.put(name, versions);
		}
	}

	public int count()
	{
		int count = 0;
		for (CardVersions versions : cardsByName.values())
		{
			count += versions.count();
		}
		return count;
	}

	public Collection<CardVersions> getCards() 
	{
		return cardsByName.values();
	}
}
