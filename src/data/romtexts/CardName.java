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
	
	public boolean matchesIgnoringPotentialNumber(String namePotentiallyWithNumber)
	{
		return namePotentiallyWithNumber.trim().startsWith(toString());
	}
	
	public static boolean doesHaveNumber(String namePotentiallyWithNumber)
	{
		return namePotentiallyWithNumber.contains("_");
	}
	
	// Returns 1 based index, 0 means failed to parse number and negative means card name
	// didn't match
	public int getCardNumFromNameIfMatches(String namePotentiallyWithNumber)
	{
		int retVal = -1;		
		if (matchesIgnoringPotentialNumber(namePotentiallyWithNumber))
		{
			// 0 used if name matched but no number or invalid number found
			retVal = 0;
			String[] numSplitOff = namePotentiallyWithNumber.split("_");
			if (numSplitOff.length > 1)
			{
				try
				{
					retVal = Integer.parseInt(numSplitOff[1]);
				}
				catch (NumberFormatException nfe)
				{
					retVal = 0;
				}
			}
			else
			{
				retVal = 1;
			}
		}
		
		return retVal;
	}
}
