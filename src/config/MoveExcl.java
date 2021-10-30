package config;

import java.util.EnumMap;

import constants.CardConstants.CardId;
import constants.CardDataConstants.EnergyType;
import data.Move;

public class MoveExcl 
{
	CardId cardId;
	String moveName;
	byte moveDamage;
	EnumMap<EnergyType, Byte> energyCost;
	boolean removeFromCard;
	
	MoveExcl(CardId cardId, String moveName, byte moveDamage, EnumMap<EnergyType, Byte> energyCost, boolean removeFromCard)
	{
		this.cardId = cardId;
		this.moveName = moveName;
		this.moveDamage = moveDamage;
		this.energyCost = new EnumMap<>(energyCost);
		this.removeFromCard = removeFromCard;
	}
	
	public boolean matchesMove(CardId id, Move move)
	{
		return (cardId == CardId.NO_CARD || cardId == id) &&
				(moveName.isEmpty() || moveName.equals(move.name.toString())) &&
				(moveDamage == -1 || moveDamage == move.damage);
				// TODO: energy
	}
}
