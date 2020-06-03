package data;

import java.util.Set;
import java.util.regex.Matcher;

import constants.RomConstants;
import rom.Texts;
import util.ByteUtils;
import util.StringUtils;

public abstract class RomText
{	
	public static final char SPECIAL_SYM_RESERVE_SPACE_CHAR = 0x11; // Device control 1 for no particular reason
	private String text;
	
	public RomText()
	{
		text = "";
	}
	
	public RomText(RomText toCopy)
	{
		text = toCopy.text;
	}

	public String getText()
	{
		return text;
	}
	
	@Override
	public String toString()
	{
		return getText();
	}
	
	public boolean isEmpty()
	{
		return text.isEmpty();
	}
	
	public void setTextAndDeformat(String inText)
	{
		if (inText == null)
		{
			text = "";
		}
		else
		{
			text = inText.replaceAll("\n", " ");
			text = removeEnglishCharTypeChars(text);
			text = reserveSpaceForSpecialChars(text);
		}
	}
	
	public void setTextVerbatim(String inText)
	{
		if (inText == null)
		{
			text = "";
		}
		else
		{
			text = removeEnglishCharTypeChars(inText);
		}
	}

	public void replaceAll(String regex, String replacement)
	{
		// Use pattern quote because they use $ for nidoran male which screws up replace all
		text = text.replaceAll(regex, Matcher.quoteReplacement(replacement));
	}
	
	protected void genericReadTextFromIds(byte[] bytes, int[] textIdIndexes, Texts idsToText, Set<Short> textIdsUsed)
	{
		String readText = "";
		for (int index : textIdIndexes)
		{
			short textId = ByteUtils.readAsShort(bytes, index);
			if (textId == 0)
			{
				break;
			}
			
			if (!readText.isEmpty())
			{
				readText += StringUtils.BLOCK_BREAK;
			}
			
			readText += idsToText.getAtId(textId);
			textIdsUsed.add(textId);
		}
		
		// Will also deformat it
		setTextAndDeformat(readText);
	}
	
	protected void genericConvertToIdsAndWriteText(byte[] bytes, int[] textIdIndexes, Texts idToText)
	{
		String textToWrite = removeReserveSpaceForSpecialChars(text);
		
		String[] blocks = textToWrite.split(StringUtils.BLOCK_BREAK);
		int expectedBlocks = textIdIndexes.length;
		int blockIndex = 0;
		
		if (!text.isEmpty())
		{
			if (blocks.length > expectedBlocks)
			{
				throw new IllegalArgumentException("Too many text blocks passed in! Expected " + 
						expectedBlocks + " got " + blocks.length + ". First block is \"" + blocks[0] + "\"");
			}
			
			// Write each block
			for (String block : blocks)
			{
				block = addEnglishCharTypeCharIfNotSetForBlock(block);
				if (block == null || block.isEmpty())
				{
					ByteUtils.writeAsShort((short)0, bytes, textIdIndexes[blockIndex]);
				}
				else
				{
					short id = idToText.insertTextOrGetId(block);
					ByteUtils.writeAsShort(id, bytes, textIdIndexes[blockIndex]);
				}
				blockIndex++;
			}
		}
		
		// In case only one block was passed when we need to write two or something
		// to that effect
		if (blockIndex < expectedBlocks)
		{
			for (;blockIndex < expectedBlocks; blockIndex++)
			{
				ByteUtils.writeAsShort((short)0, bytes, textIdIndexes[blockIndex]);
			}
		}
	}
			
	private static String addEnglishCharTypeCharIfNotSetForBlock(String block)
	{
		if (block.getBytes()[0] != RomConstants.ENLGISH_TEXT_CHAR)
		{
			block = RomConstants.ENLGISH_TEXT_CHAR + block;
		}
		return block;
	}
	
	private static String removeEnglishCharTypeChars(String text)
	{
		if (text.startsWith("" + RomConstants.ENLGISH_TEXT_CHAR))
		{
			text = text.substring(1);
		}
		return text.replaceAll(StringUtils.BLOCK_BREAK + RomConstants.ENLGISH_TEXT_CHAR, StringUtils.BLOCK_BREAK);
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
