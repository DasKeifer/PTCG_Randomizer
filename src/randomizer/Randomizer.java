package randomizer;

import java.io.IOException;
import java.util.List;

import gameData.Card;
import gameData.PokemonCard;
import rom.Texts;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	public static void main(String[] args) throws IOException //Temp
	{
		RomData rom = RomHandler.readRom();
		List<Card> venu = rom.cardsByName.getCardsWithName("Venusaur");
		
		test(venu);
		//test(rom.idsToText);

		List<Card> bulba = rom.cardsByName.getCardsWithName("Bulbasaur");
		((PokemonCard)bulba.get(0)).move1 = ((PokemonCard)venu.get(1)).move1;
		venu.get(1).name.text = (char)0x06 + "Blahblahsaur";
		
		RomHandler.writeRom(rom);
	}
	
	public static void test(List<Card> cards)
	{
		for (Card card : cards)
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
