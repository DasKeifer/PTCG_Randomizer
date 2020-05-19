package randomizer;

import java.io.IOException;
import java.util.List;

import gameData.Card;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	public static void main(String[] args) throws IOException //Temp
	{
		RomData rom = RomHandler.readRom();
		test(rom.cards);
		test(rom.text);
		RomHandler.writeRom(rom);
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
