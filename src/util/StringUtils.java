package util;

import java.util.ArrayList;
import java.util.List;

public class StringUtils 
{
	public static final String BLOCK_BREAK = "" + (char) 0x0C;
	
	public static String prettyFormatText(String text, int maxCharsPerLine, int maxLines)
	{
		return prettyFormatText(text, maxCharsPerLine, maxLines, maxLines, 1);
	}
	
	public static String prettyFormatText(String text, int maxCharsPerLine, int maxLinesPerBlock, int preferedLinesPerBlock, int maxNumberOfBlocks)
	{
		String[] textWords = text.split(" ");
		List<String> formattedLines = new ArrayList<>();
		String currLine = "";
		for (String word : textWords)
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
			return null;
		}
		
		// Figure our if we need to overpack it
		int linesPerBlock = preferedLinesPerBlock;
		if (formattedLines.size() > preferedLinesPerBlock * maxNumberOfBlocks)
		{
			linesPerBlock = formattedLines.size() / maxNumberOfBlocks + 1;
		}

		return formatIntoBlocks(formattedLines, linesPerBlock);
	}
	
	public static String packFormatText(String text, int charsPerLine, int maxLines)
	{
		return packFormatText(text, charsPerLine, maxLines, 1);
	}
	
	public static String packFormatText(String text, int charsPerLine, int maxLinesPerBlock, int maxNumberOfBlocks)
	{
		List<String> packedLines = new ArrayList<>();
		String remainingText = text;
		int numCharsToTake = charsPerLine;
		while(remainingText.length() > 0)
		{
			if (numCharsToTake > remainingText.length())
			{
				numCharsToTake = remainingText.length();
			}
			packedLines.add(text.substring(0, numCharsToTake).trim());
			remainingText = remainingText.substring(numCharsToTake).trim();
		}
		
		// Make sure it will fit
		if (packedLines.size() > maxLinesPerBlock * maxNumberOfBlocks)
		{
			return null;
		}
		
		return formatIntoBlocks(packedLines, maxLinesPerBlock);
	}
	
	private static String formatIntoBlocks(List<String> lines, int linesPerBlock)
	{
		StringBuilder formatted = new StringBuilder();
		int linesInBlock = 0;
		for (String line : lines)
		{
			if (linesInBlock >= linesPerBlock)
			{
				formatted.append(BLOCK_BREAK);
			}
			else if (formatted.length() != 0)
			{
				formatted.append("\n");
			}
			formatted.append(line);
		}
		return formatted.toString();
	}
}
