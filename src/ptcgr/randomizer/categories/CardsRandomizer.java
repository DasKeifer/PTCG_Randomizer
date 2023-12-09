package ptcgr.randomizer.categories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import gbc_framework.utils.Logger;
import ptcgr.constants.CardDataConstants.CardType;
import ptcgr.constants.CardDataConstants.EvolutionStage;
import ptcgr.data.MonsterCard;
import ptcgr.randomizer.actions.ActionBank;
import ptcgr.randomizer.actions.ActionCategories;
import ptcgr.randomizer.actions.LambdaAction;
import ptcgr.randomizer.actions.logActions.CardsLogAction;
import ptcgr.randomizer.actions.logActions.CardsLogAction.Column;
import ptcgr.randomizer.actions.logActions.CardsLogAction.ColumnFormat;
import ptcgr.randomizer.actions.logActions.CardsLogAction.TypeToPrint;
import universal_randomizer.pool.EliminatePool;
import universal_randomizer.pool.EliminatePoolSet;
import universal_randomizer.pool.MultiPool;
import universal_randomizer.pool.ReusePool;
import universal_randomizer.pool.RandomizerPool;
import universal_randomizer.randomize.DependentRandomizer;
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
				rom -> setEvoLineData(StreamUtils.group(rom.allCards.cards().monsterCards().stream(),
						mc -> mc.name.toString()))));
		
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"Even Rando Evo Line Types",
				"Randomize the energy type per evolution line for all monsters to have a balanced number of cards of each type",
				rom -> {
		    		Stream<List<MonsterCard>> byEvoLine = 
		    				StreamUtils.group(rom.allCards.cards().monsterCards().stream(),
		    						mc -> mc.get("evoLineId")).values().stream();
		        	SetterNoReturn<List<MonsterCard>, CardType> setter = (l, t) -> { 
		        		for (MonsterCard mc : l) 
		        		{
		        			mc.type = t;
		        		}
		        	};
		        	SingleRandomizer<List<MonsterCard>, CardType> randomizer =
		        			SingleRandomizer.create(setter.asSetter());
		        	randomizer.perform(byEvoLine, EliminatePoolSet.create(
		        			EliminatePool.create(CardType.monsterValues()), EliminatePoolSet.UNLIMITED_DEPTH));
		    	}));

		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"HP by Stage from ROM",
				"Randomize the HP of cards based on their stage and max stage in the evo line weighting values based on the data in the rom",
				rom -> {
					Map<Integer, RandomizerPool<Integer>> poolMap = new HashMap<>();
					Map<EvolutionStage, List<MonsterCard>> byMaxStage = 
							StreamUtils.group(rom.allCards.cards().monsterCards().stream(), 
									mc -> (EvolutionStage) mc.get("evoLineMaxStage"));
					for (Entry<EvolutionStage, List<MonsterCard>> maxStageEntry : byMaxStage.entrySet())
					{
						Map<EvolutionStage, List<MonsterCard>> byStage = 
								StreamUtils.group(maxStageEntry.getValue().stream(), mc -> mc.stage);
						for (Entry<EvolutionStage, List<MonsterCard>> stageEntry : byStage.entrySet())
						{
							poolMap.put(stageAndMaxStageHash(stageEntry.getKey(), maxStageEntry.getKey()), 
									ReusePool.create(stageEntry.getValue().stream().map(
											mc -> Integer.valueOf(mc.getHp())).toList()));
						}
					}
					randomizeHpByStageMaxStageWithPool(rom.allCards.cards().monsterCards().stream(), poolMap);
		    	}));
		
		actionBank.add(new LambdaAction(
				ActionCategories.CATEGORY_CARDS,
				"HP by Stage",
				"Randomize the HP of cards based on their stage and max stage in the evo line",
				rom -> {
					Map<Integer, RandomizerPool<Integer>> poolMap = new HashMap<>();
					poolMap.put(stageAndMaxStageHash(EvolutionStage.BASIC, EvolutionStage.BASIC), 
							ReusePool.create(50, 50, 60, 60, 60, 70, 70, 70, 80, 90, 100, 120));

					poolMap.put(stageAndMaxStageHash(EvolutionStage.BASIC, EvolutionStage.STAGE_1),
							ReusePool.create(30, 40, 40, 50, 50, 60, 70, 80));
					poolMap.put(stageAndMaxStageHash(EvolutionStage.STAGE_1, EvolutionStage.STAGE_1),
							ReusePool.create(50, 60, 70, 70, 80, 90, 100));
					
					poolMap.put(stageAndMaxStageHash(EvolutionStage.BASIC, EvolutionStage.STAGE_2),
							ReusePool.create(30, 30, 40, 40, 50, 50, 60));
					poolMap.put(stageAndMaxStageHash(EvolutionStage.STAGE_1, EvolutionStage.STAGE_2),
							ReusePool.create(50, 60, 60, 70, 70, 80, 90));
					poolMap.put(stageAndMaxStageHash(EvolutionStage.STAGE_2, EvolutionStage.STAGE_2),
							ReusePool.create(80, 90, 100, 100, 110, 120));
				
					randomizeHpByStageMaxStageWithPool(rom.allCards.cards().monsterCards().stream(), poolMap);
		    	}));
	}
	
	private static int stageAndMaxStageHash(EvolutionStage stage, EvolutionStage maxStage)
	{
		return maxStage.getValue() * 3 + stage.getValue();
	}
	
	private static void randomizeHpByStageMaxStageWithPool(
			Stream<MonsterCard> cards, 
			Map<Integer, RandomizerPool<Integer>> poolMap)
	{
		// DO a pre pass for hp "trend" to keep evo lines more consistent
		// assign "high" "med" and "low" for more multipools
		Getter<MonsterCard, Integer> hpIndexGetter = 
				mc -> stageAndMaxStageHash(mc.stage, (EvolutionStage) mc.get("evoLineMaxStage"));
		MultiPool<MonsterCard, Integer, Integer> hpPool = 
				MultiPool.create(poolMap, hpIndexGetter.asMultiGetter());
		
    	SetterNoReturn<MonsterCard, Integer> setter = (mc, hp) -> mc.setHp(hp);
    	DependentRandomizer<List<MonsterCard>, MonsterCard, Integer, EvolutionStage> randomizer =
    			DependentRandomizer.create(
    					setter.asSetter(), 
    					Integer::compare,
    					mc -> mc.stage,
    					EvolutionStage::compareTo);
    	
    	randomizer.perform(StreamUtils.group(cards, mc -> mc.get("evoLineId")).values().stream(), hpPool);
	}
	
	private static void setEvoLineData(Map<String, List<MonsterCard>> cards)
	{
		int nextEvoId = 1;
		for (Entry<String, List<MonsterCard>> mcs : cards.entrySet())
		{
			MonsterCard card = mcs.getValue().get(0);
			// if it has a valid pre evo poke (i.e is not a fossil poke), skip as we
			// will assign it in the second pass
			if (!card.prevEvoName.isEmpty() && 
					cards.get(card.prevEvoName.toString()) != null) {
				continue;
			}
			
			// Otherwise assign the id
			int thisEvoId = nextEvoId++;
			for (MonsterCard mc : mcs.getValue())
			{
				mc.set("evoLineId", thisEvoId);
				// Set it to its current stage. Later we will overwrite this potentially
				mc.set("evoLineMaxStage", mc.stage);
			}
		}
		setEvoLineData_SecondPass(cards);
	}
	
	private static void setEvoLineData_SecondPass(Map<String, List<MonsterCard>> cards)
	{
		for (Entry<String, List<MonsterCard>> mcs : cards.entrySet())
		{
			int thisEvoId = 0;
			MonsterCard baseCard = mcs.getValue().get(0);
			EvolutionStage cardEvoStage = baseCard.stage;
			
			// skip any basic pokes - already assigned
			if (baseCard.prevEvoName.isEmpty()) 
			{
				continue;
			}
			
			// If we have a higher stage in the chain update 
			// only if higher though in case we process the 2nd
			// stage before the first stage
			if (cardEvoStage.compareTo((EvolutionStage)baseCard.get("evoLineMaxStage", EvolutionStage.BASIC)) > 0)
			{
				for (MonsterCard mc : mcs.getValue())
				{
					mc.set("evoLineMaxStage", cardEvoStage);
				}
			}
			
			while (!baseCard.prevEvoName.isEmpty())
			{
				List<MonsterCard> prev = cards.get(baseCard.prevEvoName.toString());
				if (prev == null)
				{
					break;
				}
				baseCard = prev.get(0);
				
				// If we have a higher stage in the chain update 
				// only if higher though in case we process the 2nd
				// stage before the first stage
				if (cardEvoStage.compareTo((EvolutionStage) baseCard.get("evoLineMaxStage", EvolutionStage.BASIC)) > 0)
				{
					for (MonsterCard mc : prev)
					{
						mc.set("evoLineMaxStage", cardEvoStage);
					}
				}
			}
			thisEvoId = (int) baseCard.get("evoLineId");
			
			for (MonsterCard mc : mcs.getValue())
			{
				mc.set("evoLineId", thisEvoId);
			}
		}
	}
}
