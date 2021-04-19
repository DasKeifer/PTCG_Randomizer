package data;

import java.util.ArrayList;
import java.util.List;

import constants.RomConstants;
import rom.Texts;
import util.ByteUtils;
import util.StringUtils;

public abstract class RomText
{	
	public static final char SPECIAL_SYM_RESERVE_SPACE_CHAR = 0x11; // Device control 1 for no particular reason
	private List<String> textBlocks;
	private List<Short> textBlockIds;
	
	public RomText()
	{
		textBlocks = new ArrayList<>();
		textBlocks.add("");
		textBlockIds = new ArrayList<>();
	}
	
	public RomText(RomText toCopy)
	{
		textBlocks = new ArrayList<>(toCopy.textBlocks);
		textBlockIds = new ArrayList<>(toCopy.textBlockIds);
	}

	public void setText(String newText)
	{
		textBlocks.clear();
		textBlockIds.clear();
		textBlocks.add(processForInternalManaging(newText));
	}
	
	public void setTextBlocks(List<String> newText)
	{
		textBlocks = new ArrayList<>(newText);
		if (textBlocks.isEmpty())
		{
			textBlocks.add("");
		}
		textBlockIds.clear();
		processForInternalManaging(newText);
	}
	
	public List<String> getTextBlocks()
	{
		return new ArrayList<>(textBlocks);
	}
	
	@Override
	public String toString()
	{
		return toString(false);
	}
	
	public String toString(boolean keepFormatted)
	{
		if (!keepFormatted)
		{
			return getDeformattedAndMergedText();
		}
		
		StringBuilder textBuilder = new StringBuilder();
		for (String string : textBlocks)
		{
			if (textBuilder.length() > 0)
			{
				textBuilder.append(StringUtils.BLOCK_BREAK);
			}
			textBuilder.append(string);
		}
		return textBuilder.toString();
	}
	
	public boolean isEmpty()
	{
		// Always will be at least one entry/block
		for (String text : textBlocks)
		{
			if (!text.isEmpty())
			{
				return false;
			}
		}
		return true;
	}

	public boolean contains(String regex)
	{
		return StringUtils.contains(textBlocks, regex);
	}
	
	public boolean replaceAll(String regex, String replacement)
	{
		if (contains(regex))
		{
			StringUtils.replaceAll(textBlocks, regex, replacement);
			textBlockIds.clear(); // Invalidated the existing ids
			return true;
		}
		return false;
	}
	
	public void finalizeAndAddTexts(Texts idToText)
	{
		// We clear the IDs if we changed/manually set the text
		if ((textBlockIds.isEmpty() && !isEmpty()) || needsReformatting(textBlocks))
		{
			textBlocks = formatText(getDeformattedAndMergedText());
		}
		
		// Now add in the English chars and such - don't do it before reformatting or else
		// it won't add at the start of block breaks correctly
		prepareForWritting(textBlocks);
		
		textBlockIds.clear();
		for (String block : textBlocks)
		{
			textBlockIds.add(idToText.insertTextOrGetId(block));
		}
	}
	
	protected abstract boolean needsReformatting(List<String> text);
	
	protected abstract List<String> formatText(String text);
	
	protected void genericReadTextFromIds(byte[] bytes, int[] textIdIndexes, Texts idsToText)
	{
		textBlocks.clear();
		textBlockIds.clear();
		
		for (int index : textIdIndexes)
		{
			short textId = ByteUtils.readAsShort(bytes, index);
			if (textId == 0)
			{
				break;
			}
			
			textBlocks.add(idsToText.getAtId(textId));
			textBlockIds.add(textId);
		}
		
		// Guard against 0 length array
		if (textIdIndexes.length == 0)
		{
			textBlocks.add("");
			textBlockIds.add(idsToText.getId(""));
		}
		
		processForInternalManaging(textBlocks);
	}

