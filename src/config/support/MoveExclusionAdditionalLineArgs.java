package config.support;

import java.util.Set;
import java.util.stream.Collectors;

import data.PokemonCard;
import rom.Cards;

public class MoveExclusionAdditionalLineArgs extends PtcgAdditionalLineArgs
{
	private Set<String> allMovesNames;
	
	public MoveExclusionAdditionalLineArgs(Cards<PokemonCard> allCards)
	{
		super (allCards);
		
		allMovesNames = allCards.getAllMoves().stream().map(m -> m.name.toString().toLowerCase()).collect(Collectors.toSet());
	}

	public Set<String> getAllMovesNames() 
	{
		return allMovesNames;
	}
}
