package config.support;

import data.CardGroup;
import data.PokemonCard;

public class PtcgAdditionalLineArgs 
{
	private CardGroup<PokemonCard> allPokes;
	
	public PtcgAdditionalLineArgs(CardGroup<PokemonCard> allPokes) 
	{
		this.allPokes = allPokes;
	}


	public CardGroup<PokemonCard> getAllPokes() 
	{
		return allPokes;
	}
}
