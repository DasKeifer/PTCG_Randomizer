package randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import randomizer.MonsterCardRandomizerWrapper;

public class LambdaAction  extends Action {
	
	PerformLambda perform;
	
	public LambdaAction(String name, String description, PerformLambda perform)
	{
		super (name, description);
		this.perform = perform;
	}

	@Override
	public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards) {
		perform.perform(cards);
	}
}
