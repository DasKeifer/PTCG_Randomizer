package rom;

import java.io.File;
import java.io.IOException;

import compiler.CodeBlock;
import data.Card;
import data.PtcgInstructionParser;
import data.custom_card_effects.CustomCardEffect;
import gbc_rom_packer.DataManager;
import gbc_framework.rom_addressing.AssignedAddresses;

public class Rom
{
	// Make package so we don't change it unintentionally
	public byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards<Card> allCards;
	public Texts idsToText;
	public Blocks blocks;
	
	public Rom(File romFile) throws IOException
	{
		allCards = new Cards<>();
		idsToText = new Texts();
		blocks = new Blocks();
		
		readRom(romFile);
	}

	public void readRom(File romFile) throws IOException
	{
		rawBytes = RomIO.readRaw(romFile);
		RomIO.verifyRom(rawBytes);
		
		idsToText = RomIO.readAllTextFromPointers(rawBytes);
		allCards = RomIO.readAllCardsFromPointers(rawBytes, idsToText);
	}
	
	public void writeRom(File romFile)
	{
		// TODO merge with ROM?
		
		// Clear the old text from the bytes - the manager will
		// take care of allocating the space as needed
		// TODO later: clear unused move commands/effects as well?
		RomIO.clearAllText(rawBytes);
		
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
		RomIO.finalizeDataAndGenerateBlocks(allCards, idsToText, blocks);
		
		// Now add all the text from the custom parser instructions
		parser.finalizeAndAddTexts(idsToText);
		
		// Now assign locations for the data
		DataManager manager = new DataManager();
		AssignedAddresses assignedAddresses = manager.allocateBlocks(rawBytes, blocks.getAllFixedBlocks(), blocks.getAllBlocksToAllocate());
			
		// Now save the cards - TODO later: move to blocks eventually?
		RomIO.writeAllCards(rawBytes, allCards, assignedAddresses);
			
		// Now actually write to the bytes
		// TODO: Move to different writing scheme
		//blocks.writeData(rawBytes, assignedAddresses);
		
		// Write the bytes to the file
		RomIO.writeRaw(rawBytes, romFile);
	}
	
	public Rom(Rom toCopy)
	{
		rawBytes = toCopy.rawBytes;
		allCards = toCopy.allCards.copy(Card.class);
		idsToText = new Texts(toCopy.idsToText);
		
		// Don't copy - too complicated for now at least and wouldn't be used anyways
		blocks = new Blocks();
	}
}
