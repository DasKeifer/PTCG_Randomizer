package rom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import constants.PtcgRomConstants;
import data.Card;
import data.custom_card_effects.HardcodedEffects;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;

public class RomIO
{
	private RomIO() {}
	
	static boolean verifyRom(byte[] rawBytes)
	{
		// TODO later: Do a CRC instead? Maybe if we go with the BPS patch format
		int index = PtcgRomConstants.HEADER_LOCATION;
		for (byte headerByte : PtcgRomConstants.HEADER)
		{
			if (headerByte != rawBytes[index++])
			{
				return false;
			}
		}
		
		return true;
	}
	
	static byte[] readRaw(File romFile) throws IOException 
	{
		return Files.readAllBytes(romFile.toPath());
	}
	
	static void writeRaw(byte[] rawBytes, File romFile)
	{
		try (FileOutputStream fos = new FileOutputStream(romFile))
		{
			fos.write(rawBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static Cards<Card> readAllCardsFromPointers(byte[] rawBytes, Texts allText)
	{
		Cards<Card> allCards = new Cards<>();

		// Read the text based on the pointer map in the rom
		int ptrIndex = PtcgRomConstants.CARD_POINTERS_LOC;
		int cardIndex = 0;

		// Read each pointer one at a time until we reach the ending null pointer
		while ((cardIndex = (short) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES)
				) != 0)
		{
			cardIndex += PtcgRomConstants.CARD_POINTER_OFFSET;
			Card.addCardFromBytes(rawBytes, cardIndex, allText, allCards);

			// Move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		}
		
		return allCards;
	}

	// Note assumes that the first text in the pointer list is the first in the file as well. This is required
	// since there is no null between the text pointer map and the texts themselves
	static Texts readAllTextFromPointers(byte[] rawBytes)
	{
		Texts textMap = new Texts();
		 
		 // Read the text based on the pointer map in the rom
		// First pointer is a null pointer so we skip it
		int ptrIndex = PtcgRomConstants.TEXT_POINTERS_LOC + PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		int ptr = 0;
		int textIndex = 0;
		int firstPtr = Integer.MAX_VALUE;
		
		// Read each pointer one at a time until we reach the ending null pointer
		while (ptrIndex < firstPtr)
		{
			ptr = (int) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES) + 
					PtcgRomConstants.TEXT_POINTER_OFFSET;
			if (ptr < firstPtr)
			{
				firstPtr = ptr;
			}
			
			// Find the ending null byte
			textIndex = ptr;
			while (rawBytes[++textIndex] != 0x00);
			
			// Read the string to the null char (but not including it)
			textMap.insertTextAtNextId(new String(rawBytes, ptr, textIndex - ptr));

			// Move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		}
		
		return textMap;
	}
	
	static void finalizeDataAndGenerateBlocks(Cards<Card> cards, Texts texts, Blocks blocks)
	{
		// Reset the singleton -- TODO later: Needed?
		HardcodedEffects.reset();
		
		// Finalize the card data, texts and blocks
		cards.finalizeDataForAllocating(texts, blocks);
		
		// Convert the text to blocks
		texts.convertAndAddBlocks(blocks);
	}
	
	static void writeAllCards(byte[] bytes, Cards<Card> cards, AssignedAddresses assignedAddresses)
	{		
		// First write the 0 index "null" card
		int ptrIndex = PtcgRomConstants.CARD_POINTERS_LOC - PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		for (int byteIndex = 0; byteIndex < PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES; byteIndex++)
		{
			bytes[ptrIndex++] = 0;
		}
		
		// determine where the first card will go based off the number of cards we have
		// The first null pointer was already taken care of so we don't need to handle it 
		// here but we still need to handle the last null pointer
		int cardIndex = PtcgRomConstants.CARD_POINTERS_LOC + (cards.count() + 1) * PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		
		for (Card card : cards.toListOrderedByCardId())
		{
			// Write the pointer
			ByteUtils.writeLittleEndian(cardIndex - PtcgRomConstants.CARD_POINTER_OFFSET, bytes, ptrIndex, PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES);
			ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
			
			// Write the card
			cardIndex = card.writeData(bytes, cardIndex, assignedAddresses);
		}

		// Write the null pointer at the end of the cards pointers
		for (int byteIndex = 0; byteIndex < PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES; byteIndex++)
		{
			bytes[ptrIndex++] = 0;
		}
		
		// Until it is determined to be necessary, don't worry about padding remaining space with 0xff
	}
	
	// Note assumes that the first text in the pointer list is the first in the file as well. This is required
	// since there is no null between the text pointer map and the texts themselves
	static void clearAllText(byte[] rawBytes)
	{				
		// Free the starting set of 0's
		ByteUtils.setBytes(rawBytes, PtcgRomConstants.TEXT_POINTERS_LOC, 
				PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES, (byte) 0xFF);
		
		// Read the text based on the pointer map in the rom
		// 0th is a null pointer and we already handled it so we jump to the
		// first "real" pointer
		int ptrIndex = PtcgRomConstants.TEXT_POINTERS_LOC + PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		int textAddress = 0;
		int textIndex = 0;
		int firstAddress = Integer.MAX_VALUE;
		
		int endOfBankAddress;
		int checkEOBIndex;
		boolean foundEndOfBank;
		// Read each pointer one at a time until we reach the ending null pointer
		while (ptrIndex < firstAddress)
		{
			// Get the address from the ptr
			textAddress = (int) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES) + 
					PtcgRomConstants.TEXT_POINTER_OFFSET;
			
			// If this is an earlier address, we need to update where to stop
			if (textAddress < firstAddress)
			{
				firstAddress = textAddress;
			}
			
			// go to the text index and loop until we finding the 0x00 termination of the
			// string writing each as a empty byte 0xFF
			textIndex = textAddress;
			for (; rawBytes[textIndex] != 0x00; textIndex++)
			{
				rawBytes[textIndex] = (byte) 0xFF;
			}
			
			// overwrite the trailing 0x00 as well
			rawBytes[textIndex] = (byte) 0xFF;
			
			// See if there are only 0x00 until the end of the bank and if so, overwrite them too
			endOfBankAddress = RomUtils.getEndOfBankAddressIsIn(textIndex);
			checkEOBIndex = textIndex + 1;
			foundEndOfBank = false;
			while(rawBytes[checkEOBIndex++] == 0x00)
			{
				if (checkEOBIndex >= endOfBankAddress)
				{
					foundEndOfBank = true;
					break;
				}
			}
			
			if (foundEndOfBank)
			{
				for (;textIndex < endOfBankAddress; textIndex++)
				{
					rawBytes[textIndex] = (byte) 0xFF;
				}
			}
			
			// Now write over the pointer as well
			ByteUtils.setBytes(rawBytes, ptrIndex, PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES, (byte) 0xFF);

			// Finally, move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		}
	}
}
