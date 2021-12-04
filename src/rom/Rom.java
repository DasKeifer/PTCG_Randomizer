package rom;

import java.io.File;
import java.io.IOException;

import bps_writer.BpsWriter;
import compiler.CodeBlock;
import data.PtcgInstructionParser;
import data.custom_card_effects.CustomCardEffect;
import gbc_framework.rom_addressing.AssignedAddresses;
import rom_packer.Blocks;
import rom_packer.DataManager;

public class Rom
{
	// Make package so we don't change it unintentionally
	public byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards allCards;
	public Texts idsToText;
	public Blocks blocks;
	
	public Rom(File romFile) throws IOException
	{
		allCards = new Cards();
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
		
		// TODO: How to handle this? Don't clear but need clear for DataManger. Designate it
		// as free space as additional arg?
		
		// TODO: Check if bytes change when writing to determine how to handle the block. That
		// or when the BPS is actually writing but that would be less "clean". Maybe check right
		// before adding the hunk
		
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
		AssignedAddresses assignedAddresses = manager.allocateBlocks(rawBytes, blocks);
			
		// Now actually write to the bytes
		BpsWriter writer = new BpsWriter();
		try 
		{
			blocks.writeBlocks(writer, assignedAddresses);
			writer.writeBps(romFile, rawBytes);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Rom(Rom toCopy)
	{
		rawBytes = toCopy.rawBytes;
		allCards = new Cards(toCopy.allCards);
		idsToText = new Texts(toCopy.idsToText);
		
		// Don't copy - too complicated for now at least and wouldn't be used anyways
		blocks = new Blocks();
	}
}
