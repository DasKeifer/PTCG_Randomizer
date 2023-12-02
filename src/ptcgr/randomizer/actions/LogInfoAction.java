package ptcgr.randomizer.actions;

import java.util.function.Supplier;
import java.util.stream.Stream;

import gbc_framework.utils.Logger;
import ptcgr.randomizer.MonsterCardRandomizerWrapper;

public class LogInfoAction extends Action {
	Logger log;
	String rowFormat;
	ColumnFormat[] columnFormats;
	
	public enum Column {
		CARD_NAME("Card Name", 15), CARD_TYPE_SHORT("Type", 4), CARD_HP("HP", 4), CARD_PREV_EVO("Prev Evo", 20), 
		CARD_EVO_ID("EvoId", 5), CARD_MAX_EVO_STAGE("MaxStage", 8);

		private String title;
		private int size;
		Column(String title, int size) 
		{
			this.title = title;
			this.size = size;
		}
		
		public String getTitle()
		{
			return title;
		}
		
		public int getSize()
		{
			return size;
		}
	}
	
	public static class ColumnFormat
	{
		Column column;
		String format;
		
		public ColumnFormat(Column col, String format)
		{
			this.column = col;
			this.format = format;
		}

		public Column getColumn() 
		{
			return column;
		}

		public String getFormat()
		{
			return format;
		}
	}
	
	public LogInfoAction(String category, String name, String description, Logger log, ColumnFormat... columnFormats)
	{
		super(category, name, description);
		this.log = log;
		this.columnFormats = columnFormats;
	}
	
	@Override
	public void Perform(Supplier<Stream<MonsterCardRandomizerWrapper>> cards)
	{
		// Start size at separators between columns and add each columns length
		int totalSize = columnFormats.length + 1;
		String[] formats = new String[columnFormats.length];
		String[] titles = new String[columnFormats.length];
		int[] size = new int[columnFormats.length];
		for (int i = 0; i < columnFormats.length; i++)
		{
			formats[i] = columnFormats[i].getFormat();
			titles[i] = columnFormats[i].getColumn().getTitle();
			size[i] = columnFormats[i].getColumn().getSize();
			totalSize += size[i];
		}
	
		String separator = Logger.createSeparatorLine(totalSize);
		rowFormat = Logger.createTableFormatString(size, formats);
		
		log.println(separator);
		log.println(Logger.createTableTitle(getName(), totalSize));
		log.printf(rowFormat, (Object[])titles);
		log.println(separator);
		
		cards.get().forEach(this::printCard);
		
		log.println(separator);
	}
	
	public void printCard(MonsterCardRandomizerWrapper card)
	{
		Object[] entries = new Object[columnFormats.length];
		for (int i = 0; i < columnFormats.length; i++)
		{
			switch (columnFormats[i].getColumn()) {
				case CARD_NAME: entries[i] = card.getMonsterCard().name.toString(); break;
				case CARD_TYPE_SHORT: entries[i] = card.getMonsterCard().type.convertToEnergyType().getAbbreviation(); break;
				case CARD_HP: entries[i] = card.getMonsterCard().getHp(); break;
				case CARD_PREV_EVO: entries[i] = card.getMonsterCard().prevEvoName; break;
				case CARD_EVO_ID: entries[i] = card.getEvoLineId(); break; 
				case CARD_MAX_EVO_STAGE: entries[i] = card.getEvoLineMaxStage().getValue(); break;
				default: entries[i] = "ERR"; break;
			}
		}
		log.printf(rowFormat, entries);
	}
}
