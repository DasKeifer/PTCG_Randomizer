package rom;

import data.Card;
import data.Cards;

public class RomData
{
	// Make package so we don't change it unintentionally
	byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards<Card> allCards = new Cards<>();
	public Texts idsToText = new Texts();
}
