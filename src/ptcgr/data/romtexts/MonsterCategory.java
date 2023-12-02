package ptcgr.data.romtexts;

import ptcgr.constants.PtcgRomConstants;
import ptcgr.constants.CharMapConstants.CharSetPrefix;

public class MonsterCategory extends OneLineText
{
	public MonsterCategory()
	{
		super(PtcgRomConstants.MAX_CHARS_MONSTER_CATEGORY);
	}
	
	public MonsterCategory(String text)
	{
		this();
		setText(text);
	}
	
	public MonsterCategory(CharSetPrefix charSet, String text)
	{
		this();
		setText(charSet, text);
	}
	
	public MonsterCategory(MonsterCategory toCopy)
	{
		super(toCopy);
	}
}
