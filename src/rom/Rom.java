package rom;

import java.io.File;
import java.io.IOException;

import data.Card;
import data.customCardEffects.CustomCardEffect;
import datamanager.DataManager;
import romAddressing.AssignedAddresses;

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
	
	public void writeRom(File romFile) throws IOException
	{
		// TODO merge with ROM?
		
		// Clear the old text from the bytes - the manager will
		// take care of allocating the space as needed
		// TODO later: clear unused move commands/effects as well?
		RomIO.clearAllText(rawBytes);
		
		// TODO later: Need to handle tweak blocks somehow. Should these all be
		// file defined and selected via a menu? could also include if they default
		// to on or not. Also for now we can handle these after the other blocks
		// are generated but we arbitrarily do it before. Is there any reason to
		// do one or the other?
		CustomCardEffect.addTweakToAllowEffectsInMoreBanks(blocks);
		
		// Finalize all the data to prepare for writing
		RomIO.finalizeDataAndGenerateBlocks(allCards, idsToText, blocks);
		
		// Now assign locations for the data
		DataManager manager = new DataManager();
		AssignedAddresses assignedAddresses = manager.allocateBlocks(rawBytes, blocks);
			
		// Now save the cards - TODO later: move to blocks eventually?
		RomIO.writeAllCards(rawBytes, allCards, assignedAddresses);
			
		// Now actually write to the bytes
		blocks.writeData(rawBytes, assignedAddresses);
		
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
