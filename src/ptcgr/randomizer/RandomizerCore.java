package ptcgr.randomizer;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import gbc_framework.utils.Logger;
import ptcgr.config.Configs;
import ptcgr.data.Card;
import ptcgr.data.CardGroup;
import ptcgr.randomizer.actions.Action;
import ptcgr.randomizer.actions.ActionBank;
import ptcgr.randomizer.categories.CardsRandomizer;
import ptcgr.randomizer.categories.MovesRandomizer;
import ptcgr.rom.Rom;
import ptcgr.rom.RomIO;
import ptcgr.rom.Texts;

public class RandomizerCore 
{
	static final String SEED_LOG_EXTENSION = ".seed.txt";
	static final String LOG_FILE_EXTENSION = ".log.txt";
	
	private Logger logger;
	private Rom romData;
	private Configs configs;
	private ActionBank actionBank;
	
	public RandomizerCore()
	{
		logger = new Logger();
		setupActionBank();
	}
	
	public ActionBank getActionBank()
	{
		return actionBank;
	}
	
	private void setupActionBank()
	{
		actionBank = new ActionBank();
		CardsRandomizer.addActions(actionBank, logger);
		MovesRandomizer.addActions(actionBank, logger);
	}
	
	public void openRom(File romFile, Component toCenterPopupsOn)
	{
		try 
		{
			romData = new Rom(RomIO.readRaw(romFile));
		} 
		catch (IOException e)
		{
			// TODO later: Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO later: Move to saving?
		// Now load the config files
		configs = new Configs(romData, toCenterPopupsOn);		
	}
	
	public void randomizeAndSaveRom(File romFile, Settings settings, List<Action> actionBank) throws IOException
	{				
		String romBasePath = romFile.getPath();
		romBasePath = romBasePath.substring(0, romBasePath.lastIndexOf('.'));
		
		if (settings.isLogSeed())
		{
			FileWriter seedFile = new FileWriter(romBasePath + SEED_LOG_EXTENSION);
			try
			{
				String seedText = settings.getSeedString();
				String seedVal = String.valueOf(settings.getSeedValue());
				if (!seedText.equals(seedVal))
				{
					seedFile.write("Text: \"" + seedText + "\", Numeric Equivalent: " + seedVal);
				}
				else
				{
					seedFile.write("Seed value: " + seedText);
				}
			}
			finally
			{
				seedFile.close();
			}
		}
		
		if (settings.isLogDetails())
		{
			logger.open(romBasePath + LOG_FILE_EXTENSION);
		}
		
		randomize(settings, actionBank);

		logger.close();
		
		// TODO later: Due to an error, the same data was being written more than once
		// and when this happened, the text for some cards compoundly got worse.
		// Need to look into why this is happening and if it still is
		romData.writePatch(romFile);
	}
	
	//public static void main(String[] args) throws IOException //Temp
	public void randomize(Settings settings, List<Action> actions)
	{	
		// get and store the base seed as the next one to use
		int nextSeed = settings.getSeedValue();
		
		// Ensure the rom data is back to the original data (for multiple randomizations
		// without reloading) and prepare it to be modified so we know that
		// it will need to be reset
		romData.resetAndPrepareForModification();
		
		for (Action action : actions)
		{
			action.Perform(romData);
		}
		
		// Create sub randomizers. If they need to original data, they can save off a copy
		// when they are created
//		MoveSetRandomizer moveSetRand = new MoveSetRandomizer(romData, logger);
		
		CardGroup<Card> venus = romData.allCards.cards().withNameIgnoringNumber("Venusaur");
		CardGroup.basedOnIndex(venus, 1).name.setText("Test-a-saur"); // Quick check to see if we ran and saved successfully
	
		// Randomize Evolutions (either within current types or completely random)
		// If randomizing evos and types but keeping lines consistent, completely 
		// randomize here then sort it out in the types
		nextSeed += 100;
		
		// Randomize Types (full random, set all mons in a evo line to the same random time)
		nextSeed += 100;

		// Anything below here contributes to the "power score" of a card and may be rejiggered
		// or skipped depending on how the balancing is done in the end
		
		// Randomize HP
		nextSeed += 100;
		
		// Randomize weaknesses and resistances
		nextSeed += 100;
		
		// Randomize Retreat cost
		nextSeed += 100;

		// Randomize moves
		nextSeed += 100;
		
		// Randomize movesets (full random or match to type)
//		moveSetRand.randomize(nextSeed, settings, configs);
//		nextSeed += 100;
		
		// Non card randomizations
		
		// Randomize trades
		
		// Randomize Promos
		
		// Randomize Decks
		
		// Temp hack to add more value cards to a pack. In the future this will be more formalized
		// 11 is the most we can do
		// TODO: Move to tweak
//		for (int i = 0; i < 16; i ++)
//		{
//			if (i % 4 == 1)
//			{
//				romData.rawBytes[0x1e4d4 + i] = 5;
//			}
//			else if (i % 4 == 2)
//			{
//				romData.rawBytes[0x1e4d4 + i] = 4;
//			}
//			else if (i % 4 == 3)
//			{
//				romData.rawBytes[0x1e4d4 + i] = 2;
//			}
//			else
//			{
//				romData.rawBytes[0x1e4d4 + i] = 0;
//			}
//		}
	}
	
	public static void test(CardGroup<Card> cards)
	{
		for (Card card : cards.iterable())
		{
			System.out.println(card.toString() + "\n");
		}
	}
	
	public static void test(Texts allText)
	{
		for (short i = 1; i < 20; i++) //String text : allText)
		{
			System.out.println(allText.getAtId(i));
		}
	}

	public String getFileExtension() 
	{
		return ".bps";
	}
}

