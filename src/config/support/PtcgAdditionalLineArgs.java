package config.support;

import data.PokemonCard;
import rom.Cards;

public class PtcgAdditionalLineArgs 
{
	private Cards<PokemonCard> allPokes;
	
	public PtcgAdditionalLineArgs(Cards<PokemonCard> allPokes) 
	{
		this.allPokes = allPokes;
	}


	public Cards<PokemonCard> getAllPokes() 
	{
		return allPokes;
	}
}
