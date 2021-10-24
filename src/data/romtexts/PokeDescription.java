package data.romtexts;


import constants.RomConstants;

public class PokeDescription extends OneBlockText
{		
	public PokeDescription() 
	{
		super(RomConstants.MAX_CHARS_PER_LINE_CARD, RomConstants.MAX_LINES_POKE_DESC);
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
