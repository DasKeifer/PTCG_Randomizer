package config;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;

import constants.CardConstants.CardId;
import data.Card;
import data.Move;
import data.PokemonCard;
import rom.Cards;
import util.IOUtils;

public class MoveAssignments
{
	public static final String FILE_NAME = "MoveAssignments.csv";

	Map<CardId, List<MoveAssignmentData>> assignmentsByCardId;
	
	private MoveAssignments()
	{
		assignmentsByCardId = new EnumMap<>(CardId.class);
	}

	public void assignSpecifiedMoves(Cards<PokemonCard> allCards, MoveExclusions exclusionsToAddTo)
	{
		Cards<PokemonCard> foundCards = allCards.getCardsWithIds(assignmentsByCardId.keySet());
		for (PokemonCard card : foundCards.iterable())
		{
			List<MoveAssignmentData> assigns = assignmentsByCardId.get(card.id);
			for (MoveAssignmentData assign : assigns)
			{
				// Set the move on the card
				card.setMove(assign.getMove(), assign.getMoveSlot());
				
				// Now add an exclusion so it won't get randomized
				exclusionsToAddTo.addMoveExclusion(card.id, assign.getMove().name.toString(), 
						false, // false = do not remove move from rand pool - if they want it removed, they need to do so through moveExclusions
						true); // true = remove from randomization so the move will be kept on the card
			}
		}
	}
	
	public static MoveAssignments parseMoveAssignmentsCsv(Cards<Card> allCards, Component toCenterPopupsOn)
	{
		// TODO: Check move slot while reading and find the moves
		
		MoveAssignments assignments = new MoveAssignments();
		StringBuilder warningsFound = new StringBuilder();
		try
		{
			File file = IOUtils.copyFileFromConfigsIfNotPresent(ConfigConstants.CONFIG_FILE_SOURCE_LOC, FILE_NAME, ConfigConstants.CONFIG_FILE_INSTALL_LOC);
	        
			// Now go ahead and read the file
			try (FileReader configFR = new FileReader(file);
					BufferedReader configReader = new BufferedReader(configFR)
			)
			{
		        String line = configReader.readLine();  // Skip the first title line
		        while((line = configReader.readLine()) != null)  
		        {  
		        	// If we don't limit it, it will remove empty columns so we use a negative
		        	// number to get all the columns without actually limiting it
		        	List<MoveAssignmentData> assigns = parseLine(line, allCards, warningsFound);
		        		
		        	for (MoveAssignmentData assign : assigns)
		        	{
		        		if (assign.isCardIdSet())
		        		{
		        			List<MoveExclusionData> list = exclusions.exclByCardId.computeIfAbsent(excl.getCardId(), 
		        					ll -> new LinkedList<>());
		        			list.add(excl);
		        		}
		        		// Only add it to the move list if its not card specific. Otherwise it
		        		// will get handled above with the cards
		        		else
		        		{
		        			List<MoveExclusionData> list = exclusions.exclByMoveName.computeIfAbsent(excl.getMoveName(), 
		        					ll -> new LinkedList<>());
		        			list.add(excl);
		        		}
		        	}
		        }  
			}
			// IO errors will be caught by the below statement
		}
		catch(IOException e)
		{
			// We have to insert in in reverse order since we are always inserting at the start
			warningsFound.insert(0, e.getMessage());
			warningsFound.insert(0, "Unexpected IO Exception reading move exclusions. Information likely was not read in successfully: ");
		}
		
		if (warningsFound.length() > 0)
		{
			// We have to insert in in reverse order since we are always inserting at the start
			warningsFound.insert(0, IOUtils.NEWLINE);
			warningsFound.insert(0, "\" relative to the JAR:");
			warningsFound.insert(0, ConfigConstants.CONFIG_FILE_INSTALL_LOC);
			warningsFound.insert(0, IOUtils.FILE_SEPARATOR);
			warningsFound.insert(0, "\" config file located in \"");
			warningsFound.insert(0, FILE_NAME);
			warningsFound.insert(0, "The following issue(s) were encoundered while parsing the \"");
			IOUtils.showScrollingMessageDialog(toCenterPopupsOn, warningsFound.toString(), 
					"Issue(s) encountered while parsing " + FILE_NAME, JOptionPane.WARNING_MESSAGE);
		}
        
		return exclusions;
	}
	
