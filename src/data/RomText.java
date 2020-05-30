package data;

import java.util.Set;

import constants.RomConstants;
import rom.Texts;
import util.ByteUtils;
import util.StringUtils;

public abstract class RomText
{	
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
	
	public void setText(String inText)
	{
		if (inText == null)
		{
			text = "";
		}
		else
		{
			text = inText.replaceAll("\n", " ");
			text = removeEnglishCharTypeChars(text);
		}
	}
	
	public void setTextPreservingNewlines(String inText)
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
		text = text.replaceAll(regex, replacement);
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
		
		// Will also unformat it
		setText(readText);
	}
	
	protected void genericConvertToIdsAndWriteText(byte[] bytes, int[] textIdIndexes, Texts idToText)
	{
		String[] blocks = text.split(StringUtils.BLOCK_BREAK);
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
	
	private static String removeEnglishCharTypeChars(String name)
	{
		return name.replaceAll("" + RomConstants.ENLGISH_TEXT_CHAR, "");
	}
}
