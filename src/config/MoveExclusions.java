package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import constants.CardConstants.CardId;
import constants.CardDataConstants.EnergyType;
import data.Card;
import data.Move;
import rom.Cards;

public class MoveExclusions
{
	public static final String FILE_NAME = "MoveExclusions.csv";
	public static final String FILE_REL_LOCATION = "configs";

	Map<CardId, List<MoveExcl>> exclByCardId;
	Map<String, List<MoveExcl>> exclByMoveName;
	
	private MoveExclusions()
	{
		exclByCardId = new EnumMap<>(CardId.class);
		exclByMoveName = new HashMap<>();
	}

	public boolean isMoveExcluded(CardId id, Move move, boolean removeFromCard)
	{
		return anyExclusionMatches(id, move, removeFromCard, exclByCardId.get(id)) ||
				anyExclusionMatches(id, move, removeFromCard, exclByMoveName.get(move.name.toString()));
	}
	
	private boolean anyExclusionMatches(CardId id, Move move, boolean onlyIfStaysOnCard, List<MoveExcl> foundExcl)
	{
		if (foundExcl != null)
		{
			for (MoveExcl excl : foundExcl)
			{
				if ((!onlyIfStaysOnCard || !excl.removeFromCard) && excl.matchesMove(id, move))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static MoveExclusions parseMoveExclusionsCsv(Cards<Card> allCards)
	{
		MoveExclusions exclusions = new MoveExclusions();
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
	        	List<MoveExcl> excls = parseLine(line, allCards);
	        		
	        	for (MoveExcl excl : excls)
	        	{
	        		boolean isCardSpecific = false;
	        		if (excl.cardId != CardId.NO_CARD)
	        		{
	        			List<MoveExcl> list = exclusions.exclByCardId.computeIfAbsent(excl.cardId, 
	        					ll -> new LinkedList<>());
	        			list.add(excl);
	        			isCardSpecific = true;
	        		}
	        		
	        		// Only add it to the move list if its not card specific. Otherwise it
	        		// will get handled above with the cards
	        		if (!isCardSpecific)
	        		{
	        			if (excl.moveName.isEmpty())
	        			{
	    	        		// TODO: Log or splash on popup
	    	    			System.err.println("Failed to read a valid card name/id or move name "
	    	    					+ "for line so it is being skipped: " + line);
	    	    			break;
	        			}
	
	        			List<MoveExcl> list = exclusions.exclByMoveName.computeIfAbsent(excl.moveName, 
	        					ll -> new LinkedList<>());
	        			list.add(excl);
	        		}
	        	}
	        }  
	        
	        // TODO: Move to finally?
	        configReader.close();
	        configFR.close();
		}
		catch(IOException e)
		{
			// TODO: Log or put on pop up
			System.err.println("IO Exception reading move exclusions: ");
			e.printStackTrace();
		}
        
		return exclusions;
	}
	
	private static List<MoveExcl> parseLine(String line, Cards<Card> allCards)
	{
		List<MoveExcl> parsedExcl = new LinkedList<>();
		String[] args = line.split(",", -1);
		
		// TODO: Check args length
		
    	boolean removeFromCard = Boolean.parseBoolean(args[0]);
    	byte moveDamage = -1;
    	if (!args[3].isEmpty())
    	{
    		// TODO: Catch/log/display and skip
    		moveDamage = Byte.parseByte(args[3]);
    	}

    	EnumMap<EnergyType, Byte> energyCost = new EnumMap<>(EnergyType.class);
    	if (!args[4].isEmpty())
    	{
    		// TODO: Implement
    	}
    	
    	// See if it is empty
    	if (args[1].isEmpty())
    	{
    		// If it also doesn't have a move name, its an invalid line
    		if (args[2].isEmpty())
    		{
    			// TODO: error
    			// Log or popup
    			System.err.println("Line does not have either a card name/id or a move name so it - "
    					+ "will be skipped a line must have at least one of these: " + line);
    		}
    		else
    		{
    			parsedExcl.add(new MoveExcl(
        				CardId.NO_CARD, args[2], moveDamage, energyCost, removeFromCard));
    		}
    	}
    	else
    	{
	    	// Assume its a card ID
	    	try
	    	{
	    		parsedExcl.add(new MoveExcl(CardId.readFromByte(Byte.parseByte(args[1])), 
	    				args[2], moveDamage, energyCost, removeFromCard));
	    	}
	    	// Otherwise assume its a name
	    	catch (IllegalArgumentException e) // Includes number format exception
	    	{
	    		Cards<Card> foundCards = allCards.getCardsWithName(args[1]);
	    		if (foundCards.count() < 1)
	    		{
	    			// TODO: error
	    			// Log or popup
	    			System.err.println("Failed to determine valid card name or id for "
	    					+ "line so it will be skipped: " + line);
	    		}
	    		else
	    		{
	    			for (Card card : foundCards.toList())
	    			{
	    				parsedExcl.add(new MoveExcl(card.id, args[2], moveDamage, energyCost, removeFromCard));
	    			}
	    		}
	    	}
    	}
    	
    	return parsedExcl;
	}
}
