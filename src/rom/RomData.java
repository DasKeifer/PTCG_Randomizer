package rom;

import data.Card;
import data.Cards;

public class RomData
{
	// Make package so we don't change it unintentionally
	public byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards<Card> allCards;
	public Texts idsToText;

	public RomData()
	{
		allCards = new Cards<>();
		idsToText = new Texts();
	}
	
	public RomData(RomData toCopy)
	{
		rawBytes = toCopy.rawBytes;
		allCards = toCopy.allCards.copy(Card.class);
		idsToText = new Texts(toCopy.idsToText);
	}
}
