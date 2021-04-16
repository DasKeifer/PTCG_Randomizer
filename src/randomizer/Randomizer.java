package randomizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import constants.RomConstants;
import constants.CardDataConstants.MoveCategory;
import data.Card;
import data.Cards;
import data.Move;
import data.PokemonCard;
import data.RomText;
import rom.Texts;
import util.Logger;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	static final String SEED_LOG_EXTENSION = ".seed.txt";
	static final String LOG_FILE_EXTENSION = ".log.txt";
	
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
		
		RomData randomized = randomize(settings);

		logger.close();
		
		// TODO: Due to an error, the same data was being written more than once
		// and when this happened, the text for some cards compoundly got worse.
		// Need to look into why this is happening
		try {
			RomHandler.writeRom(randomized, romFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//public static void main(String[] args) throws IOException //Temp
	public RomData randomize(Settings settings)
	{
		// get and store the base seed as the next one to use
		int nextSeed = settings.getSeedValue();
		
		
		for (Move m : romData.allCards.getPokemonCards().getAllMoves())
		{
			if (m.isPokePower())
				System.out.println(m.name.getText() + " " + m.category + " " + m.getDamageString() + " " + m.description.getText());
		}
		
		// Make a copy of the data to modify and return
		RomData randomizedData = new RomData(romData);
		
		// Create sub randomizers. If they need to original data, they can save off a copy
		// when they are creaeted
		MoveSetRandomizer moveSetRand = new MoveSetRandomizer(randomizedData, logger);
		
		List<Card> venu = randomizedData.allCards.getCardsWithName("Venusaur").toList();
		venu.get(1).name.setTextAndDeformat("Test-a-saur"); // Quick check to see if we ran and saved successfully
		
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

		//test(randomizedData.allCards.getCardsWithName("Mew"));
		
		// Temp hack to add more value cards to a pack. In the future this will be more formalized
		// 11 is the most we can do
		for (int i = 0; i < 16; i ++)
		{
			if (i % 4 == 1)
			{
				randomizedData.rawBytes[0x1e4d4 + i] = 5;
			}
			else if (i % 4 == 2)
			{
				randomizedData.rawBytes[0x1e4d4 + i] = 4;
			}
			else if (i % 4 == 3)
			{
				randomizedData.rawBytes[0x1e4d4 + i] = 2;
			}
			else
			{
				randomizedData.rawBytes[0x1e4d4 + i] = 0;
			}
		}
//			
		// 2cc40 - 16 bytes - no changes needed - just point to it
		for (int i = 0x1baf8; i < 0x1bafa; i++)
		{
//			if (randomizedData.rawBytes[0xce8a  + i] != -1)
//			{
				System.out.println(String.format("0x%x", randomizedData.rawBytes[i]));
//			}
		}
		
		int baseLoc = 0x2cf6d;
		randomizedData.rawBytes[baseLoc + 8] = 0x2e;
		randomizedData.rawBytes[baseLoc + 9] = 0x1;
		randomizedData.rawBytes[baseLoc + 11] = 0x41;
		randomizedData.rawBytes[baseLoc + 12] = 0x01;
		randomizedData.rawBytes[baseLoc + 24] = 0x2f;
		randomizedData.rawBytes[baseLoc + 25] = 0x01;
		// chose from deck Belsprout text 8, 9 (2e, 01)
		// Belsprout text 11, 12 (41, 01)
		// choose Belsprout text 24, 25 (2f, 01)
		
		// BellsproutID at +14, 41, 74
		randomizedData.rawBytes[baseLoc + 14] = (byte) 0x99;
		randomizedData.rawBytes[baseLoc + 41] = (byte) 0x99;
		randomizedData.rawBytes[baseLoc + 74] = (byte) 0x99;
		
		return randomizedData;
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

