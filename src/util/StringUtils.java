package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils 
{
	public static final String BLOCK_BREAK = "" + (char) 0x0C;

	private StringUtils() {}
	
	public static boolean contains(List<String> strings, String regex)
	{
		for (String string : strings)
		{
			if (Pattern.compile(regex).matcher(string).find())
			{
				return true;
			}
		}
		
		return false;
	}

	public static String replaceAll(String string, String regex, String replacement)
	{
		return string.replaceAll(regex, Matcher.quoteReplacement(replacement));
	}
	
	public static void replaceAll(List<String> strings, String regex, String replacement)
	{
		// Use pattern quote because they use $ for nidoran male which screws up replace all
		for (int i = 0; i < strings.size(); i++)
		{
			strings.set(i, strings.get(i).replaceAll(regex, Matcher.quoteReplacement(replacement)));
		}
	}
	
	public static String createAbbreviation(String stringToAbbreviate, int maxLength)
	{
		return createAbbreviation(stringToAbbreviate, maxLength, true);
	}
	
	public static String createAbbreviation(String stringToAbbreviate, int maxLength, boolean addElipses)
	{
		if (stringToAbbreviate.length() > maxLength)
		{
			if (addElipses)
			{
				return stringToAbbreviate.substring(0, maxLength) + "...";
			}
			else
			{
				return stringToAbbreviate.substring(0, maxLength);
			}
		}
		
		return stringToAbbreviate;
	}
	
	public static boolean isFormattedValidly(List<String> blocksOfText, int maxCharsPerLine, int maxLines)
	{
		for (String block : blocksOfText)
		{
			if (!isFormattedValidly(block, maxCharsPerLine, maxLines))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isFormattedValidly(String text, int maxCharsPerLine, int maxLines)
	{
		String[] lines = text.split("/n");
		
		if (lines.length > maxLines)
		{
			return false;
		}
		
		for (String line : lines)
		{
			if (line.length() > maxCharsPerLine)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static List<String> prettyFormatText(String text, int maxCharsPerLine, int maxLines)
	{
		return prettyFormatText(text, maxCharsPerLine, maxLines, maxLines, 1);
	}

	public static List<String> prettyFormatText(String text, int maxCharsPerLine, int maxLinesPerBlock, int preferedLinesPerBlock, int maxNumberOfBlocks)
	{
		String[] textWords = text.split(" ");
		List<String> formattedLines = new ArrayList<>();
		StringBuilder currLine = new StringBuilder();
		for (String word : textWords)
		{
			if (currLine.length() == 0)
			{
				currLine.append(word);
			}
			else if (currLine.length() + 1 + word.length() <= maxCharsPerLine)
			{
				currLine.append(" ").append(word);
			}
			else
			{
				formattedLines.add(currLine.toString());
				currLine.setLength(0); // Clear the string builder
				currLine.append(word);
			}
		}

		// Add the last line that was being worked on
		formattedLines.add(currLine.toString());

		// See if there is enough space
		if (formattedLines.size() > maxLinesPerBlock * maxNumberOfBlocks)
		{
			return new ArrayList<>();
		}

		// Figure our if we need to overpack it
		int linesPerBlock = preferedLinesPerBlock;
		if (formattedLines.size() > preferedLinesPerBlock * maxNumberOfBlocks)
		{
			linesPerBlock = formattedLines.size() / maxNumberOfBlocks + 1;
		}

		return formatIntoBlocks(formattedLines, linesPerBlock);
	}

	public static List<String> packFormatText(String text, int charsPerLine, int maxLines)
	{
		return packFormatText(text, charsPerLine, maxLines, 1);
	}

	public static List<String> packFormatText(String text, int charsPerLine, int maxLinesPerBlock, int maxNumberOfBlocks)
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
			return new ArrayList<>();
		}

		return formatIntoBlocks(packedLines, maxLinesPerBlock);
	}

	private static List<String> formatIntoBlocks(List<String> lines, int linesPerBlock)
	{
		List<String> blocks = new ArrayList<>();
		StringBuilder block = new StringBuilder();
		int linesInBlock = 0;
		int totalLinesSoFar = 0;
		boolean hasOrphan = lines.size() % linesPerBlock == 1;

		// Figure out if we need to prevent orphans
		for (String line : lines)
		{
			if (linesInBlock >= linesPerBlock)
			{
				blocks.add(block.toString());
				block.setLength(0); // Clear the builder
				linesInBlock = 0;
			}
			else if (block.length() != 0)
			{
				if (hasOrphan && totalLinesSoFar == lines.size() - 2)
				{
					// Orphan case detected! Prevent it
					block.append(BLOCK_BREAK);
					linesInBlock = 0;
				}
				else 
				{
					block.append("\n");
				}
			}
			block.append(line);
			linesInBlock++;
			totalLinesSoFar++;
		}

		// Add the last block that was being worked on
		blocks.add(block.toString());
		return blocks;
	}
}
