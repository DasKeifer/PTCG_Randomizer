package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import constants.CardConstants.CardId;
import constants.CardDataConstants.EnergyType;

public class MoveExclusions
{
	public static final String FILE_NAME = "MoveExclusions.csv";
	public static final String FILE_REL_LOCATION = "configs";
	
	boolean removeFromCard;
	String cardName;
	CardId cardId;
	String moveName;
	byte moveDamage;
	EnumMap<EnergyType, Byte> energyCost;
	
	private MoveExclusions(String[] args)
	{
		// TODO: Check args length
		
    	removeFromCard = Boolean.parseBoolean(args[0]);
    	
    	// Assume its a card ID
    	try
    	{
    		cardId = CardId.readFromByte(Byte.parseByte(args[1]));
    		cardName = "";
    	}
    	// Otherwise assume its a name
    	catch (IllegalArgumentException e) // Includes number format exception
    	{
    		cardName = args[1];
    		cardId = CardId.NO_CARD;
    	}
    	
    	moveName = args[2];
    	if (!args[3].isEmpty())
    	{
    		moveDamage = Byte.parseByte(args[3]);
    	}
    	else
    	{
    		moveDamage = -1;
    	}
    	
    	energyCost = new EnumMap<>(EnergyType.class);
    	if (!args[4].isEmpty())
    	{
    		// TODO: Implement
    	}
	}
	
	public static List<MoveExclusions> parseMoveExclusionsCsv()
	{
		List<MoveExclusions> exclusions = new LinkedList<>();
		try
		{
			File file = ConfigUtils.copyFileFromConfigsIfNotPresent(FILE_NAME, FILE_REL_LOCATION);
	        
	        // Now go ahead and read the file
	        FileReader configFR = new FileReader(file);
	        BufferedReader configReader = new BufferedReader(configFR); 
	        String line = configReader.readLine();  // Skip the first title line
	        while((line = configReader.readLine()) != null)  
	        {  
	        	// If we don't limit it, it will remove empty columns so we use a negative
	        	// number to get all the columns without actually limiting it
	        	String[] columns = line.split(",", -1);
	        	try
	        	{
	        		exclusions.add(new MoveExclusions(columns));   
	        	}
	        	catch (NumberFormatException e)
	        	{
	        		// TODO: Log
	    			System.err.println("Failed to read move exclusion line: " + line);
	        	}
	        }  
	        // TODO: Move to finally?
	        configReader.close();
	        configFR.close();
		}
		catch(IOException e)
		{
			// TODO: Log
			System.err.println("IO Exception reading move exclusions: ");
			e.printStackTrace();
		}
        
		return exclusions;
	}
}
