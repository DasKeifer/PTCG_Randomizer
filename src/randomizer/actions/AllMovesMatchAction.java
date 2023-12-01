package randomizer.actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import constants.CardDataConstants.CardType;
import randomizer.MonsterCardRandomizerWrapper;
import randomizer.MoveSetRandomizer;
import universal_randomizer.utils.StreamUtils;

public class AllMovesMatchAction extends Action {

	protected AllMovesMatchAction() 
	{
		super("All Moves Match Type", "Sets all energy costs of all moves to match the type of the card");
	}

	@Override
	public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards) {
		Map<CardType, List<MonsterCardRandomizerWrapper>> byType = 
				StreamUtils.group(cards.get(), mc -> mc.getMonsterCard().type);
		
		// Do one energy type at a time
		for (Entry<CardType, List<MonsterCardRandomizerWrapper>> entry : byType.entrySet())
		{				
			// Determine the number of moves per monster for this type
			MoveSetRandomizer.changeAllMovesTypes(entry.getValue().stream(), 
					entry.getKey().convertToEnergyType());
		}	
	}

}
