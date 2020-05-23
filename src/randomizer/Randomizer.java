package randomizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import gameData.Card;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	public static void main(String[] args) throws IOException //Temp
	{
		RomData rom = RomHandler.readRom();
		System.out.println(rom.ptrToText.get((short)0x080a));
		rom.ptrToText.put((short)0x080a, (char)0x06 + "BlahBlahSaur");
		test(rom.cardsByName.get((char)0x06 + "Bulbasaur"));
		test(rom.ptrToText);
		RomHandler.writeRom(rom);
	}
	
	public static void test(List<Card> cards)
	{
		for (Card card : cards)
		{
			System.out.println(card.toString() + "\n");
		}
	}
	
	public static void test(Map<Short, String> allText)
	{
		for (short i = 1; i < 20; i++) //String text : allText)
		{
			System.out.println(allText.get(i));
		}
	}
}
