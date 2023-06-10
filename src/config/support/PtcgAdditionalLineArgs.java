package config.support;

import data.CardGroup;
import data.MonsterCard;

public class PtcgAdditionalLineArgs 
{
	private CardGroup<MonsterCard> allPokes;
	
	public PtcgAdditionalLineArgs(CardGroup<MonsterCard> allPokes) 
	{
		this.allPokes = allPokes;
	}


	public CardGroup<MonsterCard> getAllPokes() 
	{
		return allPokes;
	}
}
