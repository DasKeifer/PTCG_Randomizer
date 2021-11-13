package config;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import constants.CardConstants.CardId;
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

	public void assignSpecifiedMoves(Cards<PokemonCard> cardsToApplyTo, MoveExclusions exclusionsToAddTo)
	{
		Cards<PokemonCard> foundCards = cardsToApplyTo.getCardsWithIds(assignmentsByCardId.keySet());
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
	
	public static MoveAssignments parseMoveAssignmentsCsv(Cards<PokemonCard> allCards, Component toCenterPopupsOn)
	{
		// TODO: Check move slot while reading and find the moves
		
		MoveAssignments assigns = new MoveAssignments();
		StringBuilder warningsFound = new StringBuilder();
		try
		{
			File file = IOUtils.copyFileFromConfigsIfNotPresent(ConfigConstants.CONFIG_FILE_SOURCE_LOC, FILE_NAME, ConfigConstants.CONFIG_FILE_INSTALL_LOC);
	        
			// Now go ahead and read the file
			try (FileReader configFR = new FileReader(file);
					BufferedReader configReader = new BufferedReader(configFR)
			)
			{
				String line;
		        while((line = configReader.readLine()) != null)  
		        {  
		        	// # is the line we use for comments in the files - skip those
		        	line = line.trim();
		        	if (!line.isEmpty() && !line.startsWith("#"))
		        	{
			        	MoveAssignmentData assign = parseLine(line, allCards, warningsFound);
			        	if (assign != null)
			        	{
	                        List<MoveAssignmentData> list = assigns.assignmentsByCardId.computeIfAbsent(
	                        		assign.getCardId(), ll -> new LinkedList<>());
	                        list.add(assign);
			        	}
		        	}
		        }  
			}
			// IO errors will be caught by the below statement
		}
		catch (IOException e)
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
        
		return assigns;
	}
	
	private static MoveAssignmentData parseLine(String line, Cards<PokemonCard> allCards, StringBuilder warningsFound)
	{
    	// If we don't limit it, it will remove empty columns so we use a negative
    	// number to get all the columns without actually limiting it
		String[] args = line.split(",", -1);
		
		// card name, move slot, host name, move name
		if (args.length != 4)
		{
			// Add a message to the warning string
			warningsFound.append(IOUtils.NEWLINE);
			warningsFound.append("Line has incorrect number of columns (comma separated) and will be skipped - found ");
			warningsFound.append(args.length);
			warningsFound.append(" but expected 4:");
			warningsFound.append(IOUtils.NEWLINE);
			warningsFound.append("\t");
			warningsFound.append(line);
		}
		else 
		{		
			// Get the slot number of the destination card
			int cardSlot = parseMoveSlot(args[1].trim());
			if (cardSlot >= 0)
			{
		    	return createMoveAssignmentData(args[0].trim(), cardSlot, args[2].trim(), args[3].trim(), allCards, warningsFound, line);
			}
			else
			{
				// Add a message to the warning string
				warningsFound.append(IOUtils.NEWLINE);
				warningsFound.append("Failed to parse the move slot number to put the move at on the specified card from \"");
				warningsFound.append(args[1]);
				warningsFound.append("\" so the line will be skipped. It must be a valid number from 1 to ");
				warningsFound.append(PokemonCard.MAX_NUM_MOVES);
				warningsFound.append(": ");
				warningsFound.append(IOUtils.NEWLINE);
				warningsFound.append("\t");
				warningsFound.append(line);
			}
		}
    	
    	return null;
	}
	
	private static MoveAssignmentData createMoveAssignmentData(String cardNameOrIdToAdd, int slot, String moveHostCardNameOrId, String moveNameOrIndexToAdd, Cards<PokemonCard> allCards, StringBuilder warningsFound, String line)
	{
		MoveAssignmentData assign = null;
		
		// Get the card to add the move to - already will add errors if not found
		PokemonCard toAddTo = getCardFromString(cardNameOrIdToAdd, allCards, warningsFound, line);
		if (toAddTo != null)
		{
			// Get the host card to get the move from - already will add errors if not found
			PokemonCard hostCard = getCardFromString(moveHostCardNameOrId, allCards, warningsFound, line);
			if (hostCard != null)
			{				
				// Try to get the move slot number from the string
				int moveToAddSlot = parseMoveSlot(moveNameOrIndexToAdd);
				if (moveToAddSlot >= 0)
				{
					// Its tempting to save the card itself but we save the id because we modify a copy
					// of the cards and not the one that is found now
					assign = new MoveAssignmentData(toAddTo.id, slot, hostCard.getMove(moveToAddSlot));
				}
				// If not then try it as a name
				else
				{
					Move moveWithName = hostCard.getMoveWithName(moveNameOrIndexToAdd);
					if (moveWithName != null)
					{
						// Its tempting to save the card itself but we save the id because we modify a copy
						// of the cards and not the one that is found now
						assign = new MoveAssignmentData(toAddTo.id, slot, moveWithName);
					}
					else
					{
						// Add a message to the warning string
						warningsFound.append(IOUtils.NEWLINE);
						warningsFound.append("Failed to find move on the host card with the given name or failed to parse the slot number of the move (must be a valid number from 1 to ");
						warningsFound.append(PokemonCard.MAX_NUM_MOVES);
						warningsFound.append(") to add to the specified card from \"");
						warningsFound.append(moveNameOrIndexToAdd);
						warningsFound.append("\" so the line will be skipped: ");
						warningsFound.append(IOUtils.NEWLINE);
						warningsFound.append("\t");
						warningsFound.append(line);
					}
				}
			}
		}
		
		return assign;
	}
	
	private static PokemonCard getCardFromString(String cardNameOrId, Cards<PokemonCard> allCards, StringBuilder warningsFound, String line)
	{
		PokemonCard foundCard = null;
				
    	// Assume its a card ID
    	try
    	{
    		return allCards.getCardWithId(CardId.readFromByte(Byte.parseByte(cardNameOrId)));
    	}
    	// Otherwise assume its a name which means it could apply to a number of
    	// cards
    	catch (IllegalArgumentException e) // Includes number format exception
    	{
    		// Continued outside catch since it will return otherwise
    	}
    	
		// Get the cards that match the name
		Cards<PokemonCard> foundCards = allCards.getCardsWithNameIgnoringNumber(cardNameOrId);
		
		// Ensure we found at least one
		if (foundCards.count() < 1)
		{
			// Add a message to the warning string
			warningsFound.append(IOUtils.NEWLINE);
			warningsFound.append("Failed to determine valid card name or id of \"");
			warningsFound.append(cardNameOrId);
			warningsFound.append("\" so the line will be skipped: ");
			warningsFound.append(IOUtils.NEWLINE);
			warningsFound.append("\t");
			warningsFound.append(line);
		}
		else
		{
    		// See if we have a specific card and if so return its ID
			foundCard = Cards.getCardFromNameSetBasedOnNumber(foundCards, cardNameOrId);
    		if (foundCard == null)
    		{
    			// No number is an expected case since if there is only one with that name
    			if (foundCards.count() == 1)
    			{
        			foundCard = foundCards.first();
    			}
    			// Otherwise notify of the failure. We don't want to assume but instead force it
    			// to be specific
    			else
    			{
        			// Otherwise we just assume the first
        			// Add a message to the warning string
        			warningsFound.append(IOUtils.NEWLINE);
        			warningsFound.append("The name \"");
        			warningsFound.append(cardNameOrId);
        			warningsFound.append("\" specified has multiple versions of the card but the version number failed ");
        			warningsFound.append("to be read in line so it will be skipped. It should be in the format \""); 
        			warningsFound.append(cardNameOrId);
        			warningsFound.append("_1\" (assuming the first on was intended): ");
        			warningsFound.append(IOUtils.NEWLINE);
        			warningsFound.append("\t");
        			warningsFound.append(line);
    			}
    		}
		}
    	
		return foundCard;
	}
	
	private static int parseMoveSlot(String moveSlot)
	{
		int cardSlot = -1;
		try
		{
			cardSlot = Integer.parseInt(moveSlot);
			if (cardSlot < 1 || cardSlot > PokemonCard.MAX_NUM_MOVES)
			{
				// change it back to -1 to indicate failure
				cardSlot = -1;
			}
			
			// Change it to 0 based
			cardSlot--;
		}
		catch (NumberFormatException nfe)
		{
			//leave it a -1 to indicate failure
		}
		
		return cardSlot;
	}
}
