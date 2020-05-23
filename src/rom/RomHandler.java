package rom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import constants.RomConstants;
import gameData.Card;
import gameData.CardVersions;
import util.ByteUtils;

public class RomHandler
{
	private RomHandler() {}
	
	static final String FILE_NAME_IN  = "ptcg.gbc";
	static final String FILE_NAME_OUT = "ptcg_randomized.gbc";
	
	static boolean verifyRom(byte[] rawBytes)
	{
		int index = RomConstants.HEADER_LOCATION;
		for (byte headerByte : RomConstants.HEADER)
		{
			if (headerByte != rawBytes[index++])
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static RomData readRom() throws IOException
	{
		RomData rom = new RomData();
		
		rom.rawBytes = readRaw();
		verifyRom(rom.rawBytes);
		
		IdsToText allText = readAllTextFromPointers(rom.rawBytes);
		rom.cardsByName = readCardsFromPointersAndConvertPointers(rom.rawBytes, allText);
		
		rom.idsToText = allText;

		//ByteUtils.printBytes(rom.rawBytes, 0x34000, 3, 0xBAF);
		//printBytes(rom.rawBytes, 0x30c5c, 240*2, 2);
		//printBytes(rom.rawBytes, 0x30e28, 65, 1);

		//readAllText(rom);
		//readAllCardsAndProcessPointers(rom);
		
		//groupByNameAndUpdateDesc();
		
		return rom;
	}
	
	public static void writeRom(RomData rom) throws IOException
	{
		//insertNameInDesc();
		
		//createCardPointers();
		
		//setAllCards(rom);
		
		setTextAndPointers(rom.rawBytes, rom.idsToText);
		
		writeRaw(rom.rawBytes);
	}
	
	private static byte[] readRaw() throws IOException 
	{
		File file = new File(FILE_NAME_IN);
		return Files.readAllBytes(file.toPath());
	}
	
	private static void writeRaw(byte[] rawBytes)
	{
		try {
			FileOutputStream fos = new FileOutputStream(FILE_NAME_OUT);
			fos.write(rawBytes);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Map<String, CardVersions> readCardsFromPointersAndConvertPointers(byte[] rawBytes, IdsToText allText)
	{
		Map<String, CardVersions> cardsByName = new HashMap<>();
		Set<Short> convertedTextPtrs = new HashSet<>();

		// Read the text based on the pointer map in the rom
		int ptrIndex = RomConstants.CARD_POINTERS_LOC;
		int cardIndex = 0;

		// Read each pointer one at a time until we reach the ending null pointer
		while ((cardIndex = (short) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, RomConstants.CARD_POINTER_SIZE_IN_BYTES)
				) != 0)
		{
			cardIndex += RomConstants.CARD_POINTER_OFFSET;
			Card.addCardAtIndex(rawBytes, cardIndex, cardsByName, allText, convertedTextPtrs);

			// Move our text pointer to the next pointer
			ptrIndex += RomConstants.CARD_POINTER_SIZE_IN_BYTES;
		}
		
		//rom.ptrToText.keySet().removeAll(convertedTextPtrs);
		return cardsByName;
	}
	
	private static IdsToText readAllTextFromPointers(byte[] rawBytes)
	{
		IdsToText textMap = new IdsToText();
		 
		 // Read the text based on the pointer map in the rom
		int ptrIndex = RomConstants.TEXT_POINTERS_LOC;
		int ptr = 0;
		int textIndex = 0;
		int firstPtr = Integer.MAX_VALUE;
		
		// Read each pointer one at a time until we reach the ending null pointer
		while (ptrIndex < firstPtr)
		{
			ptr = (int) ByteUtils.readLittleEndian(
					rawBytes, ptrIndex, RomConstants.TEXT_POINTER_SIZE_IN_BYTES) + 
					RomConstants.TEXT_POINTER_OFFSET;
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
			ptrIndex += RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		}
		
		return textMap;
	}
	
//	private static void setAllCards(RomData rom)
//	{
//		int writeIndex = RomConstants.FIRST_CARD_BYTE;
//		
//		// TODO: need to flatten and reorder
//		
//		// Write each card. We do not need to overflow check
//		// since there is currently a fixed number of the cards
//		// and the cards themselves are fixed sizes. If more cards
//		// are ever added, this logic will need to change to 
//		// span gaps of code
//		for (Entry<String, List<Card>> cards : rom.cardsByName.entrySet())
//		{
//			for (Card version : cards.getValue())
//			{
//				writeIndex = version.writeData(rom.rawBytes, writeIndex);
//			}
//		}
//
//		// Pad with 0xff like the rom does
//		while (writeIndex <= RomConstants.LAST_CARD_BYTE)
//		{
//			rom.rawBytes[writeIndex++] = (byte) 0xff;
//		}
//	}
	
	private static void setTextAndPointers(byte[] rawBytes, IdsToText ptrToText) throws IOException
	{
		// First write the 0 index "null" text pointer
		int ptrIndex = RomConstants.TEXT_POINTERS_LOC - RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		for (int byteIndex = 0; byteIndex < RomConstants.TEXT_POINTER_SIZE_IN_BYTES; byteIndex++)
		{
			rawBytes[ptrIndex++] = 0;
		}
		
		// determine where the first text will go based off the number of text we have
		// The null pointer was already taken care of so we don't need to handle it here
		int textIndex = RomConstants.TEXT_POINTER_OFFSET + (ptrToText.count() + 1) * RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		
		// Now for each text, write the pointer then write the text at that address
		// Note we intentionally do a index based lookup instead of iteration in order to
		// ensure that the IDs are sequential as they need to be (i.e. there are no gaps)
		// We start at 1 because 0 is a null ptr
		for (short textId = 1; textId < ptrToText.count() + 1; textId++)
		{
			if (textId < 20)
			{
				System.out.println(ptrToText.getAtId(textId) + ", " + 
						Arrays.toString(ptrToText.getAtId(textId).getBytes()) + ", " + 
						ptrToText.getAtId(textId).getBytes().length + ", " + textIndex);
			}
			ByteUtils.writeLittleEndian(textIndex - RomConstants.TEXT_POINTER_OFFSET, rawBytes, ptrIndex, RomConstants.TEXT_POINTER_SIZE_IN_BYTES);
			ptrIndex += RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
			
			byte[] textBytes = ptrToText.getAtId(textId).getBytes();
			System.arraycopy(textBytes, 0, rawBytes, textIndex, textBytes.length);
			textIndex += textBytes.length;
			
			// Write trailing null
			rawBytes[textIndex++] = 0x00;
		}
		
		// Until it is determined to be necessary, don't worry about padding remaining space with 0xff
	}
}
