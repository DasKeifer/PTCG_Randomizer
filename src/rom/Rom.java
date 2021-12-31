package rom;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import bps_writer.BpsWriter;
import compiler.CodeBlock;
import data.PtcgInstructionParser;
import data.custom_card_effects.CustomCardEffect;
import gbc_framework.rom_addressing.AddressRange;
import gbc_framework.rom_addressing.AssignedAddresses;
import rom_packer.Blocks;
import rom_packer.DataManager;

public class Rom
{
	// TODO: Minor Make private
	public byte[] rawBytes;
	private List<AddressRange> rangesToConsiderFree;
	
	// Make public - we will be modifying these
	public Cards allCards;
	public Texts idsToText;
	public Blocks blocks;
	
	public Rom(File romFile) throws IOException
	{
		rangesToConsiderFree = new LinkedList<>();
		
		allCards = new Cards();
		idsToText = new Texts();
		blocks = new Blocks();
		
		readRom(romFile);
	}

	public void readRom(File romFile) throws IOException
	{
		rawBytes = RomIO.readRaw(romFile);
		RomIO.verifyRom(rawBytes);
		
		idsToText = RomIO.readTextFromData(rawBytes, rangesToConsiderFree);
		allCards = RomIO.readCardsFromData(rawBytes, idsToText, rangesToConsiderFree);
	}
	
	public void writeRom(File romFile)
	{
		// TODO: minor merge with ROM?
		
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
		List<AddressRange> spacesToBlank = new LinkedList<>();
		
		// TODO: Check for null
		AssignedAddresses assignedAddresses = manager.allocateBlocks(rawBytes, blocks, rangesToConsiderFree, spacesToBlank);
			
		// Now actually write to the bytes
		BpsWriter writer = new BpsWriter();
		try 
		{
			blocks.writeBlocks(writer, assignedAddresses);
			writer.blankUnusedSpace(spacesToBlank);
			writer.createBlanksAndFillEmptyHunksWithSourceRead(rawBytes.length);
			writer.writeBps(romFile, rawBytes);
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// TODO: remove copy?
	public Rom(Rom toCopy)
	{
		rawBytes = toCopy.rawBytes;
		rangesToConsiderFree = new LinkedList<>(toCopy.rangesToConsiderFree);
		allCards = new Cards(toCopy.allCards);
		idsToText = new Texts(toCopy.idsToText);
		
		// Don't copy - too complicated for now at least and wouldn't be used anyways
		blocks = new Blocks();
	}
}
