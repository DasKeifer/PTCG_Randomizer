package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import constants.RomConstants;
import gameData.Card;

public class RomHandler
{
	byte[] romBytes;
	final String FILE_NAME_IN  = "ptcg.gbc";
	final String FILE_NAME_OUT = "ptcg_randomized.gbc";
	
	public void read() 
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
	
	public Card[] readCards()
	{
		int readIndex = RomConstants.FIRST_CARD;
		Card[] allCards = new Card[RomConstants.TOTAL_NUM_CARDS];
		
		for (int cardIndex = 0; cardIndex < allCards.length; cardIndex++)
		{
			allCards[cardIndex] = Card.createCardAtIndex(romBytes, readIndex);
			readIndex += allCards[cardIndex].getCardSizeInBytes();
		}
		
		return allCards;
	}
	
	public void write()
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
}
