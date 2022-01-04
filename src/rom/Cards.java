package rom;

import compiler.CodeBlock;
import compiler.reference_instructs.BlockBankLoadedAddress;
import compiler.static_instructs.RawBytes;
import constants.PtcgRomConstants;
import data.Card;
import data.CardGroup;
import rom_packer.Blocks;
import rom_packer.HybridBlock;
import rom_packer.ReplacementBlock;

public class Cards 
{
	private CardGroup<Card> allCards;
	
	public Cards()
	{
		allCards = new CardGroup<>();
	}
	
	public CardGroup<Card> cards()
	{
		return allCards;
	}

	// TODO later: Maybe move out of here since its a bit awkward here?
	public void finalizeConvertAndAddData(Texts texts, Blocks blocks)
	{
		// First go through and finalize all the data, adding effect blocks
		// as needed
		for (Card card : allCards.listOrderedByCardId())
		{
			card.finalizeAndAddData(this, texts, blocks);
		}
		
		// Now add the cards themselves as blocks
		// Write a null pointer to start because thats how it was in the original rom
		CodeBlock cardPtrs = new CodeBlock("internal_cardPointers");
		cardPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0));
		
		for (Card card : allCards.iterable())
		{
			HybridBlock cardBlock = card.convertToHybridBlock();
			blocks.addHybridBlock(cardBlock);
			cardPtrs.appendInstruction(new BlockBankLoadedAddress(cardBlock.getMovableBlock().getId(), false));
		}

		// Add the trailing null and create the fixed block. Since its all fixed size, we can just pass the length of the block
		cardPtrs.appendInstruction(new RawBytes((byte) 0, (byte) 0));
		blocks.addFixedBlock(new ReplacementBlock(cardPtrs, PtcgRomConstants.CARD_POINTERS_LOC, cardPtrs.getWorstCaseSize()));
	}
}
