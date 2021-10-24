package data.romtexts;


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
	
	public OneLineText(OneLineText toCopy)
	{
		super(toCopy);
	}
}
