package data.romtexts;

import constants.RomConstants;

public class CardName extends OneLineText
{
	public CardName(boolean isPokeCard)
	{
		super(isPokeCard ? RomConstants.MAX_CHARS_POKE_NAME :RomConstants.MAX_CHARS_CARD_NAME);
	}
	
	public CardName(boolean isPokeCard, String text)
	{
		this(isPokeCard);
		setText(text);
	}
	
	public CardName(CardName toCopy)
	{
		super(toCopy);
	}
}
