package rom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import constants.RomConstants;
import gameData.Card;
import util.ByteUtils;

public class RomHandler
{
	private RomHandler() {}
	
	static final String FILE_NAME_IN  = "ptcg.gbc";
	static final String FILE_NAME_OUT = "ptcg_randomized.gbc";
	
	static boolean verifyRom(RomData rom)
	{
		int index = RomConstants.HEADER_LOCATION;
		for (byte headerByte : RomConstants.HEADER)
		{
			if (headerByte != rom.rawBytes[index++])
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static RomData readRom() throws IOException
	{
		RomData rom = new RomData();
		
		readRaw(rom);
		verifyRom(rom);
		
		readTextFromPointers(rom);

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
		
		setTextAndPointers(rom);
		
		writeRaw(rom);
	}
	
	private static void readRaw(RomData rom) throws IOException 
	{
		File file = new File(FILE_NAME_IN);
		rom.rawBytes = Files.readAllBytes(file.toPath());
		rom.cardsByName.clear();
		rom.ptrToText.clear();
	}
	
	private static void writeRaw(RomData rom)
	{
		try {
			FileOutputStream fos = new FileOutputStream(FILE_NAME_OUT);
			fos.write(rom.rawBytes);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void readAllCardsAndProcessPointers(RomData rom)
	{
		Set<Short> convertedTextPtrs = new HashSet<>();
		
		int readIndex = RomConstants.FIRST_CARD_BYTE;
		while (readIndex < RomConstants.LAST_CARD_BYTE)
		{
			Card card = Card.createCardAtIndex(rom.rawBytes, readIndex, rom.ptrToText, convertedTextPtrs);
			
			if (!rom.cardsByName.containsKey(card.name))
			{
				rom.cardsByName.put(card.name, new ArrayList<>());
			}
			rom.cardsByName.get(card.name).add(card);
			
			readIndex += card.getCardSizeInBytes();
			if (rom.rawBytes[readIndex] == (byte) 0xff)
			{
				// No more cards
				break;
			}
			
		}
		
		//rom.ptrToText.keySet().removeAll(convertedTextPtrs);
	}
	
	private static void readTextFromPointers(RomData rom)
	{
		// To be more generic, we will read each one at a time until there are no more
		int ptrIndex = RomConstants.TEXT_POINTERS_LOC;
		int ptr = 0;
		int textIndex = 0;
		short counter = 1; // Starts with 1 - 0 is a "null" ptr
		int firstPtr = Integer.MAX_VALUE;
		
		// Read each pointer one at a time until we reach the ending null pointer
		while (ptrIndex < firstPtr)
		{
			ptr = (int) ByteUtils.readLittleEndian(
					rom.rawBytes, ptrIndex, RomConstants.TEXT_POINTER_SIZE_IN_BYTES) + 
					RomConstants.TEXT_POINTER_OFFSET;
			if (ptr < firstPtr)
			{
				firstPtr = ptr;
			}
			
			textIndex = ptr;
			while (rom.rawBytes[++textIndex] != 0x00);
			// Read to the null char (but not including it)
			rom.ptrToText.put(counter++, new String(rom.rawBytes, ptr, textIndex - ptr));
			System.out.println(counter - 1 + ", " + rom.ptrToText.get((short)(counter - 1)));
			
			ptrIndex += RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		}
	}
	
	private static void setAllCards(RomData rom)
	{
		int writeIndex = RomConstants.FIRST_CARD_BYTE;
		
		// TODO: need to flatten and reorder
		
		// Write each card. We do not need to overflow check
		// since there is currently a fixed number of the cards
		// and the cards themselves are fixed sizes. If more cards
		// are ever added, this logic will need to change to 
		// span gaps of code
		for (Entry<String, List<Card>> cards : rom.cardsByName.entrySet())
		{
			for (Card version : cards.getValue())
			{
				writeIndex = version.writeData(rom.rawBytes, writeIndex);
			}
		}

		// Pad with 0xff like the rom does
		while (writeIndex <= RomConstants.LAST_CARD_BYTE)
		{
			rom.rawBytes[writeIndex++] = (byte) 0xff;
		}
	}
	
	private static void setTextAndPointers(RomData rom) throws IOException
	{
		// First write the 0 index "null" text pointer
		int ptrIndex = RomConstants.TEXT_POINTER_OFFSET - RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		for (int byteIndex = 0; byteIndex < RomConstants.TEXT_POINTER_SIZE_IN_BYTES; byteIndex++)
		{
			rom.rawBytes[ptrIndex++] = 0;
		}
		
		// determine where the first text will go based off the number of text we have
		// The null pointer was already taken care of so we don't need to handle it here
		int textPtr = ptrIndex + rom.ptrToText.size() * RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
		
		// Now for each text, write the pointer then write the text at that address
		// Note we intentionally do a index based lookup instead of iteration in order to
		// ensure that the IDs are sequential as they need to be (i.e. there are no gaps)
		// We start at 1 because 0 is a null ptr
		for (short textId = 1; textId < rom.ptrToText.size() + 1; textId++)
		{
			ByteUtils.writeLittleEndian(textPtr, rom.rawBytes, ptrIndex, RomConstants.TEXT_POINTER_SIZE_IN_BYTES);
			textPtr += RomConstants.TEXT_POINTER_SIZE_IN_BYTES;
			
			byte[] textBytes = rom.ptrToText.get(textId).getBytes();
			System.arraycopy(textBytes, 0, rom.rawBytes, textPtr, textBytes.length);
			textPtr += textBytes.length;
			
			// Write trailing null
			rom.rawBytes[textPtr++] = 0x00;
		}
		
		// Until it is determined to be necessary, don't worry about padding remaining space with 0xff
	}
}
