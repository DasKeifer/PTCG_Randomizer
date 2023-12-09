package ptcgr.randomizer.categories;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gbc_framework.utils.Logger;
import ptcgr.constants.CardDataConstants.CardType;
import ptcgr.constants.CardDataConstants.EnergyType;
import ptcgr.data.MonsterCard;
import ptcgr.data.Move;
import ptcgr.randomizer.actions.ActionBank;
import ptcgr.randomizer.actions.ActionCategories;
import ptcgr.randomizer.actions.LambdaAction;
import universal_randomizer.pool.ReusePool;
import universal_randomizer.randomize.SingleRandomizer;
import universal_randomizer.user_object_apis.MultiSetterNoReturn;
import universal_randomizer.user_object_apis.SetterNoReturn;
import universal_randomizer.utils.StreamUtils;

public class MovesRandomizer
{
	public static void addActions(ActionBank actionBank, Logger logger) 
	{
		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES,
				"Set Num Moves based on Rom",
				"Sets the number of moves for each card",
				rom -> {
		        	SetterNoReturn<MonsterCard, Integer> setter = (mc, count) -> mc.set("numMoves", count);
		        	SingleRandomizer<MonsterCard, Integer> randomizer =
		        			SingleRandomizer.create(setter.asSetter());
		        	randomizer.perform(rom.allCards.cards().monsterCards().stream(), ReusePool.create( 
		        			StreamUtils.field(rom.allCards.cards().monsterCards().stream(),
		        					mc -> mc.getNumMoves()).toList()));
		    	}));
		
		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES,
				"Set Num Moves to 2",
				"Sets the number of moves to 2 for every card",
				rom -> rom.allCards.cards().monsterCards().stream().forEach(mc -> mc.set("numMoves", 2))));
		
		// Set num damaging

		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES,
				"Randomize Moves",
				"Randomizes all moves regardless of type",
				rom -> {
					List<Move> moves = rom.allCards.cards().monsterCards().stream().flatMap(
							mc -> mc.getAllNonEmptyMoves().stream()).collect(Collectors.toList());
		        	MultiSetterNoReturn<MonsterCard, Move> setter = MonsterCard::setMove;
		        	SingleRandomizer<MonsterCard, Move> randomizer =
		        			SingleRandomizer.create(setter.asMultiSetter(), MonsterCard::getNumMoves);
		        	randomizer.perform(rom.allCards.cards().monsterCards().stream(), ReusePool.create(moves));
		    	}));
//		
//		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES, 
//				"Randomize Moves within Type", 
//				"Randomizes moves within each type of card",
//				cards -> { changeAllMovesTypes(cards.get(), EnergyType.COLORLESS);}));
		// Add IsOfType(x)? or get primarytpye()? or have a cardtype from creation (what card it was on)?
		
		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES, 
				"All Moves Colorless", 
				"Sets all energy costs of all moves to only be colorless",
				rom -> { changeAllMovesTypes(rom.allCards.cards().monsterCards().stream(), EnergyType.COLORLESS);}));
		
		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES, 
				"All Moves Match Type",
				"Sets all energy costs of all moves to match the type of the card",
				rom -> { 
					Map<CardType, List<MonsterCard>> byType = 
							StreamUtils.group(rom.allCards.cards().monsterCards().stream(), mc -> mc.type);
					
					// Do one energy type at a time
					for (Entry<CardType, List<MonsterCard>> entry : byType.entrySet())
					{				
						// Determine the number of moves per monster for this type
						changeAllMovesTypes(entry.getValue().stream(), 
								entry.getKey().convertToEnergyType());
					}
				}));
	}
	
	private static void changeAllMovesTypes(Stream<MonsterCard> pokes, EnergyType type)
	{
		pokes.forEach(poke -> {
			List<Move> moves = poke.getAllMovesIncludingEmptyOnes();
			for (Move move : moves)
			{
				// Get the current data and then clear it
				byte colorlessCost = move.getCost(EnergyType.COLORLESS);
				byte nonColorlessCost = move.getNonColorlessEnergyCosts();
				move.clearCosts();
				
				// If we are setting to colorless, we need to add the
				// two together
				if (type == EnergyType.COLORLESS)
				{
					move.setCost(EnergyType.COLORLESS, (byte) (colorlessCost + nonColorlessCost));
				}
				// Otherwise set the colorless back and set the non colorless
				// to the new type
				else
				{
					move.setCost(EnergyType.COLORLESS, colorlessCost);
					move.setCost(type, nonColorlessCost);
				}
			}
			
			// Copy the moves back over
			poke.setMoves(moves);
		});
	}
}
