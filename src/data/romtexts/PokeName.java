package data.romtexts;

import constants.RomConstants;

public class PokeName extends OneLineText
{
	public PokeName()
	{
		super(RomConstants.MAX_CHARS_POKE_NAME);
	}
	
	public PokeName(String text)
	{
		this();
		setText(text);
	}
	
	public PokeName(PokeName toCopy)
	{
		super(toCopy);
	}
}
