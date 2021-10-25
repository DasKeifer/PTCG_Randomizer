package randomizer;

import java.util.List;

import config.MoveExclusions;

public class Configs 
{
	public List<MoveExclusions> moveExclusions;
	
	public Configs()
	{
		moveExclusions = MoveExclusions.parseMoveExclusionsCsv();
	}
}
