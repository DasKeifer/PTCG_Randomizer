package randomizer;

import java.util.List;

import gameData.Card;
import io.RomHandler;

public class Randomizer 
{
	public static void main(String[] args)
	{
		RomHandler rom = new RomHandler();
		rom.readRaw();
		Card[] allCards = rom.readAllCards();
		List<String> allText = rom.readAllText();
		test(allCards);
		test(allText);
		rom.writeRaw();
	}
	
	public static void test(Card[] cards)
	{
		for (Card card : cards)
		{
			System.out.println(card.toString() + "\n");
		}
	}
	
	public static void test(List<String> allText)
	{
		for (int i = 0; i < 20; i++) //String text : allText)
		{
			System.out.println(allText.get(i));
		}
	}
}
