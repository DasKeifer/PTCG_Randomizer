package util;

import java.util.ArrayList;
import java.util.List;

public class TextUtils 
{
	public static final String BLOCK_BREAK = "" + (char) 0x0C;
	
	public static String prettyFormatText(String text, int maxCharsPerLine, int maxLines)
	{
		return prettyFormatText(text, maxCharsPerLine, maxLines, maxLines, 1);
	}
	
	public static String prettyFormatText(String block, int maxCharsPerLine, int maxLinesPerBlock, int preferedLinesPerBlock, int maxNumberOfBlocks)
	{
		String[] blockWords = block.split(" ");
		List<String> formattedLines = new ArrayList<>();
		String currLine = "";
		for (String word : blockWords)
		{
			if (currLine.isEmpty())
			{
				currLine = word;
			}
			else if (currLine.length() + 1 + word.length() <= maxCharsPerLine)
			{
				currLine += " " + word;
			}
			else
			{
				formattedLines.add(currLine);
				currLine = word;
			}
		}
		
		// Add the last line that was being worked on
		formattedLines.add(currLine);
		
		// See if there is enough space
		if (formattedLines.size() > maxLinesPerBlock * maxNumberOfBlocks)
		{
			for (String text : formattedLines)
			{
				System.out.println(text);
			}
			return null;
		}
		
		// Figure our if we need to overpack it
		int linesPerBlock = preferedLinesPerBlock;
		if (formattedLines.size() > preferedLinesPerBlock * maxNumberOfBlocks)
		{
			linesPerBlock = formattedLines.size() / maxNumberOfBlocks + 1;
		}
		
		String formatted = "";
		int linesInBlock = 0;
		for (String line : formattedLines)
		{
			if (linesInBlock >= linesPerBlock)
			{
				formatted += BLOCK_BREAK;
			}
			else if (!formatted.isEmpty())
			{
				formatted += "\n";
			}
			formatted += line;
		}
		
		return formatted;
	}
	
	public static String packFormatText(String text, int maxCharsPerLine, int maxLines)
	{
		return packFormatText(text, maxCharsPerLine, maxLines, 1);
	}
	
	public static String packFormatText(String block, int maxCharsPerLine, int maxLinesPerBlock, int maxNumberOfBlocks)
	{
		// TODO:
		return null;
	}
}
