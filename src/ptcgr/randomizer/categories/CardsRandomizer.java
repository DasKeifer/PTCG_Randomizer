package ptcgr.randomizer.categories;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import gbc_framework.utils.Logger;
import ptcgr.constants.CardDataConstants.CardType;
import ptcgr.constants.CardDataConstants.EvolutionStage;
import ptcgr.randomizer.MonsterCardRandomizerWrapper;
import ptcgr.randomizer.actions.ActionBank;
import ptcgr.randomizer.actions.ActionCategories;
import ptcgr.randomizer.actions.LambdaAction;
import ptcgr.randomizer.actions.logActions.CardsLogAction;
import ptcgr.randomizer.actions.logActions.CardsLogAction.Column;
import ptcgr.randomizer.actions.logActions.CardsLogAction.ColumnFormat;
import ptcgr.randomizer.actions.logActions.CardsLogAction.TypeToPrint;
import universal_randomizer.pool.EliminatePoolSet;
import universal_randomizer.pool.MultiPool;
import universal_randomizer.pool.PeekPool;
import universal_randomizer.pool.RandomizerPool;
import universal_randomizer.randomize.DependentRandomizer;
import universal_randomizer.randomize.EnforceParams;
import universal_randomizer.randomize.SingleRandomizer;
import universal_randomizer.user_object_apis.Getter;
import universal_randomizer.user_object_apis.SetterNoReturn;
import universal_randomizer.utils.StreamUtils;

