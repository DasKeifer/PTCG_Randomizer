package randomizer.actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import randomizer.MonsterCardRandomizerWrapper;
import universal_randomizer.utils.StreamUtils;

public class SetEvoLineIdsAction extends Action {
	public SetEvoLineIdsAction()
	{
		super("Set Evo Line Ids", "Sets the evoLineId for the cards based on the current prevEvoName field");
	}
	
	@Override
	public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards)
	{
		setEvoLineIdsFirstPass(StreamUtils.group(cards.get(),
				mc -> mc.getMonsterCard().name.toString()));
		setEvoLineIdsSecondPass(StreamUtils.group(cards.get(),
				mc -> mc.getMonsterCard().name.toString()));
	}
	
	public static int setEvoLineIdsFirstPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
	{
		int nextEvoId = 1;
		for (Entry<String, List<MonsterCardRandomizerWrapper>> mcs : cards.entrySet())
		{
			MonsterCardRandomizerWrapper card = mcs.getValue().get(0);
			// if it has a valid pre evo poke (i.e is not a fossil poke), skip as we
			// will assign it in the second pass
			if (!card.getMonsterCard().prevEvoName.isEmpty() && 
					cards.get(card.getMonsterCard().prevEvoName.toString()) != null) {
				continue;
			}
			
			// Otherwise assign the id
			int thisEvoId = nextEvoId++;
			for (MonsterCardRandomizerWrapper mc : mcs.getValue())
			{
				mc.setEvolutionLineId(thisEvoId);
			}
		}
		return nextEvoId;
	}
	
	public static void setEvoLineIdsSecondPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
	{
		for (Entry<String, List<MonsterCardRandomizerWrapper>> mcs : cards.entrySet())
		{
			int thisEvoId = 0;
			MonsterCardRandomizerWrapper baseCard = mcs.getValue().get(0);
			
			// skip any basic pokes - already assigned
			if (baseCard.getMonsterCard().prevEvoName.isEmpty()) {
				continue;
			}
			
			while (!baseCard.getMonsterCard().prevEvoName.isEmpty())
			{
				List<MonsterCardRandomizerWrapper> prev = cards.get(baseCard.getMonsterCard().prevEvoName.toString());
				if (prev == null)
				{
					break;
				}
				baseCard = prev.get(0);
			}
			thisEvoId = baseCard.getEvolutionLineId();
			
			for (MonsterCardRandomizerWrapper mc : mcs.getValue())
			{
				mc.setEvolutionLineId(thisEvoId);
			}
		}
	}

}
