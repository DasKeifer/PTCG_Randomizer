package data.romtexts;


import constants.PtcgRomConstants;

public class PokeDescription extends OneBlockText
{		
	public PokeDescription() 
	{
		super(PtcgRomConstants.MAX_CHARS_PER_LINE_CARD, PtcgRomConstants.MAX_LINES_POKE_DESC);
	}
	
	public PokeDescription(String text)
	{
		this();
		setText(text);
	}
	
	public PokeDescription(PokeDescription toCopy) 
	{
		super(toCopy);
	}
}
