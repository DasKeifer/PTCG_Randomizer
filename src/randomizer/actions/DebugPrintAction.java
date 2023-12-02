package randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import gbc_framework.utils.Logger;
import randomizer.MonsterCardRandomizerWrapper;

public class DebugPrintAction extends Action {
	Logger log;
	String rowFormat;
	
	public DebugPrintAction(Logger log)
	{
		super(ActionCategories.CATEGORY_TWEAKS, "Print Card Info", "Shouldn't see this...");
		this.log = log;
	}
	
	@Override
	public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards)
	{
		int[] size = {15,4,20,5};
		String[] titles = {"Name", "Type", "Prev Evo", "EvoId"};
		String separator = Logger.createSeparatorLine(50);
		rowFormat = Logger.createTableFormatString(size, "-", "", "-", "");
		
		log.println(separator);
		log.println(Logger.createTableTitle("Debug Print", 50));
		log.printf(rowFormat, (Object[])titles);
		log.println(separator);
		
		cards.get().forEach(this::printCard);
		
		log.println(separator);
	}
	
	public void printCard(MonsterCardRandomizerWrapper card)
	{
		log.printf(rowFormat, 
				card.getMonsterCard().name, 
				card.getMonsterCard().type.convertToEnergyType().getAbbreviation(),
				card.getMonsterCard().prevEvoName, 
				card.getEvolutionLineId());
	}
}
