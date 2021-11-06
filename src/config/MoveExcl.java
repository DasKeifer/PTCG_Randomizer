package config;


import constants.CardConstants.CardId;
import data.Move;

public class MoveExcl 
{
	CardId cardId;
	String moveName;
	boolean removeFromCard;
	
	MoveExcl(CardId cardId, String moveName, boolean removeFromCard)
	{
		this.cardId = cardId;
		this.moveName = moveName;
		this.removeFromCard = removeFromCard;
	}
	
	public boolean matchesMove(CardId id, Move move)
	{
		return (cardId == CardId.NO_CARD || cardId == id) &&
				(moveName.isEmpty() || moveName.equals(move.name.toString()));
	}
}
