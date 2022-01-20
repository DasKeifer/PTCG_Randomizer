package rom;

import java.io.File;

import compiler.CodeBlock;
import data.PtcgInstructionParser;
import data.custom_card_effects.CustomCardEffect;
import data.custom_card_effects.HardcodedEffects;
import gbc_framework.rom_addressing.AddressRange;
import gbc_framework.rom_addressing.AssignedAddresses;
import rom_packer.Blocks;
import rom_packer.DataManager;

public class Rom
{
	// TODO later: with tweak to allow 11 cards in pack, make this private
	private byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards allCards;
	public Texts idsToText;
	public Blocks blocks;
	
	private boolean dirtyBit;
	
	public Rom(byte[] romRaw)
	{
		rawBytes = romRaw;
		dirtyBit = true;
		resetRom();
	}

	public void resetRom()
	{
		if (dirtyBit)
		{
			dirtyBit = false;
			
			allCards = new Cards();
			idsToText = new Texts();
			blocks = new Blocks();
			
			readRomData();
		}
	}
	
	public void resetAndPrepareForModification()
	{
		if (dirtyBit)
		{
			resetRom();
		}
		dirtyBit = true;
	}

	private void readRomData()
	{	
		idsToText = RomIO.readTextsFromData(rawBytes, blocks);
		allCards = RomIO.readCardsFromData(rawBytes, idsToText, blocks);
	}
	
	public void writePatch(File patchFile)
	{
		// Create the custom parser and set the data blocks to use it
		PtcgInstructionParser parser = new PtcgInstructionParser();
		CodeBlock.setInstructionParserSingleton(parser);
		
		// TODO later: Need to handle tweak blocks somehow. Should these all be
		// file defined and selected via a menu? could also include if they default
		// to on or not. Also for now we can handle these after the other blocks
		// are generated but we arbitrarily do it before. Is there any reason to
		// do one or the other?
		CustomCardEffect.addTweakToAllowEffectsInMoreBanks(blocks);
		
		// Finalize all the data to prepare for writing
		finalizeDataAndGenerateBlocks(parser);
		
		// Now assign locations for the data
		DataManager manager = new DataManager();		
		AssignedAddresses assignedAddresses = manager.allocateBlocks(rawBytes, blocks);
			
		RomIO.writeBpsPatch(patchFile, rawBytes, blocks, assignedAddresses);
	}
	
	private void finalizeDataAndGenerateBlocks(PtcgInstructionParser parser)
	{
		// Reset the singleton -- TODO later: Needed?
		HardcodedEffects.reset();
		
		// Finalize the card data, texts and blocks
		allCards.finalizeConvertAndAddData(idsToText, blocks);
		
		// Now add all the text from the custom parser instructions
		parser.finalizeAndAddTexts(idsToText);
		
		// Convert the text to blocks
		idsToText.convertAndAddBlocks(blocks);
		
		// Sort them and combine values to make things easier elsewhere in the code
		// TODO later: if adding custom blanking, we should call this afterwards
		AddressRange.sortAndCombine(blocks.getAllBlankedBlocks());
	}
}
