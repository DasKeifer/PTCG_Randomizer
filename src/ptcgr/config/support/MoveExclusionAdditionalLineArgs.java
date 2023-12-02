package ptcgr.config.support;

import java.util.Set;
import java.util.stream.Collectors;

import ptcgr.data.CardGroup;
import ptcgr.data.MonsterCard;

public class MoveExclusionAdditionalLineArgs extends PtcgAdditionalLineArgs
{
	private Set<String> allMovesNames;
	
	public MoveExclusionAdditionalLineArgs(CardGroup<MonsterCard> allCards)
	{
		super (allCards);
		
		allMovesNames = allCards.allMoves().stream().map(m -> m.name.toString().toLowerCase()).collect(Collectors.toSet());
	}

	public Set<String> getAllMovesNames() 
	{
		return allMovesNames;
	}
}