public class CardsRandomizer
{
	public static void addActions(ActionBank actionBank, Logger logger) 
	{
		actionBank.add(new CardsLogAction(
				ActionCategories.CATEGORY_CARDS, "Log Card Info", 
				"Log info related to card randomizations", logger, TypeToPrint.MONSTERS,
				new ColumnFormat(Column.C_NAME, "-"), new ColumnFormat(Column.C_TYPE_SHORT, ""), 
				new ColumnFormat(Column.MC_HP, ""), new ColumnFormat(Column.MC_PREV_EVO, "-"), 
				new ColumnFormat(Column.MC_EVO_ID, ""), new ColumnFormat(Column.MC_MAX_EVO_STAGE, "")));
		
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
		        "Set Evo Line Metadata", 
		        "Sets the evoLineId and maxEvoStage for the cards based on the current prevEvoName fields",
				cards -> setEvoLineData(StreamUtils.group(cards.get(), mc -> mc.getMonsterCard().name.toString()))));
		
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
				"HP by Stage from ROM",
				"Randomize the HP of cards based on their stage and max stage in the evo line weighting values based on the data in the rom",
				cards -> {
					Map<Integer, RandomizerPool<Integer>> poolMap = new HashMap<>();
					Map<EvolutionStage, List<MonsterCardRandomizerWrapper>> byMaxStage = 
							StreamUtils.group(cards.get(), MonsterCardRandomizerWrapper::getEvoLineMaxStage);
					for (Entry<EvolutionStage, List<MonsterCardRandomizerWrapper>> maxStageEntry : byMaxStage.entrySet())
					{
						Map<EvolutionStage, List<MonsterCardRandomizerWrapper>> byStage = 
								StreamUtils.group(maxStageEntry.getValue().stream(), mc -> mc.getMonsterCard().stage);
						for (Entry<EvolutionStage, List<MonsterCardRandomizerWrapper>> stageEntry : byStage.entrySet())
						{
							poolMap.put(stageAndMaxStageHash(stageEntry.getKey(), maxStageEntry.getKey()), 
									PeekPool.create(false,stageEntry.getValue().stream().map(
											mc -> Integer.valueOf(mc.getMonsterCard().getHp())).toList()));
						}
					}
					randomizeHpByStageMaxStageWithPool(cards, poolMap);
		    	}));
		
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"HP by Stage",
				"Randomize the HP of cards based on their stage and max stage in the evo line",
				cards -> {
					Map<Integer, RandomizerPool<Integer>> poolMap = new HashMap<>();
					poolMap.put(stageAndMaxStageHash(EvolutionStage.BASIC, EvolutionStage.BASIC), 
							PeekPool.create(false, 50, 50, 60, 60, 60, 70, 70, 70, 80, 90, 100, 120));

					poolMap.put(stageAndMaxStageHash(EvolutionStage.BASIC, EvolutionStage.STAGE_1),
							PeekPool.create(false, 30, 40, 40, 50, 50, 60, 70, 80));
					poolMap.put(stageAndMaxStageHash(EvolutionStage.STAGE_1, EvolutionStage.STAGE_1),
							PeekPool.create(false, 50, 60, 70, 70, 80, 90, 100));
					
					poolMap.put(stageAndMaxStageHash(EvolutionStage.BASIC, EvolutionStage.STAGE_2),
							PeekPool.create(false, 30, 30, 40, 40, 50, 50, 60));
					poolMap.put(stageAndMaxStageHash(EvolutionStage.STAGE_1, EvolutionStage.STAGE_2),
							PeekPool.create(false, 50, 60, 60, 70, 70, 80, 90));
					poolMap.put(stageAndMaxStageHash(EvolutionStage.STAGE_2, EvolutionStage.STAGE_2),
							PeekPool.create(false, 80, 90, 100, 100, 110, 120));
				
					randomizeHpByStageMaxStageWithPool(cards, poolMap);
		    	}));
	}
	
	private static int stageAndMaxStageHash(EvolutionStage stage, EvolutionStage maxStage)
	{
		return maxStage.getValue() * 3 + stage.getValue();
	}
	
	private static void randomizeHpByStageMaxStageWithPool(
			Supplier<Stream<MonsterCardRandomizerWrapper>> cards, 
			Map<Integer, RandomizerPool<Integer>> poolMap)
	{
		// DO a pre pass for hp "trend" to keep evo lines more consistent
		// assign "high" "med" and "low" for more multipools
		Getter<MonsterCardRandomizerWrapper, Integer> hpIndexGetter = 
				mc -> stageAndMaxStageHash(mc.getMonsterCard().stage, mc.getEvoLineMaxStage());
		MultiPool<MonsterCardRandomizerWrapper, Integer, Integer> hpPool = 
				MultiPool.create(poolMap, hpIndexGetter.asMultiGetter());
		
    	SetterNoReturn<MonsterCardRandomizerWrapper, Integer> setter = (mc, hp) -> mc.getMonsterCard().setHp(hp);
    	DependentRandomizer<List<MonsterCardRandomizerWrapper>, MonsterCardRandomizerWrapper, Integer, EvolutionStage> randomizer =
    			DependentRandomizer.create(
    					setter.asSetter(), 
    					Integer::compare,
    					mc -> mc.getMonsterCard().stage,
    					EvolutionStage::compareTo,
    					EnforceParams.createNoEnforce());
    	
    	randomizer.perform(StreamUtils.group(cards.get(), MonsterCardRandomizerWrapper::getEvoLineId).values().stream(), hpPool);
	}
	
	private static final Comparator<MonsterCardRandomizerWrapper> SORT_BY_HP = 
			(l, r) -> Byte.compare(l.getMonsterCard().getHp(), r.getMonsterCard().getHp());
	
	private static void enforceEvoLineHpConsistency(Supplier<Stream<MonsterCardRandomizerWrapper>> cards)
	{
		// Group by evo Id
		Collection<List<MonsterCardRandomizerWrapper>> byEvoLine = 
				StreamUtils.group(cards.get(), mc -> mc.getEvoLineId()).values();
		for (List<MonsterCardRandomizerWrapper> evoLine : byEvoLine)
		{
			// Then group by stage
			Map<EvolutionStage, List<MonsterCardRandomizerWrapper>> byStage = 
					StreamUtils.group(evoLine.stream(), mc -> mc.getMonsterCard().stage);

			// Sort each stage by hp
			for (List<MonsterCardRandomizerWrapper> stage : byStage.values())
			{
				stage.sort(SORT_BY_HP);
			}
			
			for (int i = 0; i < EvolutionStage.values().length - 1; i++)
			{
				// Get this evo stage. If we don't find it, skip to the next
				List<MonsterCardRandomizerWrapper> currEvoStage = byStage.get(EvolutionStage.values()[i]);
				if (currEvoStage == null)
				{
					continue;
				}
				
				// and now get the next one (skipping any missing stages)
				int nextIdx = i + 1;
				List<MonsterCardRandomizerWrapper> nextEvoStage = null;
				do
				{
					nextEvoStage = byStage.get(EvolutionStage.values()[nextIdx++]);
				}
				while (nextEvoStage == null && nextIdx < EvolutionStage.values().length);
				// If we didn't find a next stage we are done early
				if (nextEvoStage == null)
				{
					break;
				}
			
				// see if this highest is higher than the lowest of the next and if so, swap and resort the lists
				MonsterCardRandomizerWrapper highest = currEvoStage.get(currEvoStage.size() - 1);
				MonsterCardRandomizerWrapper lowest = nextEvoStage.get(0);
				while (highest.getMonsterCard().getHp() > lowest.getMonsterCard().getHp())
				{
					byte highestHp = highest.getMonsterCard().getHp();
					highest.getMonsterCard().setHp(lowest.getMonsterCard().getHp());
					lowest.getMonsterCard().setHp(highestHp);

					resort(currEvoStage, currEvoStage.size() - 1);
					resort(nextEvoStage, 0);
				}
			}
		}
	}
	
	private static void resort(List<MonsterCardRandomizerWrapper> list, int index)
	{
		MonsterCardRandomizerWrapper modified = list.remove(index);
		int insert = Collections.binarySearch(list, modified, SORT_BY_HP);
		// If key was not found, convert it to where it should be inserted
	    if (insert < 0) 
	    {
	    	insert = -insert - 1;
	    }
	    list.add(insert, modified);
	}
	
	private static void setEvoLineData(Map<String, List<MonsterCardRandomizerWrapper>> cards)
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
		setEvoLineData_SecondPass(cards);
	}
	
	private static void setEvoLineData_SecondPass(Map<String, List<MonsterCardRandomizerWrapper>> cards)
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
			
			// If we have a higher stage in the chain update 
			// only if higher though in case we process the 2nd
			// stage before the first stage
			if (baseCard.getEvoLineMaxStage().compareTo(cardEvoStage) < 0)
			{
				for (MonsterCardRandomizerWrapper mc : mcs.getValue())
				{
					mc.setEvoLineMaxStage(cardEvoStage);
				}
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
