package data.romtexts;

import constants.RomConstants;

public class PokeCategory extends OneLineText
{
	public PokeCategory()
	{
		super(RomConstants.MAX_CHARS_POKE_CATEGORY);
	}
	
	public PokeCategory(String text)
	{
		this();
		setText(text);
	}
	
	public PokeCategory(PokeCategory toCopy)
	{
		super(toCopy);
	}
}
