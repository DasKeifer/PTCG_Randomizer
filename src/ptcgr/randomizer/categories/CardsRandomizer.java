package ptcgr.randomizer.categories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import ptcgr.constants.CardDataConstants.CardType;
import ptcgr.constants.CardDataConstants.EvolutionStage;
import ptcgr.randomizer.MonsterCardRandomizerWrapper;
import ptcgr.randomizer.actions.ActionBank;
import ptcgr.randomizer.actions.ActionCategories;
import ptcgr.randomizer.actions.LambdaAction;
import universal_randomizer.pool.EliminatePoolSet;
import universal_randomizer.pool.MultiPool;
import universal_randomizer.pool.PeekPool;
import universal_randomizer.pool.RandomizerPool;
import universal_randomizer.randomize.EnforceParams;
import universal_randomizer.randomize.SingleRandomizer;
import universal_randomizer.user_object_apis.Getter;
import universal_randomizer.user_object_apis.SetterNoReturn;
import universal_randomizer.utils.CreationUtils;
import universal_randomizer.utils.StreamUtils;

public class CardsRandomizer
{
	public static void addActions(ActionBank actionBank) 
	{
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
		        "Set Evo Line Metadata", 
		        "Sets the evoLineId and maxEvoStage for the cards based on the current prevEvoName fields",
				cards -> {
					setEvoLineDataSecondPass(StreamUtils.group(cards.get(),
							mc -> mc.getMonsterCard().name.toString()));
					setEvoLineDataSecondPass(StreamUtils.group(cards.get(),
							mc -> mc.getMonsterCard().name.toString()));
				}));
		
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"Even Rando Evo Line Types",
				"Randomize the energy type per evolution line for all monsters to have a balanced number of cards of each type",
				cards -> {
		    		Stream<List<MonsterCardRandomizerWrapper>> byEvoLine = 
		    				StreamUtils.group(cards.get(), mc -> mc.getEvoLineId()).values().stream();
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

		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"HP by Stage",
				"Randomize the HP of cards based on their stage and max stage in the evo line",
				cards -> {
					Map<Integer, RandomizerPool<Integer>> poolMap = new HashMap<>();
					poolMap.put(0, PeekPool.create(false, 
							50, 50, 60, 60, 60, 70, 70, 70, 80, 90, 100, 120));

					poolMap.put(3, PeekPool.create(false, 
							30, 40, 40, 50, 50, 60, 70, 80));
					poolMap.put(4, PeekPool.create(false, 
							50, 60, 70, 70, 80, 90, 100));
					
					poolMap.put(6, PeekPool.create(false, 
							30, 30, 40, 40, 50, 50, 60));
					poolMap.put(7, PeekPool.create(false, 
							50, 60, 60, 70, 70, 80, 90));
					poolMap.put(8, PeekPool.create(false, 
							80, 90, 100, 100, 110, 120));
				
					Getter<MonsterCardRandomizerWrapper, Integer> hpIndexGetter = 
							mc -> mc.getEvoLineMaxStage().getValue() * 3 + mc.getMonsterCard().stage.getValue();
					MultiPool<MonsterCardRandomizerWrapper, Integer, Integer> hpPool = 
							MultiPool.create(poolMap, hpIndexGetter.asMultiGetter());
					
					// TODO Here
		    		Stream<List<MonsterCardRandomizerWrapper>> byEvoLine = 
		    				StreamUtils.group(cards.get(), mc -> mc.getMonsterCard()).values().stream();
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
	
	private static int setEvoLineDataFirstPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
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
				mc.setEvoLineId(thisEvoId);
				// Set it to its current stage. Later we will overwrite this potentially
				mc.setEvoLineMaxStage(mc.getMonsterCard().stage);
			}
		}
		return nextEvoId;
	}
	
	private static void setEvoLineDataSecondPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
	{
		for (Entry<String, List<MonsterCardRandomizerWrapper>> mcs : cards.entrySet())
		{
			int thisEvoId = 0;
			MonsterCardRandomizerWrapper baseCard = mcs.getValue().get(0);
			EvolutionStage cardEvoStage = baseCard.getMonsterCard().stage;
			
			// skip any basic pokes - already assigned
			if (baseCard.getMonsterCard().prevEvoName.isEmpty()) 
			{
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
				
				// If we have a higher stage in the chain update 
				// only if higher though in case we process the 2nd
				// stage before the first stage
				if (baseCard.getEvoLineMaxStage().compareTo(cardEvoStage) < 0)
				{
					for (MonsterCardRandomizerWrapper mc : prev)
					{
						mc.setEvoLineMaxStage(cardEvoStage);
					}
				}
			}
			thisEvoId = baseCard.getEvoLineId();
			
			for (MonsterCardRandomizerWrapper mc : mcs.getValue())
			{
				mc.setEvoLineId(thisEvoId);
			}
		}
	}
}
