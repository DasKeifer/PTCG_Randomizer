package randomizer;

import java.io.IOException;

import gameData.Card;
import gameData.CardVersions;
import rom.Texts;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	public static void main(String[] args) throws IOException //Temp
	{
		RomData rom = RomHandler.readRom();
		System.out.println(rom.idsToText.getAtId((short)0x080a));
		rom.idsToText.setAtId((short)0x080a, "BlahBlahSaur");
		test(rom.cardsByName.getCardsWithName("Bulbasaur"));
		test(rom.idsToText);
		RomHandler.writeRom(rom);
	}
	
	public static void test(CardVersions cards)
	{
		for (Card card : cards.versions)
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
}
