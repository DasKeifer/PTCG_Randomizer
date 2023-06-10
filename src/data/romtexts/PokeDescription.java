package data.romtexts;


import constants.PtcgRomConstants;
import constants.CharMapConstants.CharSetPrefix;

public class PokeDescription extends OneBlockText
{		
	public PokeDescription() 
	{
		super(PtcgRomConstants.MAX_CHARS_PER_LINE_CARD, PtcgRomConstants.MAX_LINES_MONSTER_DESC);
	}
	
	public PokeDescription(String text)
	{
		this();
		setText(text);
	}
	
	public PokeDescription(CharSetPrefix charSet, String text)
	{
		this();
		setText(charSet, text);
	}
	
	public PokeDescription(PokeDescription toCopy) 
	{
		super(toCopy);
	}
}
