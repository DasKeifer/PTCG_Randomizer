package randomizer.categories;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import constants.CardDataConstants.CardType;
import randomizer.MonsterCardRandomizerWrapper;
import randomizer.actions.ActionBank;
import randomizer.actions.ActionCategories;
import randomizer.actions.LambdaAction;
import universal_randomizer.pool.EliminatePoolSet;
import universal_randomizer.pool.PeekPool;
import universal_randomizer.randomize.EnforceParams;
import universal_randomizer.randomize.SingleRandomizer;
import universal_randomizer.user_object_apis.SetterNoReturn;
import universal_randomizer.utils.StreamUtils;

public class CardsRandomizer
{
	public static void addActions(ActionBank actionBank) 
	{
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
		        "Set Evo Line Ids", 
		        "Sets the evoLineId for the cards based on the current prevEvoName field",
				cards -> {
					setEvoLineIdsFirstPass(StreamUtils.group(cards.get(),
							mc -> mc.getMonsterCard().name.toString()));
					setEvoLineIdsSecondPass(StreamUtils.group(cards.get(),
							mc -> mc.getMonsterCard().name.toString()));
				}));
		
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"Even Rando Evo Line Types",
				"Randomize the energy type per evolution line for all monsters to have a balanced number of cards of each type",
				cards -> {
		    		Stream<List<MonsterCardRandomizerWrapper>> byEvoLine = 
		    				StreamUtils.group(cards.get(), mc -> mc.getEvolutionLineId()).values().stream();
		        	SetterNoReturn<List<MonsterCardRandomizerWrapper>, CardType> setter = (l, t) -> { 
		        		for (MonsterCardRandomizerWrapper mc : l) 
		        		{
		        			mc.getMonsterCard().type = t;
		        		}
		        	};
		        	SingleRandomizer<List<MonsterCardRandomizerWrapper>, CardType> randomizer =
		        			SingleRandomizer.create(setter.asSetter(), EnforceParams.createNoEnforce());
		        	randomizer.perform(byEvoLine, EliminatePoolSet.create(
		        			PeekPool.create(true, CardType.monsterValues()), EliminatePoolSet.UNLIMITED_DEPTH));
		    	}));
	}
	
	private static int setEvoLineIdsFirstPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
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
	
	private static void setEvoLineIdsSecondPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
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
