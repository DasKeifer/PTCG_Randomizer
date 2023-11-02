package randomizer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static void setEvoLineIds(Stream<MonsterCardRandomizerWrapper> cards)
	{
		setEvoLineIds(cards.collect(Collectors.toMap(
				mc -> mc.getMonsterCard().name.toString(), mc -> mc)));
	}
	
	public static void setEvoLineIds(Map<String, MonsterCardRandomizerWrapper> cards)
	{
		int nextEvoId = 1;
		for (Entry<String, MonsterCardRandomizerWrapper> mc : cards.entrySet())
		{
			MonsterCardRandomizerWrapper baseCard = mc.getValue();
			while (!baseCard.monsterCard.prevEvoName.isEmpty())
			{
				baseCard = cards.get(baseCard.monsterCard.prevEvoName.toString());
			}
			if (baseCard.evolutionLineId > 0)
			{
				mc.getValue().setEvolutionLineId(baseCard.getEvolutionLineId());
			}
			else
			{
				mc.getValue().setEvolutionLineId(nextEvoId++);
			}
		}
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
	
	protected int getEvolutionLineId() 
	{
		return evolutionLineId;
	}

	protected void setEvolutionLineId(int evolutionLineId) 
	{
		this.evolutionLineId = evolutionLineId;
	}
}
