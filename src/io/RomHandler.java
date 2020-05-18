package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import constants.RomConstants;
import gameData.Card;

public class RomHandler
{
	byte[] romBytes;
	final String FILE_NAME_IN  = "ptcg.gbc";
	final String FILE_NAME_OUT = "ptcg_randomized.gbc";

	boolean verifyRom()
	{
		int index = RomConstants.HEADER_LOCATION;
		for (byte headerByte : RomConstants.HEADER)
		{
			if (headerByte != romBytes[index++])
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void readRaw() 
	{
		try 
		{
			File file = new File(FILE_NAME_IN);
			romBytes = Files.readAllBytes(file.toPath());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!verifyRom())
		{
			System.err.println("File header does not match!");
		}
	}
	
	public void writeRaw()
	{
		try {
			FileOutputStream fos = new FileOutputStream(FILE_NAME_OUT);
			fos.write(romBytes);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Card[] readAllCards()
	{
		int readIndex = RomConstants.FIRST_CARD_BYTE;
		Card[] allCards = new Card[RomConstants.TOTAL_NUM_CARDS];
		
		for (int cardIndex = 0; cardIndex < allCards.length; cardIndex++)
		{
			allCards[cardIndex] = Card.createCardAtIndex(romBytes, readIndex);
			readIndex += allCards[cardIndex].getCardSizeInBytes();
		}
		
		return allCards;
	}
	
	public List<String> readAllText()
	{
		ArrayList<String> allText = new ArrayList<>();
		
		// To be more generic, we will read each one at a time until there are no more
		int readIndex = RomConstants.FIRST_CARD_TEXT_BYTE;
		int lastReadIndex = readIndex;
		
		// This will read all the padding bytes but since a null wont be encountered,
		// it won't be saved in the read in text list
		while (readIndex < RomConstants.LAST_CARD_TEXT_BYTE)
		{
			// End of text
			if (romBytes[readIndex] == 0x00)
			{
				// Read to the null char (but not including it)
				allText.add(new String(romBytes, lastReadIndex, readIndex - lastReadIndex));
				lastReadIndex = readIndex + 1;
			}
			readIndex++;
		}
		
		return allText;
	}
	
	public void writeAllCards(Card[] allCards)
	{
		int writeIndex = RomConstants.FIRST_CARD_BYTE;
		
		// Write each card. We do not need to overflow check
		// since there is currently a fixed number of the cards
		// and the cards themselves are fixed sizes. If more cards
		// are ever added, this logic will need to change to 
		// span gaps of code
		for (Card card : allCards)
		{
			writeIndex = card.writeData(romBytes, writeIndex);
		}

		// Pad with 0xff like the rom does
		while (writeIndex <= RomConstants.LAST_CARD_BYTE)
		{
			romBytes[writeIndex++] = (byte) 0xff;
		}
	}
	
	public void writeAllText(List<String> allText) throws IOException
	{
		int writeIndex = RomConstants.FIRST_CARD_TEXT_BYTE;
		
		for (String text : allText)
		{
			byte[] textBytes = text.getBytes();
			
			// Overflow protection
			if (writeIndex + textBytes.length + 1 >  RomConstants.LAST_CARD_TEXT_BYTE)
			{
				throw new IOException("Ran out of memory to write text!");
			}
			
			System.arraycopy(textBytes, 0, romBytes, writeIndex, textBytes.length);
			writeIndex += textBytes.length;
			romBytes[writeIndex++] = 0x00;
		}
		
		// Pad with 0xff like the rom does
		while (writeIndex <= RomConstants.LAST_CARD_TEXT_BYTE)
		{
			romBytes[writeIndex++] = (byte) 0xff;
		}
	}
}
