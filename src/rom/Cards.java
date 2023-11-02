package rom;

import compiler.CodeBlock;
import compiler.InstructionParser;
import compiler.reference_instructs.BlockBankLoadedAddress;
import compiler.static_instructs.RawBytes;
import constants.PtcgRomConstants;
import data.Card;
import data.CardGroup;
import gbc_framework.rom_addressing.AddressRange;
import rom_packer.Blocks;
import rom_packer.FixedBlock;
import rom_packer.HybridBlock;

public class Cards 
{
	private CardGroup<Card> allCards;
	private AddressRange origPtrsRange;
	
	public Cards()
	{
		allCards = new CardGroup<>();
		origPtrsRange = null;
	}
	
	public void setOrigPtrsRange(AddressRange range)
	{
		origPtrsRange = range;
	}
	
	public CardGroup<Card> cards()
	{
		return allCards;
	}

	// TODO later: Maybe move out of here since its a bit awkward here?
	public void finalizeConvertAndAddData(Texts texts, Blocks blocks, InstructionParser parser)
	{
		// First go through and finalize all the data, adding effect blocks
		// as needed
		for (Card card : allCards.listOrderedByCardId())
		{
			card.finalizeAndAddData(this, texts, blocks, parser);
		}
		
		// Now add the cards themselves as blocks
		// Write a null pointer to start because thats how it was in the original rom
		CodeBlock cardPtrs = new CodeBlock("internal_cardPointers");
		if (origPtrsRange != null)
		{
			cardPtrs.addByteSourceHint(origPtrsRange);
		}
		cardPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0));
		
		for (Card card : allCards.iterable())
		{
			HybridBlock cardBlock = card.convertToHybridBlock();
			blocks.addHybridBlock(cardBlock);
			cardPtrs.appendInstruction(new BlockBankLoadedAddress(cardBlock.getMovableBlock().getId(), false));
		}

		// Add the trailing null and create the fixed block. Since its all fixed size, we can just pass the length of the block
		cardPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0));
		blocks.addFixedBlock(new FixedBlock(cardPtrs, PtcgRomConstants.CARD_POINTERS_LOC));
	}
}
