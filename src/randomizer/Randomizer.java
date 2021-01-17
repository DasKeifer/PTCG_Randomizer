package randomizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import data.Card;
import data.Cards;
import rom.Texts;
import util.Logger;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	static final String SEED_LOG_EXTENSION = ".seed.txt";
	static final String LOG_FILE_EXTENSION = ".log.txt";
	private static final long BASE_SEED = 42;
	private long nextSeed = BASE_SEED;
	
	private Logger logger;
	private RomData romData;
	
	public Randomizer()
	{
		logger = new Logger();
	}
	
	public void openRom(File romFile)
	{
		try 
		{
			romData = RomHandler.readRom(romFile);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void randomizeAndSaveRom(File romFile, Settings settings) throws IOException
	{		
		String romBasePath = romFile.getPath();
		romBasePath = romBasePath.substring(0, romBasePath.lastIndexOf('.'));
		
		if (settings.isLogSeed())
		{
			FileWriter seedFile = new FileWriter(romBasePath + SEED_LOG_EXTENSION);
			try
			{
				seedFile.write(Long.toString(BASE_SEED));
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
		
		randomize(settings);

		logger.close();
		
		try {
			RomHandler.writeRom(romData, romFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//public static void main(String[] args) throws IOException //Temp
	public void randomize(Settings settings)
	{
		// Create sub randomizers
		MoveSetRandomizer moveSetRand = new MoveSetRandomizer(romData, logger);
		
		List<Card> venu = romData.allCards.getCardsWithName("Venusaur").toList();
		venu.get(1).name.setTextAndDeformat("Test-a-saur");
		
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
		moveSetRand.randomize(nextSeed, settings);
		nextSeed += 100;
		
		// Non card randomizations
		
		// Randomize trades
		
		// Randomize Promos
		
		// Randomize Decks
		
		test(romData.allCards.getCardsWithName("Kakuna"));
		
		// Temp hack to add more value cards to a pack
		// 11 is the most we can do
		/*for (int i = 0; i < 16; i ++)
		{
			System.out.println(romData.rawBytes[0x1e4d4 + i]);
			if (i % 4 == 1)
			{
				romData.rawBytes[0x1e4d4 + i] = 5;
			}
			else if (i % 4 == 2)
			{
				romData.rawBytes[0x1e4d4 + i] = 4;
			}
			else if (i % 4 == 3)
			{
				romData.rawBytes[0x1e4d4 + i] = 2;
			}
			else
			{
				romData.rawBytes[0x1e4d4 + i] = 0;
			}
		}*/
	}
	
	public static void test(Cards<Card> cards)
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
		return ".gbc";
	}
}

