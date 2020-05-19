package rom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import constants.RomConstants;
import gameData.Card;

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
		
		readAllCards(rom);
		readAllText(rom);
		
		return rom;
	}
	
	public static void writeRom(RomData rom) throws IOException
	{
		setAllCards(rom);
		setAllText(rom);
		writeRaw(rom);
	}
	
	private static void readRaw(RomData rom) throws IOException 
	{
		File file = new File(FILE_NAME_IN);
		rom.rawBytes = Files.readAllBytes(file.toPath());
		rom.cards = new Card[0];
		rom.text.clear();
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
	
	private static void readAllCards(RomData rom)
	{
		int readIndex = RomConstants.FIRST_CARD_BYTE;
		rom.cards = new Card[RomConstants.TOTAL_NUM_CARDS];
		
		for (int cardIndex = 0; cardIndex < rom.cards.length; cardIndex++)
		{
			rom.cards[cardIndex] = Card.createCardAtIndex(rom.rawBytes, readIndex);
			readIndex += rom.cards[cardIndex].getCardSizeInBytes();
		}
	}
	
	private static void readAllText(RomData rom)
	{
		// To be more generic, we will read each one at a time until there are no more
		int readIndex = RomConstants.FIRST_TEXT_BYTE;
		int lastReadIndex = readIndex;
		
		// This will read all the padding bytes but since a null wont be encountered,
		// it won't be saved in the read in text list
		while (readIndex < RomConstants.LAST_TEXT_BYTE)
		{
			// End of text
			if (rom.rawBytes[readIndex] == 0x00)
			{
				// Read to the null char (but not including it)
				rom.text.add(new String(rom.rawBytes, lastReadIndex, readIndex - lastReadIndex));
				lastReadIndex = readIndex + 1;
			}
			
			readIndex++;
		}
	}
	
	private static void setAllCards(RomData rom)
	{
		int writeIndex = RomConstants.FIRST_CARD_BYTE;
		
		// Write each card. We do not need to overflow check
		// since there is currently a fixed number of the cards
		// and the cards themselves are fixed sizes. If more cards
		// are ever added, this logic will need to change to 
		// span gaps of code
		for (Card card : rom.cards)
		{
			writeIndex = card.writeData(rom.rawBytes, writeIndex);
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
		
		for (String text : rom.text)
		{
			byte[] textBytes = text.getBytes();
			
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
