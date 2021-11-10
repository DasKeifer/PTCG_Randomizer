package config;


import constants.CardConstants.CardId;
import data.Move;

public class MoveAssignmentData 
{
	private CardId cardId;
	private int moveSlot;
	private Move move;
	
	MoveAssignmentData(CardId cardId, int moveSlot, Move move)
	{
		this.cardId = cardId;
		this.moveSlot = moveSlot;
		this.move = new Move(move);
	}
	
	public CardId getCardId()
	{
		return cardId;
	}
	
	public int getMoveSlot()
	{
		return moveSlot;
	}
	
	public	Move getMove()
	{
		return move;
	}
}