	protected void genericWriteTextIds(byte[] bytes, int[] indexesToWriteAt)
	{
		// TODO: fix throws
		if (textBlockIds.size() != textBlocks.size())
		{
			throw new IllegalArgumentException("Object not setup to write! The found number of ids (" + 
					textBlockIds.size() + ") does not match the number of text blocks (" + textBlocks.size() + 
					") for text \"" + StringUtils.createAbbreviation(toString(), 25) + "\"");
		}
		
		int expectedBlocks = indexesToWriteAt.length;
		if (textBlocks.size() > expectedBlocks)
		{
			throw new IllegalArgumentException("Too many text blocks passed in! Expected " + 
					expectedBlocks + " got " + textBlocks.size() + ". First block is \"" + textBlocks.get(0) + "\"");
		}
		
		// Write each blocks id
		int blockIndex = 0;
		for (; blockIndex < textBlockIds.size(); blockIndex++)
		{
			ByteUtils.writeAsShort(textBlockIds.get(blockIndex).shortValue(), 
					bytes, indexesToWriteAt[blockIndex]);
		}
		
		// In case only one block was set when we need to write two or something
		// to that effect, fill in the rest with 0 (nulls)
		if (blockIndex < expectedBlocks)
		{
			for (;blockIndex < expectedBlocks; blockIndex++)
			{
				ByteUtils.writeAsShort((short)0, bytes, indexesToWriteAt[blockIndex]);
			}
		}
		
		// Set it back
		processForInternalManaging(textBlocks);
	}
	
	protected String getDeformattedAndMergedText()
	{
		if (textBlocks.size() > 1)
		{
			StringBuilder merged= new StringBuilder();
			for (String string : textBlocks)
			{
				merged.append(stripOfFormatting(processForInternalManaging(string)));
			}
			
			return merged.toString();
		}
		else if (textBlocks.size() == 1)
		{
			return stripOfFormatting(processForInternalManaging(textBlocks.get(0)));
		}
		return "";
	}
	
	private static String stripOfFormatting(String toStrip)
	{
		return toStrip.replaceAll(" \n", " ").replaceAll("\n", " ").replaceAll(StringUtils.BLOCK_BREAK, " ").replaceAll("  ", " ");
	}

	private String processForInternalManaging(String text)
	{		
		if (!text.isEmpty())
		{
			return reserveSpaceForSpecialChars(removeEnglishCharTypeChars(text));
		}
		return "";
	}
	
	private void processForInternalManaging(List<String> text)
	{
		for (int i = 0; i < text.size(); i++)
		{
			text.set(i, processForInternalManaging(text.get(i)));
		}
	}
	
	private String prepareForWritting(String text)
	{
		if (!text.isEmpty())
		{
			return removeReserveSpaceForSpecialChars(addEnglishCharsTypeCharIfNeeded(text));
		}
		return "";
	}
	
	private void prepareForWritting(List<String> text)
	{
		for (int i = 0; i < text.size(); i++)
		{
			text.set(i, prepareForWritting(text.get(i)));
		}
	}
			
	private static String addEnglishCharsTypeCharIfNeeded(String text)
	{
		if (!text.startsWith("" + RomConstants.ENLGISH_TEXT_CHAR))
		{
			return RomConstants.ENLGISH_TEXT_CHAR + text;
		}
		return text;
	}
	
	private static String removeEnglishCharTypeChars(String text)
	{
		if (text.startsWith("" + RomConstants.ENLGISH_TEXT_CHAR))
		{
			return text.substring(1);
		}
		return text;
	}
	
	private static String reserveSpaceForSpecialChars(String text)
	{
		// Energy types behave a bit oddly - if there is a space before them (which there always seems 
		// to be is) then they need to align with an even char position. If the space is an even char, 
		// that means it displays as two spaces but if its an odd char, it displays as one space. To 
		// keep the  formatting generic, we add the extra space in for all energies to assume the 
		// "worst" case
		for (String specialChars : RomConstants.SPECIAL_SYMBOLS)
		{
			// First remove in case this is called more than once or something
			text = text.replaceAll(specialChars + SPECIAL_SYM_RESERVE_SPACE_CHAR, specialChars);
			text = text.replaceAll(specialChars, specialChars + SPECIAL_SYM_RESERVE_SPACE_CHAR);
		}
		return text;
	}
	
	private static String removeReserveSpaceForSpecialChars(String text)
	{
		// Energy types behave a bit oddly - if there is a space before them (which there always seems 
		// to be is) then they need to align with an even char position. If the space is an even char, 
		// that means it displays as two spaces but if its an odd char, it displays as one space. To 
		// keep the  formatting generic, we add the extra space in for all energies to assume the 
		// "worst" case
		for (String specialChars : RomConstants.SPECIAL_SYMBOLS)
		{
			text = text.replaceAll(specialChars + SPECIAL_SYM_RESERVE_SPACE_CHAR, specialChars);
		}
		return text;
	}
}
