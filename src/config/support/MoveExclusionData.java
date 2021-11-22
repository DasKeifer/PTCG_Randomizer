package config.support;


import constants.CardConstants.CardId;
import data.Move;

public class MoveExclusionData 
{
	private CardId cardId;
	private String moveName;
	private boolean removeFromPool;
	private boolean excludeFromRandomization;
	
	public MoveExclusionData(CardId cardId, String moveName, boolean removeFromPool, boolean excludeFromRandomization)
	{
		this.cardId = cardId;
		this.moveName = moveName;
		this.removeFromPool = removeFromPool;
		this.excludeFromRandomization = excludeFromRandomization;
	}
	
	public boolean matchesMove(CardId id, Move move)
	{
		return (!isCardIdSet() || cardId == id) &&
				(moveName.isEmpty() || moveName.equals(move.name.toString()));
	}

	public boolean isCardIdSet()
	{
		return cardId != CardId.NO_CARD;
	}
	
	public CardId getCardId()
	{
		return cardId;
	}

	public boolean isMoveNameSet() 
	{
		return !moveName.isEmpty();
	}
	
	public String getMoveName()
	{
		return moveName;
	}
	
	public boolean isRemoveFromPool() 
	{
		return removeFromPool;
	}
	
	public boolean isExcludeFromRandomization() 
	{
		return excludeFromRandomization;
	}
}
