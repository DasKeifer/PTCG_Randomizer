package config;


import java.awt.Component;

import rom.Rom;

public class Configs 
{
	public MoveExclusions moveExclusions;
	public MoveAssignments moveAssignments;
	
	public Configs(Rom romData, Component toCenterPopupsOn)
	{
		moveExclusions = MoveExclusions.parseMoveExclusionsCsv(romData.allCards, toCenterPopupsOn);
		moveAssignments = MoveAssignments.parseMoveAssignmentsCsv(romData.allCards, toCenterPopupsOn);
	}
}
