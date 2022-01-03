package data.romtexts;

import constants.PtcgRomConstants;
import constants.CharMapConstants.CharSetPrefix;

public class MoveName extends OneLineText
{
	public MoveName()
	{
		super(PtcgRomConstants.MAX_CHARS_MOVE_NAME);
	}
	
	public MoveName(String text)
	{
		this();
		setText(text);
	}
	
	public MoveName(CharSetPrefix charSet, String text)
	{
		this();
		setText(charSet, text);
	}
	
	public MoveName(MoveName toCopy)
	{
		super(toCopy);
	}
}
