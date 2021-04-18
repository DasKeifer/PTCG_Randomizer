package rom;

import java.io.File;
import java.io.IOException;

import data.Card;
import data.Cards;

public class Rom
{
	// Make package so we don't change it unintentionally
	public byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards<Card> allCards;
	public Texts idsToText;
	
	// TODO: extract to separate class
	// TODO: Maybe make a smart class that will hold onto the data and only assign it as saving based on total space avail and what not
	// The class would rejigger and farcall as needed based on available space. That sounds pretty snazzy - lets give it a whirl
	//
	// Thoughts on this:
	// We would need to hold onto pointers for things like text and calls.
	// Calls we would need special logic to determine if we can do them locally or need a farcall
	// We also need some concept of non-bank swappable pointers for the effect commands since those assume a bank and are
	// only given addresses relative to that bank. The addressed location could presumably then farcall something else
	// Text would just be a matter of putting the right value int so that would be easier
	// So we have: Call/jp, ldtx, and bankSpecificPointers
	// Then with that data I think we can rejigger things
	// Effect command would be byte, bankSpecificPointer(B, effectFunctionId), byte, bankSpecificPointer(B, effectFunctionId), ..., byte
	// Effect function would then be byte[], call(bank, address), byte[], etc.
	// 		if it was relocated, the function would just be a farcall to the new loc then a ret
	
	// Function - ID, CallLocation(reqBank, address)[], trueBank, trueAddr, data<bytesOrCalls>
	// Blob - ID, requiredBank, address, data<bytesOrCalls>
	// Text probably doesn't need to be here - it can be part of defining the data/Is handled separately but then we need to remove the text space from here...
	// Hmmm gets tricky
	
	// Process - do all the texts first. Read in all the text, label all that space as free, but as we add text, it gets used up. This probably isn't too much
	// of a change from how we do it now.
	// Then do the other stuff - check if we need to move things around, etc.s
	
	public Rom(File romFile) throws IOException
	{
		allCards = new Cards<>();
		idsToText = new Texts();
		
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
		// Clear the old text to make space for the new text and 
		// create the manager to control distributing free space
		RomIO.clearAllText(rawBytes);
		
		// TODO: clear unused move commands/effects as well
		FreeSpaceManager spaceManager = new FreeSpaceManager(rawBytes);
		
		// Finalize and gather card text
		RomIO.finalizeAndAddTexts(allCards, idsToText);

		// Save the new data starting with the text so we can ensure it gets
		// the space in memory it needs
		RomIO.setTextAndPointers(rawBytes, spaceManager, idsToText);
		RomIO.setAllCardsAndPointers(rawBytes, spaceManager, allCards);
		
		// Write it to the file
		RomIO.writeRaw(rawBytes, romFile);
	}
	
	public Rom(Rom toCopy)
	{
		rawBytes = toCopy.rawBytes;
		allCards = toCopy.allCards.copy(Card.class);
		idsToText = new Texts(toCopy.idsToText);
	}
}
