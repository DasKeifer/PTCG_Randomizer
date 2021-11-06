package randomizer;


import java.awt.Component;

import config.MoveExclusions;
import rom.Rom;

public class Configs 
{
	public MoveExclusions moveExclusions;
	
	public Configs(Rom romData, Component toCenterPopupsOn)
	{
		moveExclusions = MoveExclusions.parseMoveExclusionsCsv(romData.allCards, toCenterPopupsOn);
	}
}
