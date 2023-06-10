package config;


import java.awt.Component;

import data.CardGroup;
import data.MonsterCard;
import rom.Rom;

public class Configs 
{
	private MoveExclusions moveExclusions;
	private MoveAssignments moveAssignments;
	
	public Configs(Rom romData, Component toCenterPopupsOn)
	{
		CardGroup<MonsterCard> allMons = romData.allCards.cards().monsterCards();

		moveExclusions = new MoveExclusions(allMons, toCenterPopupsOn);
		moveAssignments = new MoveAssignments(allMons, toCenterPopupsOn);
	}

	public MoveExclusions getMoveExclusions()
	{
		return moveExclusions;
	}

	public MoveAssignments getMoveAssignments() 
	{
		return moveAssignments;
	}
}
