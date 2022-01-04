package data;


import compiler.CodeBlock;
import compiler.static_instructs.RawBytes;
import constants.PtcgRomConstants;
import data.romtexts.CardName;
import data.romtexts.EffectDescription;
import rom.Cards;
import rom.Texts;
import rom_packer.Blocks;
import gbc_framework.utils.ByteUtils;

public class NonPokemonCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	private short effectPtr;
	private EffectDescription description;

	public NonPokemonCard() 
	{
		super();
		
		description = new EffectDescription();
	}
	
	public NonPokemonCard(NonPokemonCard toCopy) 
	{
		super(toCopy);
		
		effectPtr = toCopy.effectPtr;
		description = new EffectDescription(toCopy.description);
	}
	
	public NonPokemonCard copy()
	{
		return new NonPokemonCard(this);
	}
	
	@Override
	protected CardName createCardName()
	{
		return new CardName(false); // not a poke name
	}
	
	@Override
	public int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		commonReadAndConvertIds(cardBytes, startIndex, idToText);
		int index = startIndex + Card.CARD_COMMON_SIZE;
		
		// reading non pokemon specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(cardBytes, descIndexes, name, idToText);
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public void finalizeAndAddData(Cards cards, Texts texts, Blocks blocks)
	{
		commonFinalizeAndAddData(texts);
		
		description.finalizeAndAddTexts(texts, name.toString());
	}

	@Override
	protected CodeBlock convertToCodeBlock() 
	{
		CodeBlock block = convertCommonDataToCodeBlock();

		block.appendInstruction(new RawBytes(
				ByteUtils.shortToLittleEndianBytes(effectPtr),
				ByteUtils.shortListToLittleEndianBytes(description.getTextIds(PtcgRomConstants.MAX_BLOCKS_EFFECT_DESC))
		));
		
		return block;
	}
}
