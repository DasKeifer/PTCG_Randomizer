package ptcgr.data;


import compiler.CodeBlock;
import compiler.InstructionParser;
import compiler.static_instructs.RawBytes;
import rom_packer.Blocks;
import gbc_framework.utils.ByteUtils;
import ptcgr.constants.PtcgRomConstants;
import ptcgr.data.romtexts.CardName;
import ptcgr.data.romtexts.EffectDescription;
import ptcgr.rom.Cards;
import ptcgr.rom.Texts;

public class NonMonsterCard extends Card
{
	public static final int TOTAL_SIZE_IN_BYTES = 14;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	
	private short effectPtr;
	private EffectDescription description;

	public NonMonsterCard() 
	{
		super();
		
		description = new EffectDescription();
	}
	
	public NonMonsterCard(NonMonsterCard toCopy) 
	{
		super(toCopy);
		
		effectPtr = toCopy.effectPtr;
		description = new EffectDescription(toCopy.description);
	}
	
	public NonMonsterCard copy()
	{
		return new NonMonsterCard(this);
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
		
		// reading non monster specific data
		effectPtr = ByteUtils.readAsShort(cardBytes, index);
		index += 2;

		int[] descIndexes = {index, index + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(cardBytes, descIndexes, name, idToText);
		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public void finalizeAndAddData(Cards cards, Texts texts, Blocks blocks, InstructionParser unused)
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
	
	@Override
	public int getSize()
	{
		return TOTAL_SIZE_IN_BYTES;
	}
	
	public short getEffectPtr() 
	{
		return effectPtr;
	}

	public EffectDescription getDescription() 
	{
		return description;
	}
}
