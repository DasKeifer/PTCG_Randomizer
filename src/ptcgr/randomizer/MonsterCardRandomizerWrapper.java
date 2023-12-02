package ptcgr.randomizer;

import ptcgr.constants.CardDataConstants.EnergyType;
import ptcgr.constants.CardDataConstants.EvolutionStage;
import ptcgr.data.MonsterCard;

public class MonsterCardRandomizerWrapper
{
	private MonsterCard monsterCard;
	private int numMoves;
	private EnergyType[] moveTypes;
	private int evolutionLineId;
	private EvolutionStage evoLineMaxStage;

	public MonsterCardRandomizerWrapper(MonsterCard mc)
	{
		// default everything to 0. They will be determined if needed
		// prior to using them
		setMonsterCard(mc);
		setNumMoves(0);
		setEvoLineId(0);
		setEvoLineMaxStage(EvolutionStage.BASIC);
	}

	public MonsterCard getMonsterCard() {
		return monsterCard;
	}

	protected void setMonsterCard(MonsterCard monsterCard) {
		this.monsterCard = monsterCard;
	}

	public int getNumMoves() {
		return numMoves;
	}

	public boolean setNumMoves(int numMoves) {
		boolean okay = numMoves >= 0;
		if (okay)
		{
			this.numMoves = numMoves;
			moveTypes = new EnergyType[numMoves];
			for (int i = 0; i < numMoves; i++)
			{
				moveTypes[i] = EnergyType.COLORLESS;
			}
		}
		return okay;
	}

	protected EnergyType[] getMoveTypes() 
	{
		return moveTypes;
	}
	
	public boolean setMoveType(EnergyType type, int index)
	{
		boolean okay = index < moveTypes.length;
		if (okay)
		{
			moveTypes[index] = type;
		}
		return okay;
	}
	
	public int getEvoLineId() 
	{
		return evolutionLineId;
	}

	public void setEvoLineId(int evolutionLineId) 
	{
		this.evolutionLineId = evolutionLineId;
	}
	
	public EvolutionStage getEvoLineMaxStage() 
	{
		return evoLineMaxStage;
	}

	public void setEvoLineMaxStage(EvolutionStage evoLineMaxStage)
	{
		this.evoLineMaxStage = evoLineMaxStage;
	}
}
