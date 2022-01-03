package data.romtexts;

import constants.PtcgRomConstants;
import constants.CharMapConstants.CharSetPrefix;

public class PokeCategory extends OneLineText
{
	public PokeCategory()
	{
		super(PtcgRomConstants.MAX_CHARS_POKE_CATEGORY);
	}
	
	public PokeCategory(String text)
	{
		this();
		setText(text);
	}
	
	public PokeCategory(CharSetPrefix charSet, String text)
	{
		this();
		setText(charSet, text);
	}
	
	public PokeCategory(PokeCategory toCopy)
	{
		super(toCopy);
	}
}
