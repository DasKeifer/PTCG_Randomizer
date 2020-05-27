package gameData;

import constants.RomConstants;
import util.TextUtils;

public class GameTextUtils 
{	
	public static String addEnglishCharTypeCharIfNotSet(String text)
	{
		String returnText = "";
		String line = "";
		String[] blocks = text.split(TextUtils.BLOCK_BREAK);
		for (String block : blocks)
		{
			line = addEnglishCharTypeCharIfNotSetForLine(block);
			if (returnText.isEmpty())
			{
				returnText = line;
			}
			else
			{
				returnText = TextUtils.BLOCK_BREAK + line;
			}
		}
		return returnText;
	}
			
	private static String addEnglishCharTypeCharIfNotSetForLine(String line)
	{
		if (line.getBytes()[0] != RomConstants.ENLGISH_TEXT_CHAR)
		{
			line = RomConstants.ENLGISH_TEXT_CHAR + line;
		}
		return line;
	}
	
	static String removeEnglishCharTypeCharIfPresent(String name)
	{
		if (name.startsWith("" + (char)0x06))
		{
			name = name.substring(1);
		}
		return name;
	}
	
	static String removeAllEnglishCharTypeChars(String name)
	{
		return name.replaceAll("" + RomConstants.ENLGISH_TEXT_CHAR, "");
	}
}
