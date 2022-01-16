package rom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import bps_writer.BpsWriter;
import constants.CharMapConstants;
import constants.PtcgRomConstants;
import constants.CharMapConstants.CharSetPrefix;
import data.Card;
import gbc_framework.rom_addressing.AddressRange;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.utils.ByteUtils;
import rom_packer.Blocks;

public class RomIO
{
	private RomIO() {}
	
	private static void verifyRom(byte[] rawBytes)
	{
		// TODO later: Do a CRC instead/in addition to? Maybe if we go with the BPS patch format
		int index = PtcgRomConstants.HEADER_LOCATION;
		for (byte headerByte : PtcgRomConstants.HEADER)
		{
			if (headerByte != rawBytes[index++])
			{
				throw new IllegalArgumentException("Failed to verify the rom: Header is incorrect!");
			}
		}
	}
	
	public static byte[] readRaw(File romFile) throws IOException 
	{
		byte[] rawBytes = Files.readAllBytes(romFile.toPath());
		RomIO.verifyRom(rawBytes);
		return rawBytes;
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

	// Note assumes that the first text in the pointer list is the first in the file as well. This is required
	// since there is no null between the text pointer map and the texts themselves
	static Texts readTextsFromData(byte[] rawBytes, Blocks toBlankSpaceIn)
	{		 
		// TODO: Optimize address range adding since they will mostly be in order
		
		Texts texts = new Texts();
		// Intentionally not clearing addressesReadToAddTo to support chaining calls/adding
		// to it via other functions
		
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
			
			// Ensure its either null or starts with the prefix char
			CharSetPrefix charSet = CharSetPrefix.readFromByte(rawBytes[textIndex]);
			if (charSet != CharSetPrefix.Empty)
			{
				// Loop until we find the ending character if its not an empty text
				while (rawBytes[++textIndex] != CharMapConstants.TEXT_END_CHAR);
			}
			
			// Read the string to the null char (but not including it) and store where
			// it was read from
			texts.insertTextAtNextId(new String(rawBytes, ptr, textIndex - ptr), ptr);
			
			// Add it to the list of spaces for the text itself
			toBlankSpaceIn.addBlankedBlock(new AddressRange(ptr, textIndex + 1));

			// Move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		}

		// Note that the texts for whatever reason doesn't end with a nullptr so
		// that's why we don't add the pointer size one last time like done for
		// reading in the cards
		
		// Add the space for the pointers. The ptrIndex will end at the first text
		// + 1 because end is exclusive
		toBlankSpaceIn.addBlankedBlock(new AddressRange(PtcgRomConstants.TEXT_POINTERS_LOC, ptrIndex + 1));
		
		return texts;
	}
	
	static Cards readCardsFromData(byte[] rawBytes, Texts allText, Blocks toBlankSpaceIn)
	{
		// TODO: Optimize address range adding since they will mostly be in order
		
		Cards cards = new Cards();
		// Intentionally not clearing addressesReadToAddTo to support chaining calls/adding
		// to it via other functions

		// Read the cards based on the pointer map in the rom
		// Skip the first null pointer
		int ptrIndex = PtcgRomConstants.CARD_POINTERS_LOC + PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		int cardIndex = 0;

		// Read each pointer one at a time until we reach the ending null pointer
		while ((cardIndex = (short) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES)
				) != 0)
		{
			cardIndex += PtcgRomConstants.CARD_POINTER_OFFSET;
			int size = Card.addCardFromBytes(rawBytes, cardIndex, allText, cards.cards());

			// Add the space for the card itself
			toBlankSpaceIn.addBlankedBlock(new AddressRange(cardIndex, cardIndex + size));

			// Move our text pointer to the next pointer
			ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		}
		
		// Move it one last time for the trailing nullptr that finishes the list
		ptrIndex += PtcgRomConstants.CARD_POINTER_SIZE_IN_BYTES;
		
		// Add the space for the pointers. The ptrIndex will end at the first text
		// + 1 because end is exclusive
		toBlankSpaceIn.addBlankedBlock(new AddressRange(PtcgRomConstants.CARD_POINTERS_LOC, ptrIndex + 1));
		
		return cards;
	}

	public static void writeBpsPatch(File patchFile, byte[] rawBytes, Blocks blocks, AssignedAddresses assignedAddresses) 
	{
		// Now actually write to the bytes
		BpsWriter writer = new BpsWriter();
		try 
		{
			blocks.writeBlocks(writer, assignedAddresses);
			writer.blankUnusedSpace(blocks.getAllBlankedBlocks());
			writer.createBlanksAndFillEmptyHunksWithSourceRead(rawBytes.length);
			writer.writeBps(patchFile, rawBytes);
		} 
		catch (IOException e)
		{
			// TODO later: Auto-generated catch block
			e.printStackTrace();
		}
	}
}
