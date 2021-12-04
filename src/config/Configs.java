package config;


import java.awt.Component;

import data.CardGroup;
import data.PokemonCard;
import rom.Rom;

public class Configs 
{
	private MoveExclusions moveExclusions;
	private MoveAssignments moveAssignments;
	
	public Configs(Rom romData, Component toCenterPopupsOn)
	{
		CardGroup<PokemonCard> allPokes = romData.allCards.cards().pokemonCards();

		moveExclusions = new MoveExclusions(allPokes, toCenterPopupsOn);
		moveAssignments = new MoveAssignments(allPokes, toCenterPopupsOn);
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
