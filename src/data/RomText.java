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
	private List<String> text;
	private List<Short> textIds;
	
	public RomText()
	{
		text = new ArrayList<>();
		textIds = new ArrayList<>();
	}
	
	public RomText(RomText toCopy)
	{
		text = new ArrayList<>(toCopy.text);
		textIds = new ArrayList<>(toCopy.textIds);
	}

	public void setText(String newText)
	{
		text.clear();
		textIds.clear();
		text.add(newText);
		processForInternalManaging();
	}
	
	public void setText(List<String> newText)
	{
		text = new ArrayList<>(newText);
		textIds.clear();
		processForInternalManaging();
	}
	
	public List<String> getText()
	{
		return new ArrayList<>(text);
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
			return deformatAndMergeText();
		}
		
		StringBuilder textBuilder = new StringBuilder();
		for (String string : text)
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
		return text.isEmpty();
	}

	public boolean contains(String regex)
	{
		return StringUtils.contains(text, regex);
	}
	
	public boolean replaceAll(String regex, String replacement)
	{
		if (contains(regex))
		{
			StringUtils.replaceAll(text, regex, replacement);
			textIds.clear(); // Invalidated the existing ids
			return true;
		}
		return false;
	}
	
	public void finalizeAndAddTexts(Texts idToText)
	{
		// We clear the IDs if we changed/manully set the text
		if (textIds.isEmpty() || needsReformatting(text))
		{
			text = formatText(deformatAndMergeText());
		}
		
		// Now add in the English chars and such - don't do it before reformatting or else
		// it won't add at the start of block breaks correctly
		prepareForWritting();
		
		textIds.clear();
		for (String block : text)
		{
			textIds.add(idToText.insertTextOrGetId(block));
		}
	}
	
	protected void checkAndDeformatIfNeeded()
	{
		// We clear the IDs if we changed/manully set the text
		if (textIds.isEmpty() || needsReformatting(text))
		{
			String deformatted = deformatAndMergeText();
			text.clear();
			text.add(deformatted);
		}
	}
	
	protected abstract boolean needsReformatting(List<String> text);
	
	protected abstract List<String> formatText(String text);
	
	protected void genericReadTextFromIds(byte[] bytes, int[] textIdIndexes, Texts idsToText)
	{
		text.clear();
		textIds.clear();
		
		for (int index : textIdIndexes)
		{
			short textId = ByteUtils.readAsShort(bytes, index);
			if (textId == 0)
			{
				break;
			}
			
			text.add(idsToText.getAtId(textId));
			textIds.add(textId);
		}
		
		processForInternalManaging();
	}

	protected void genericWriteTextIds(byte[] bytes, int[] indexesToWriteAt)
	{
		// TODO: fix throws
		if (textIds.size() != text.size())
		{
			throw new IllegalArgumentException("Object not setup to write! The found number of ids (" + 
					textIds.size() + ") does not match the number of text blocks (" + text.size() + 
					") for text \"" + StringUtils.createAbbreviation(toString(), 25) + "\"");
		}
		
		int expectedBlocks = indexesToWriteAt.length;
		if (text.size() > expectedBlocks)
		{
			throw new IllegalArgumentException("Too many text blocks passed in! Expected " + 
					expectedBlocks + " got " + text.size() + ". First block is \"" + text.get(0) + "\"");
		}
		
		// Write each blocks id
		int blockIndex = 0;
		for (; blockIndex < textIds.size(); blockIndex++)
		{
			ByteUtils.writeAsShort(textIds.get(blockIndex).shortValue(), 
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
		processForInternalManaging();
	}
	
	protected String deformatAndMergeText()
	{
		if (text.size() > 1)
		{
			StringBuilder merged= new StringBuilder();
			for (String string : text)
			{
				merged.append(stripOfFormatting(string));
			}
			
			return merged.toString();
		}
		else if (text.size() == 1)
		{
			return stripOfFormatting(text.get(0));
		}
		return "";
	}
	
	private static String stripOfFormatting(String toStrip)
	{
		return toStrip.replaceAll(" \n", " ").replaceAll("\n", " ").replaceAll(StringUtils.BLOCK_BREAK, " ").replaceAll("  ", " ");
	}

	private void processForInternalManaging()
	{
		removeEnglishCharTypeChars();
		reserveSpaceForSpecialChars();
	}
	
	private void prepareForWritting()
	{
		removeReserveSpaceForSpecialChars();
		addEnglishCharsTypeCharIfNeeded();
	}
			
	private void addEnglishCharsTypeCharIfNeeded()
	{
		for (int i = 0; i < text.size(); i++)
		{
			if (!text.get(i).startsWith("" + RomConstants.ENLGISH_TEXT_CHAR))
			{
				text.set(i, RomConstants.ENLGISH_TEXT_CHAR + text.get(i));
			}
		}
	}
	
	private void removeEnglishCharTypeChars()
	{
		for (int i = 0; i < text.size(); i++)
		{
			if (text.get(i).startsWith("" + RomConstants.ENLGISH_TEXT_CHAR))
			{
				text.set(i, text.get(i).substring(1));
			}
		}
	}
	
	private void reserveSpaceForSpecialChars()
	{
		// Energy types behave a bit oddly - if there is a space before them (which there always seems 
		// to be is) then they need to align with an even char position. If the space is an even char, 
		// that means it displays as two spaces but if its an odd char, it displays as one space. To 
		// keep the  formatting generic, we add the extra space in for all energies to assume the 
		// "worst" case
		for (int i = 0; i < text.size(); i++)
		{
			for (String specialChars : RomConstants.SPECIAL_SYMBOLS)
			{
				// First remove in case this is called more than once or something
				text.set(i, text.get(i).replaceAll(specialChars + SPECIAL_SYM_RESERVE_SPACE_CHAR, specialChars));
				text.set(i, text.get(i).replaceAll(specialChars, specialChars + SPECIAL_SYM_RESERVE_SPACE_CHAR));
			}
		}
	}
	
	private void removeReserveSpaceForSpecialChars()
	{
		// Energy types behave a bit oddly - if there is a space before them (which there always seems 
		// to be is) then they need to align with an even char position. If the space is an even char, 
		// that means it displays as two spaces but if its an odd char, it displays as one space. To 
		// keep the  formatting generic, we add the extra space in for all energies to assume the 
		// "worst" case
		for (int i = 0; i < text.size(); i++)
		{
			for (String specialChars : RomConstants.SPECIAL_SYMBOLS)
			{
				text.set(i, text.get(i).replaceAll(specialChars + SPECIAL_SYM_RESERVE_SPACE_CHAR, specialChars));
			}
		}
	}
}
