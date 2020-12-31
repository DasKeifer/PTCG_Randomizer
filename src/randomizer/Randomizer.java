package randomizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import constants.CardConstants.CardId;
import constants.CardDataConstants.CardType;
import data.Card;
import data.Cards;
import data.Move;
import data.PokemonCard;
import randomizer.Settings.RandomizationStrategy;
import rom.Texts;
import util.Logger;
import util.MathUtils;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	static final String SEED_LOG_EXTENSION = ".seed.txt";
	static final String LOG_FILE_EXTENSION = ".log.txt";
	private static final long SEED = 42;
	
	private Logger logger;
	private Random rand;
	private RomData romData;
	
	private MoveSetRandomizer moveSetRand;
	
	public Randomizer()
	{
		logger = new Logger();
		rand = new Random(SEED);
	}
	
	public void openRom(File romFile)
	{
		try 
		{
			romData = RomHandler.readRom(romFile);
			
			moveSetRand = new MoveSetRandomizer(romData, rand);
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
				seedFile.write(Long.toString(SEED));
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
		List<Card> venu = romData.allCards.getCardsWithName("Venusaur").toList();
		venu.get(1).name.setTextAndDeformat("Test-a-saur");
		
		moveSetRand.randomize(settings);

		test(romData.allCards.getCardsWithName("Metapod"));
		
		// Temp hack to add more value cards to a pack
		// 11 is the most we can do
		for (int i = 0; i < 16; i ++)
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
		}
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

