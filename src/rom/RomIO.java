package rom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import constants.PtcgRomConstants;
import data.Card;
import data.custom_card_effects.HardcodedEffects;
import gbc_framework.rom_addressing.AddressRange;
import gbc_framework.utils.ByteUtils;
import rom_packer.Blocks;

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

	// TODO: Minor rename this and cards version
	// Note assumes that the first text in the pointer list is the first in the file as well. This is required
	// since there is no null between the text pointer map and the texts themselves
	static Texts readTextFromData(byte[] rawBytes, List<AddressRange> addressesRead)
	{		 
		// TODO: Optimize address range adding since they will mostly be in order
		
		Texts texts = new Texts();
		
		 // Read the text based on the pointer map in the rom
		// First pointer is a null pointer so we skip it
		int ptrIndex = PtcgRomConstants.TEXT_POINTERS_LOC + PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		int ptr = 0;
		int textIndex = 0;
		int firstPtr = Integer.MAX_VALUE;
		
		// Read each pointer one at a time until we reach the first actual text.
		// This is because they didn't end the pointer list with a null
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
			
			// Read the string to the null char (but not including it) and store where
			// it was read from
			texts.insertTextAtNextId(new String(rawBytes, ptr, textIndex - ptr), ptr);
			
			// Add it to the list of spaces for the text itself
			if (addressesRead != null)
			{
				// + 1 because end is exclusive
				addressesRead.add(new AddressRange(ptr, textIndex + 1));
			}

			// Move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		}
		
		// Add the space for the pointers. The ptrIndex will end at the first text
		if (addressesRead != null)
		{
			// + 1 because end is exclusive
			addressesRead.add(new AddressRange(PtcgRomConstants.TEXT_POINTERS_LOC, ptrIndex + 1));
		}
		
		return texts;
	}
	
	static Cards readCardsFromData(byte[] rawBytes, Texts allText, List<AddressRange> addresses)
	{
		// TODO: Optimize address range adding since they will mostly be in order
		
		Cards cards = new Cards();

		// Read the cards based on the pointer map in the rom
		// Skip the first null pointer
		int ptrIndex = PtcgRomConstants.CARD_POINTERS_LOC + PtcgRomConstants.DECK_POINTER_SIZE_IN_BYTES;
		int cardIndex = 0;

		// Read each pointer one at a time until we reach the ending null pointer
		while ((cardIndex = (short) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES)
				) != 0)
		{
			cardIndex += PtcgRomConstants.CARD_POINTER_OFFSET;
			int size = Card.addCardFromBytes(rawBytes, cardIndex, allText, cards.cards());

			// Add the space for the card itself
			if (addresses != null)
			{
				addresses.add(new AddressRange(cardIndex, cardIndex + size));
			}

			// Move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		}
		
		// Add the space for the pointers. The ptrIndex will end at the first text
		if (addresses != null)
		{
			// + 1 because end is exclusive
			addresses.add(new AddressRange(PtcgRomConstants.CARD_POINTERS_LOC, ptrIndex + 1));
		}
		
		return cards;
	}
	
	static void finalizeDataAndGenerateBlocks(Cards cards, Texts texts, Blocks blocks)
	{
		// Reset the singleton -- TODO later: Needed?
		HardcodedEffects.reset();
		
		// Finalize the card data, texts and blocks
		cards.finalizeConvertAndAddData(texts, blocks);
		
		// Convert the text to blocks
		texts.convertAndAddBlocks(blocks);
	}
}
