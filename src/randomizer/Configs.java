package randomizer;


import config.MoveExclusions;
import rom.Rom;

public class Configs 
{
	public MoveExclusions moveExclusions;
	
	public Configs(Rom romData)
	{
		moveExclusions = MoveExclusions.parseMoveExclusionsCsv(romData.allCards);
	}
}
