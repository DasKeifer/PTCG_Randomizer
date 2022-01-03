package data.romtexts;

import constants.CharMapConstants.CharSetPrefix;

public class OneLineText extends OneBlockText
{
	public OneLineText(int maxChars)
	{
		super(maxChars, 1); // max 1 line, 1 block
	}
	
	public OneLineText(String text, int maxChars)
	{
		this(maxChars);
		setText(text);
	}
	
	public OneLineText(CharSetPrefix charSet, String text, int maxChars)
	{
		this(maxChars);
		setText(charSet, text);
	}
	
	public OneLineText(OneLineText toCopy)
	{
		super(toCopy);
	}
}
