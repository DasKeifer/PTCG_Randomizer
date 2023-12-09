package ptcgr.randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import ptcgr.randomizer.MonsterCardRandomizerWrapper;
import ptcgr.rom.Rom;

public class LambdaAction  extends Action {
	
	PerformLambda perform;
	
	public LambdaAction(String category, String name, String description, PerformLambda perform)
	{
		super (category, name, description);
		this.perform = perform;
	}

	@Override
	public void Perform(Rom rom) {
		perform.perform(rom);
	}
}
