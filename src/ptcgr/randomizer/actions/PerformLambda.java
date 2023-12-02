package ptcgr.randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import ptcgr.randomizer.MonsterCardRandomizerWrapper;

public interface PerformLambda {
	public void perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards);
}