	private static List<MoveExclusionData> parseLine(String line, Cards<Card> allCards, StringBuilder warningsFound)
	{
		String[] args = line.split(",", -1);
		
		if (args.length != 3)
		{
			// Add a message to the warning string
			warningsFound.append(IOUtils.NEWLINE);
			warningsFound.append("Line has incorrect number of columns (comma separated) and will be skipped: ");
			warningsFound.append(IOUtils.NEWLINE);
			warningsFound.append("\t");
			warningsFound.append(line);
		}
		else 
		{		
	    	boolean removeFromCard = args[0].equalsIgnoreCase("true");
	    	if (!removeFromCard && !args[0].equalsIgnoreCase("false"))
	    	{
				// Add a message to the warning string
				warningsFound.append(IOUtils.NEWLINE);
				warningsFound.append("Line's \"Remove Move from Card(s)\" field specifies a value other ");
				warningsFound.append("than \"true\" or \"false\". False will be assumed for this line: ");
    			warningsFound.append(IOUtils.NEWLINE);
    			warningsFound.append("\t");
				warningsFound.append(line);
	    	}
	    	
	    	return createMoveExclusionData(removeFromCard, args[1], args[2], allCards, warningsFound, line);
		}
    	
    	return new LinkedList<>();
	}
	
	private static List<MoveExclusionData> createMoveExclusionData(boolean removeFromCard, String cardNameOrId, String moveName, Cards<Card> allCards, StringBuilder warningsFound, String line)
	{
		List<MoveExclusionData> parsedExcl = new LinkedList<>();
		
    	// See if card name is empty
    	if (cardNameOrId.isEmpty())
    	{
    		// If it also doesn't have a move name, its an invalid line
    		if (moveName.isEmpty())
    		{
    			// Add a message to the warning string
    			warningsFound.append(IOUtils.NEWLINE);
    			warningsFound.append("Line does not have either a card name/id or a move name so it - ");
    			warningsFound.append("will be skipped. A line must have at least one of these: ");
    			warningsFound.append(IOUtils.NEWLINE);
    			warningsFound.append("\t");
    			warningsFound.append(line);
    		}
    		else
    		{
    			parsedExcl.add(new MoveExclusionData(CardId.NO_CARD, moveName, removeFromCard));
    		}
    	}
    	else
    	{
	    	// Assume its a card ID
	    	try
	    	{
	    		parsedExcl.add(new MoveExclusionData(CardId.readFromByte(Byte.parseByte(cardNameOrId)), 
	    				moveName, removeFromCard));
	    	}
	    	// Otherwise assume its a name which means it could apply to a number of
	    	// cards
	    	catch (IllegalArgumentException e) // Includes number format exception
	    	{
	    		// Get the cards that match the name
	    		Cards<Card> foundCards = allCards.getCardsWithNameIgnoringNumber(cardNameOrId);
	    		
	    		// Ensure we found at least one
	    		if (foundCards.count() < 1)
	    		{
	    			// Add a message to the warning string
	    			warningsFound.append(IOUtils.NEWLINE);
	    			warningsFound.append("Failed to determine valid card name or id for ");
	    			warningsFound.append("line so it will be skipped: ");
	    			warningsFound.append(IOUtils.NEWLINE);
	    			warningsFound.append("\t");
	    			warningsFound.append(line);
	    		}
	    		else
	    		{
		    		// See if we have a specific card
		    		Card specificCard = Cards.getCardFromNameSetBasedOnNumber(foundCards, cardNameOrId);
		    		if (specificCard == null)
		    		{
		    			
		    		}
		    			
	    			for (Card card : foundCards.toList())
	    			{
	    				parsedExcl.add(new MoveExclusionData(card.id, moveName, removeFromCard));
	    			}
	    		}
	    	}
    	}
    	
		return parsedExcl;
	}
}
