package config;


import constants.CardConstants.CardId;
import data.Move;

public class MoveExclusionData 
{
	private CardId cardId;
	private String moveName;
	private boolean removeFromCard;
	
	MoveExclusionData(CardId cardId, String moveName, boolean removeFromCard)
	{
		this.cardId = cardId;
		this.moveName = moveName;
		this.removeFromCard = removeFromCard;
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
	
	public String getMoveName()
	{
		return moveName;
	}
	
	public boolean isRemoveFromCard() 
	{
		return removeFromCard;
	}
}
