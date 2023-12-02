package ptcgr.data.romtexts;


import gbc_framework.utils.StringUtils;
import ptcgr.constants.PtcgRomConstants;
import ptcgr.rom.Texts;

public class EffectDescription extends RomText
{	
	private String sourceCardName;

	public EffectDescription()
	{
		super(PtcgRomConstants.MAX_CHARS_PER_LINE_CARD, 
				PtcgRomConstants.PREFERRED_LINES_PER_BLOCK_EFFECT_DESC, 
				PtcgRomConstants.MAX_LINES_PER_BLOCK_EFFECT_DESC, 
				PtcgRomConstants.MAX_BLOCKS_EFFECT_DESC);
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
		throw new IllegalArgumentException("Must pass the name of the Monster when finalizing effect descriptions");
	}
	
	public void finalizeAndAddTexts(Texts idToText, String descriptionCardName)
	{
		// Get the flattened text
		boolean changedText = false;
		String deformatted = getDeformattedAndMergedText();
		
		// TODO later: This isn't quite right or sufficient for what is needed
		// Replace with a method to read changes from a file?
		if (!isEmpty())
		{
			// Some descriptions have misspelled names. Check if this is one and if so, replace it
			if (PtcgRomConstants.MISSPELLED_CARD_NAMES.containsKey(sourceCardName))
			{
				if (deformatted.contains(PtcgRomConstants.MISSPELLED_CARD_NAMES.get(sourceCardName)))
				{
					StringUtils.replaceAll(deformatted, PtcgRomConstants.MISSPELLED_CARD_NAMES.get(sourceCardName), descriptionCardName);
					changedText = true;
				}
			}
			
			// Now check if we need to change other references to the card's name
			if (!sourceCardName.equals(descriptionCardName))
			{
				deformatted = StringUtils.replaceAll(deformatted, sourceCardName, descriptionCardName);
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
	public void readDataAndConvertIds(byte[] bytes, int[] textIdIndexes, Texts idsToText)
	{
		throw new IllegalArgumentException("Must pass the name of the card when reading effect descriptions");
	}
	
	public void readDataAndConvertIds(byte[] bytes, int[] textIdIndexes, RomText cardName, Texts idsToText)
	{
		super.readDataAndConvertIds(bytes, textIdIndexes, idsToText);
		
		// Save the poke name for later rewriting of the description and fixing misspellings
		sourceCardName = cardName.toString();
	}
}
