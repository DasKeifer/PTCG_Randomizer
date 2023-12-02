package ptcgr.config.support;

import ptcgr.data.CardGroup;
import ptcgr.data.MonsterCard;

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
