package randomizer.categories;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import constants.CardDataConstants.CardType;
import constants.CardDataConstants.EnergyType;
import data.Move;
import randomizer.MonsterCardRandomizerWrapper;
import randomizer.actions.ActionBank;
import randomizer.actions.ActionCategories;
import randomizer.actions.LambdaAction;
import universal_randomizer.utils.StreamUtils;

public class MovesRandomizer
{
	public static void addActions(ActionBank actionBank) 
	{
		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES, 
				"All Moves Colorless", 
				"Sets all energy costs of all moves to only be colorless",
				cards -> { changeAllMovesTypes(cards.get(), EnergyType.COLORLESS);}));
		
		actionBank.add(new LambdaAction(ActionCategories.CATEGORY_MOVES, 
				"All Moves Match Type",
				"Sets all energy costs of all moves to match the type of the card",
				cards -> { 
					Map<CardType, List<MonsterCardRandomizerWrapper>> byType = 
							StreamUtils.group(cards.get(), mc -> mc.getMonsterCard().type);
					
					// Do one energy type at a time
					for (Entry<CardType, List<MonsterCardRandomizerWrapper>> entry : byType.entrySet())
					{				
						// Determine the number of moves per monster for this type
						changeAllMovesTypes(entry.getValue().stream(), 
								entry.getKey().convertToEnergyType());
					}
				}));
	}
	
	private static void changeAllMovesTypes(Stream<MonsterCardRandomizerWrapper> pokes, EnergyType type)
	{
		pokes.forEach(poke -> {
			List<Move> moves = poke.getMonsterCard().getAllMovesIncludingEmptyOnes();
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
			poke.getMonsterCard().setMoves(moves);
		});
	}
}
