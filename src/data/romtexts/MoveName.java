package data.romtexts;

import constants.RomConstants;

public class MoveName extends OneLineText
{
	public MoveName()
	{
		super(RomConstants.MAX_CHARS_MOVE_NAME);
	}
	
	public MoveName(String text)
	{
		this();
		setText(text);
	}
	
	public MoveName(MoveName toCopy)
	{
		super(toCopy);
	}
}
