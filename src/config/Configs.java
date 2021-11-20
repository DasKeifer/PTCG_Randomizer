package config;


import java.awt.Component;

import data.PokemonCard;
import rom.Cards;
import rom.Rom;

public class Configs 
{
	public MoveExclusions moveExclusions;
	public MoveAssignments moveAssignments;
	
	public Configs(Rom romData, Component toCenterPopupsOn)
	{
		Cards<PokemonCard> allPokes = romData.allCards.getPokemonCards();
		
		moveExclusions = new MoveExclusions(allPokes, toCenterPopupsOn);
		moveAssignments = new MoveAssignments(allPokes, toCenterPopupsOn);
	}
}
