package data.romtexts;

import constants.PtcgRomConstants;

public class CardName extends OneLineText
{
	public static final String CARD_NAME_NUMBER_SEPARATOR = "_";
	
	public CardName(boolean isPokeCard)
	{
		super(isPokeCard ? PtcgRomConstants.MAX_CHARS_POKE_NAME :PtcgRomConstants.MAX_CHARS_CARD_NAME);
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
		// Split of the number if there is one and match on the name portion
		String[] split = namePotentiallyWithNumber.split(CARD_NAME_NUMBER_SEPARATOR);
		return split[0].trim().equals(toString());
	}
	
	public static boolean doesHaveNumber(String namePotentiallyWithNumber)
	{
		return namePotentiallyWithNumber.contains(CARD_NAME_NUMBER_SEPARATOR);
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
			String[] numSplitOff = namePotentiallyWithNumber.split(CARD_NAME_NUMBER_SEPARATOR);
			if (numSplitOff.length > 1)
			{
				try
				{
					retVal = Integer.parseInt(numSplitOff[1]);
				}
				catch (NumberFormatException nfe)
				{
					// Ret val already set to 0
				}
			}
		}
		
		return retVal;
	}
}
