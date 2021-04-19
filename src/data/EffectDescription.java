package data;

import java.util.List;

import constants.RomConstants;
import rom.Texts;
import util.StringUtils;

public class EffectDescription extends RomText
{	
	private static final int NUM_BLOCKS_IN_ROM = 2;
	private String sourceCardName;

	public EffectDescription()
	{
		super();
		sourceCardName = "";
	}
	
	public EffectDescription(EffectDescription toCopy)
	{
		super(toCopy);
		sourceCardName = toCopy.sourceCardName;
	}
	
	// Effectively undefine the function without the names for this object
	@Override
	public void finalizeAndAddTexts(Texts idToText)
	{
		throw new IllegalArgumentException("Must pass the name of the pokemon when finalizing effect descriptions");
	}
	
	public void finalizeAndAddTexts(Texts idToText, String owningCardName)
	{
		// Get the flattened text
		boolean changedText = false;
		String deformatted = getDeformattedAndMergedText();
		
		if (!isEmpty())
		{
			// Some descriptions have misspelled names. Check if this is one and if so, replace it
			if (RomConstants.MISPELLED_CARD_NAMES.containsKey(sourceCardName))
			{
				if (deformatted.contains(RomConstants.MISPELLED_CARD_NAMES.get(sourceCardName)))
				{
					StringUtils.replaceAll(deformatted, RomConstants.MISPELLED_CARD_NAMES.get(sourceCardName), owningCardName);
					changedText = true;
				}
			}
			
			// Now check if we need to change other references to the card's name
			if (!sourceCardName.equals(owningCardName))
			{
				deformatted = StringUtils.replaceAll(deformatted, sourceCardName, owningCardName);
				changedText = true;
			}
		}
		
		// If we changed it, we need to update the text in the class
		if (changedText)
		{
			setText(deformatted);
		}

		// Then call the parent version of the function
		super.finalizeAndAddTexts(idToText);
	}
	
	@Override
	protected boolean needsReformatting(List<String> text)
	{
		if (text.size() > 2)
		{
			return true;
		}
		else if (text.isEmpty())
		{
			return false;
		}
		
		return !StringUtils.isFormattedValidly(text, RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC);
	}

	@Override
	protected List<String> formatText(String text) 
	{
		List<String> formatted = StringUtils.prettyFormatText(text,
				RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC,
				RomConstants.PREFERRED_LINES_PER_EFFECT_DESC, NUM_BLOCKS_IN_ROM);
		
		if (formatted.isEmpty())
		{
			System.out.println("Could not nicely fit effect text over line breaks - attempting to " +
					"split words over lines to get it to fit for \"" + text + "\"");
			
			// If all else fails, just pack as tight as possible
			formatted = StringUtils.packFormatText(text,
				RomConstants.MAX_CHARS_PER_LINE, RomConstants.MAX_LINES_PER_EFFECT_DESC,
				NUM_BLOCKS_IN_ROM);
			
			if (formatted.isEmpty())
			{
				System.out.println("Failed to nicely pack effect description \"" + 
						StringUtils.createAbbreviation(text, 25) + "\" so words were split across lines to make it fit");
			}
			else
			{
				throw new IllegalArgumentException("Could not successfully format effect description \"" + 
						StringUtils.createAbbreviation(text, 25) + "\"");
			}
		}
		
		return formatted;
	}
	
	public void readDataAndConvertIds(byte[] bytes, int[] textIdIndexes, RomText cardName, Texts idToText)
	{
		if (textIdIndexes.length != NUM_BLOCKS_IN_ROM)
		{
			throw new IllegalArgumentException("Reading effect description was passed the wrong number of id indexes :" + 
					textIdIndexes.length);
		}
		
		// Save the poke name for later rewriting of the description and fixing misspellings
		sourceCardName = cardName.toString();
		
		// Read the text
		genericReadTextFromIds(bytes, textIdIndexes, idToText);
	}
	
	public void writeTextId(byte[] bytes, int[] textIdIndexes)
	{
		if (textIdIndexes.length != NUM_BLOCKS_IN_ROM)
		{
			throw new IllegalArgumentException("Writing effect description was passed the wrong number of id indexes :" + 
					textIdIndexes.length);
		}
		genericWriteTextIds(bytes, textIdIndexes);
	}

}
