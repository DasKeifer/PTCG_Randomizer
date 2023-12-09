package ptcgr.randomizer.actions.logActions;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import gbc_framework.utils.Logger;
import ptcgr.constants.CardDataConstants.EvolutionStage;
import ptcgr.data.MonsterCard;
import ptcgr.data.NonMonsterCard;
import ptcgr.randomizer.MonsterCardRandomizerWrapper;
import ptcgr.randomizer.actions.Action;
import ptcgr.rom.Rom;

public class CardsLogAction extends Action 
{
	public enum TypeToPrint { ALL, MONSTERS, NON_MONSTER };
	
	private Logger log;
	private TypeToPrint toPrint;
	private ColumnFormat[] columnFormats;
	private String rowFormat;
	
	public enum Column {
		C_ID("CID", 4), C_NAME("Card Name", 15), C_TYPE_SHORT("Type", 4), C_GFX("GFX ID", 6), 
		C_RARITY("Rarity", 6), C_SET("Set", 3), C_PACK("pack", 4),
		
		MC_HP("HP", 4), MC_STAGE("Stage", 5), MC_PREV_EVO("Prev Evo", 20),
		MC_MOVE_1_NAME("Move 1", 15), MC_MOVE_2_NAME("Move 2", 15),
		// TODO: More move stuff?
		MC_RETREAT("HP", 4), MC_WEAKNESS("Weakness", 8), MC_RESISTANCE("Resist", 8),
		MC_CATEGORY("Category", 10), MC_DEX_NUMBER("DexNum", 6), MC_UNKNOWN_1("Unkn1", 5),
		MC_LEVEL("Lvl", 3), MC_LENGTH("Length", 6), MC_WEIGHT("Wght", 4),
		MC_DESCRIPTION_ID("DescrId", 7), MC_UNKNOWN_2("Unkn2", 5),
		MC_EVO_ID("EvoId", 5), MC_MAX_EVO_STAGE("MaxStage", 8),
		
		NMC_EFFECT_ID("EffId", 5), NMC_EFFECT_DESCRIPTION_ID("EffDescrId", 10);

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
	
	public CardsLogAction(String category, String name, String description, Logger log, TypeToPrint toPrint, ColumnFormat... columnFormats)
	{
		super(category, name, description);
		this.log = log;
		this.toPrint = toPrint;
		this.columnFormats = columnFormats;
	}
	
	@Override
	public void Perform(Rom rom)
	{
		Supplier<Stream<MonsterCard>> mcs = () -> rom.allCards.cards().monsterCards().stream();
		
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
		
		mcs.get().forEach(this::printCard);
		
		log.println(separator);
	}

	
	public void printCard(MonsterCard card)
	{
		Object[] entries = new Object[columnFormats.length];
		for (int i = 0; i < columnFormats.length; i++)
		{
			switch (columnFormats[i].getColumn()) 
			{
				case C_ID: entries[i] = card.id; break;
				case C_NAME: entries[i] = card.name; break;
				case C_TYPE_SHORT: entries[i] = card.type.convertToEnergyType().getAbbreviation(); break;
				case C_GFX: entries[i] = card.gfx; break;
				case C_RARITY: entries[i] = card.rarity.getAbbreviation(); break;
				case C_SET: entries[i] = card.set.getAbbreviation(); break;
				case C_PACK: entries[i] = card.pack.getAbbreviation(); break;
				case MC_HP: entries[i] = card.getHp(); break;
				case MC_STAGE: entries[i] = card.stage; break;
				case MC_PREV_EVO: entries[i] = card.prevEvoName; break;
				case MC_MOVE_1_NAME: entries[i] = card.getMove(0).name; break;
				case MC_MOVE_2_NAME: entries[i] = card.getMove(1).name; break;
				case MC_RETREAT: entries[i] = card.retreatCost; break;
				case MC_WEAKNESS: entries[i] = card.weakness; break;
				case MC_RESISTANCE: entries[i] = card.resistance; break;
				case MC_CATEGORY: entries[i] = card.monsterCategory; break;
				case MC_DEX_NUMBER: entries[i] = card.dexNumber; break;
				case MC_UNKNOWN_1: entries[i] = card.unknownByte1; break;
				case MC_LEVEL: entries[i] = card.level; break;
				case MC_LENGTH: entries[i] = card.lengthFt + "'" + 
							card.lengthIn + "\""; break;
				case MC_WEIGHT: entries[i] = card.weight; break;
				case MC_DESCRIPTION_ID: entries[i] = card.description.getTextId(); break;
				case MC_UNKNOWN_2: entries[i] = card.unknownByte2; break;
				case MC_EVO_ID: entries[i] = card.get("evoLineId", -1); break; 
				case MC_MAX_EVO_STAGE: entries[i] = card.get("evoLineMaxStage", EvolutionStage.BASIC).getAbbreviation(); break;
				default: entries[i] = "N/A"; break;
			}
		}
		log.printf(rowFormat, entries);
	}
	
	public void printCard(NonMonsterCard card)
	{
		Object[] entries = new Object[columnFormats.length];
		for (int i = 0; i < columnFormats.length; i++)
		{
			switch (columnFormats[i].getColumn()) 
			{
				case C_ID: entries[i] = card.id; break;
				case C_NAME: entries[i] = card.name; break;
				case C_TYPE_SHORT: entries[i] = card.type.convertToEnergyType().getAbbreviation(); break;
				case C_GFX: entries[i] = card.gfx; break;
				case C_RARITY: entries[i] = card.rarity.getAbbreviation(); break;
				case C_SET: entries[i] = card.set.getAbbreviation(); break;
				case C_PACK: entries[i] = card.pack.getAbbreviation(); break;
				case NMC_EFFECT_ID: entries[i] = card.getEffectPtr(); break;
				case NMC_EFFECT_DESCRIPTION_ID: {
					List<Short> ids = card.getDescription().getTextIds();
					String allIds = "";
					for (int idx = 0; i < ids.size(); i++)
					{
						if (idx == 0)
						{
							allIds += ids.get(idx);
						}
						else
						{
							allIds +=  ", " + ids.get(idx);
						}
					}
					entries[i] = allIds;
					
				} break;
				default: entries[i] = "N/A"; break;
			}
		}
		log.printf(rowFormat, entries);
	}
}
