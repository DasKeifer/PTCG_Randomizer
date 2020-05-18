package randomizer;

import gameData.Card;
import io.RomHandler;

public class Randomizer 
{
	public static void main(String[] args)
	{
		RomHandler rom = new RomHandler();
		rom.read();
		Card[] allCards = rom.readCards();
		test(allCards);
		rom.write();
	}
	
	public static void test(Card[] cards)
	{
		for (Card card : cards)
		{
			System.out.println(card.toString() + "\n");
		}
	}
}
