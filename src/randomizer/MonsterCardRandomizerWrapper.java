package randomizer;

import data.MonsterCard;
import constants.CardDataConstants.EnergyType;

public class MonsterCardRandomizerWrapper
{
	private MonsterCard monsterCard;
	private int numMoves;
	private EnergyType[] moveTypes;
	private int evolutionLineId;

	public MonsterCardRandomizerWrapper(MonsterCard mc)
	{
		// default everything to 0. They will be determined if needed
		// prior to using them
		setMonsterCard(mc);
		setNumMoves(0);
		setEvolutionLineId(0);
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
	
	public int getEvolutionLineId() 
	{
		return evolutionLineId;
	}

	public void setEvolutionLineId(int evolutionLineId) 
	{
		this.evolutionLineId = evolutionLineId;
	}
}
