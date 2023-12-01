package randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import constants.CardDataConstants.EnergyType;
import randomizer.MonsterCardRandomizerWrapper;
import randomizer.MoveSetRandomizer;

public class AllMovesColorlessAction extends Action {

	protected AllMovesColorlessAction() 
	{
		super("All Moves Colorless", "Sets all energy costs of all moves to only be colorless");
	}

	@Override
	public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards) 
	{
		MoveSetRandomizer.changeAllMovesTypes(cards.get(), EnergyType.COLORLESS);
	}

}
