package randomizer;

import java.io.IOException;
import java.util.List;

import data.Card;
import data.Move;
import data.PokemonCard;
import rom.Texts;
import rom.RomData;
import rom.RomHandler;

public class Randomizer 
{
	public static void main(String[] args) throws IOException //Temp
	{
		RomData rom = RomHandler.readRom();
		List<Card> venu = rom.cardsByName.getCardsWithName("Venusaur").toList();
		
		//test(venu);

		List<Card> bulba = rom.cardsByName.getCardsWithName("Bulbasaur").toList();
		((PokemonCard)bulba.get(0)).move1 = new Move(((PokemonCard)venu.get(1)).move1);
		venu.get(1).name.setText("Test-a-saur");
		
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
