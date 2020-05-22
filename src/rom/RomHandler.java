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

		System.out.println("starting");
		ByteUtils.printBytes(rom.rawBytes, 0x30c5c, 2, 10);
		System.out.println();
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
		//setAllText(rom);		
		
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
	
	private static void readAllText(RomData rom)
	{
		// To be more generic, we will read each one at a time until there are no more
		int readIndex = RomConstants.FIRST_TEXT_BYTE;
		int lastReadIndex = readIndex;
		short counter = 1; // Starts with 1 - 0 is a "null" text
		
		// This will read all the padding bytes but since a null wont be encountered,
		// it won't be saved in the read in text list
		while (readIndex < RomConstants.LAST_TEXT_BYTE)
		{
			// End of text
			if (rom.rawBytes[readIndex] == 0x00)
			{
				// Read to the null char (but not including it)
				rom.ptrToText.put(counter++, new String(rom.rawBytes, lastReadIndex, readIndex - lastReadIndex));
				//System.out.println(counter + ", " + rom.ptrToText.get((short)(counter - 1)));
				lastReadIndex = readIndex + 1;
			}
			
			readIndex++;
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
	
	private static void setAllText(RomData rom) throws IOException
	{
		int writeIndex = RomConstants.FIRST_TEXT_BYTE;
		
		// TODO Check the ids don't have gaps
		for (Entry<Short, String> text : rom.ptrToText.entrySet())
		{
			byte[] textBytes = text.getValue().getBytes();
			
			// Overflow protection
			if (writeIndex + textBytes.length + 1 >  RomConstants.LAST_TEXT_BYTE)
			{
				throw new IOException("Ran out of memory to write text!");
			}
			
			// All texts start with 0x06 and end with 0x00
			System.arraycopy(textBytes, 0, rom.rawBytes, writeIndex, textBytes.length);
			writeIndex += textBytes.length;
			rom.rawBytes[writeIndex++] = 0x00;
		}
		
		// Pad with 0xff like the rom does
		while (writeIndex <= RomConstants.LAST_TEXT_BYTE)
		{
			rom.rawBytes[writeIndex++] = (byte) 0xff;
		}
	}
}
