package ptcgr.randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import ptcgr.randomizer.MonsterCardRandomizerWrapper;
import ptcgr.rom.Rom;

public interface PerformLambda {
	public void perform(Rom rom);
}
