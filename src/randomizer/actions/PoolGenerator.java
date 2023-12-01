package randomizer.actions;

import java.util.stream.Stream;

import randomizer.MonsterCardRandomizerWrapper;
import universal_randomizer.pool.RandomizerMultiPool;

public interface PoolGenerator<P> {
	public RandomizerMultiPool<MonsterCardRandomizerWrapper, P> generatePool(Stream<MonsterCardRandomizerWrapper> cards);
}
