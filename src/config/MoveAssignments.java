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
	// TODO: create and move to base class
	StringBuilder warningsFoundParsing;
	
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
			
				// TODO: Figure out how to handle this...
				// Now add an exclusion so it won't get randomized
				exclusionsToAddTo.addMoveExclusion(card.id, assign.getMove().name.toString(), 
						false, // false = do not remove move from rand pool - if they want it removed, they need to do so through moveExclusions
						true, //// true = remove from randomization so the move will be kept on the card
						"Internal error occured while adding exclusion based on Move Assignment with card ID " + assign.getCardId() +
						" and move " + assign.getMove().name.toString()); 
			}
		}
	}
	
	public static MoveAssignments parseMoveAssignmentsCsv(Cards<PokemonCard> allCards, Component toCenterPopupsOn)
	{
		MoveAssignments assignments = new MoveAssignments();
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
			        	MoveAssignmentData assign = assignments.parseLine(line, allCards);
			        	if (assign != null)
			        	{
	                        List<MoveAssignmentData> list = assignments.assignmentsByCardId.computeIfAbsent(
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
			assignments.warningsFoundParsing.insert(0, e.getMessage());
			assignments.warningsFoundParsing.insert(0, "Unexpected IO Exception reading move exclusions. Information likely was not read in successfully: ");
		}
		
		assignments.displayWarningsIfPresent(toCenterPopupsOn);
        
		return assignments;
	}
	
	// TODO: refactor to parseAndAddLine to match exclusion
	private MoveAssignmentData parseLine(String line, Cards<PokemonCard> allCards)
	{
    	// If we don't limit it, it will remove empty columns so we use a negative
    	// number to get all the columns without actually limiting it
		String[] args = line.split(",", -1);
		
		// card name, move slot, host name, move name
		if (args.length != 4)
		{
			// Add a message to the warning string
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("Line has incorrect number of columns (comma separated) and will be skipped - found ");
			warningsFoundParsing.append(args.length);
			warningsFoundParsing.append(" but expected 4:");
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("\t");
			warningsFoundParsing.append(line);
		}
		else 
		{		
			// Get the slot number of the destination card
			int cardSlot = parseMoveSlot(args[1].trim());
			if (cardSlot >= 0)
			{
		    	return createMoveAssignmentData(args[0].trim(), cardSlot, args[2].trim(), args[3].trim(), allCards, line);
			}
			else
			{
				// Add a message to the warning string
				warningsFoundParsing.append(IOUtils.NEWLINE);
				warningsFoundParsing.append("Failed to parse the move slot number to put the move at on the specified card from \"");
				warningsFoundParsing.append(args[1]);
				warningsFoundParsing.append("\" so the line will be skipped. It must be a valid number from 1 to ");
				warningsFoundParsing.append(PokemonCard.MAX_NUM_MOVES);
				warningsFoundParsing.append(": ");
				warningsFoundParsing.append(IOUtils.NEWLINE);
				warningsFoundParsing.append("\t");
				warningsFoundParsing.append(line);
			}
		}
    	
    	return null;
	}
	
	private MoveAssignmentData createMoveAssignmentData(
			String cardNameOrIdToAdd, 
			int slot, 
			String moveHostCardNameOrId, 
			String moveNameOrIndexToAdd, 
			Cards<PokemonCard> allCards,
			String line
	)
	{
		MoveAssignmentData assign = null;
		
		// Get the card to add the move to - already will add errors if not found
		PokemonCard toAddTo = getCardFromString(cardNameOrIdToAdd, allCards, line);
		if (toAddTo != null)
		{
			// Get the host card to get the move from - already will add errors if not found
			PokemonCard hostCard = getCardFromString(moveHostCardNameOrId, allCards, line);
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
						warningsFoundParsing.append(IOUtils.NEWLINE);
						warningsFoundParsing.append("Failed to find move on the host card with the given name or failed to parse the slot number of the move (must be a valid number from 1 to ");
						warningsFoundParsing.append(PokemonCard.MAX_NUM_MOVES);
						warningsFoundParsing.append(") to add to the specified card from \"");
						warningsFoundParsing.append(moveNameOrIndexToAdd);
						warningsFoundParsing.append("\" so the line will be skipped: ");
						warningsFoundParsing.append(IOUtils.NEWLINE);
						warningsFoundParsing.append("\t");
						warningsFoundParsing.append(line);
					}
				}
			}
		}
		
		return assign;
	}
	
	private PokemonCard getCardFromString(String cardNameOrId, Cards<PokemonCard> allCards, String line)
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
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("Failed to determine valid card name or id of \"");
			warningsFoundParsing.append(cardNameOrId);
			warningsFoundParsing.append("\" so the line will be skipped: ");
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("\t");
			warningsFoundParsing.append(line);
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
        			warningsFoundParsing.append(IOUtils.NEWLINE);
        			warningsFoundParsing.append("The name \"");
        			warningsFoundParsing.append(cardNameOrId);
        			warningsFoundParsing.append("\" specified has multiple versions of the card but the version number failed ");
        			warningsFoundParsing.append("to be read in line so it will be skipped. It should be in the format \""); 
        			warningsFoundParsing.append(cardNameOrId);
        			warningsFoundParsing.append("_1\" (assuming the first on was intended): ");
        			warningsFoundParsing.append(IOUtils.NEWLINE);
        			warningsFoundParsing.append("\t");
        			warningsFoundParsing.append(line);
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

	private void displayWarningsIfPresent(Component toCenterPopupsOn)
	{
		if (warningsFoundParsing.length() > 0)
		{
			// We have to insert in in reverse order since we are always inserting at the start
			warningsFoundParsing.insert(0, IOUtils.NEWLINE);
			warningsFoundParsing.insert(0, "\" relative to the JAR:");
			warningsFoundParsing.insert(0, ConfigConstants.CONFIG_FILE_INSTALL_LOC);
			warningsFoundParsing.insert(0, IOUtils.FILE_SEPARATOR);
			warningsFoundParsing.insert(0, "\" config file located in \"");
			warningsFoundParsing.insert(0, FILE_NAME);
			warningsFoundParsing.insert(0, "The following issue(s) were encoundered while parsing the \"");
			IOUtils.showScrollingMessageDialog(toCenterPopupsOn, warningsFoundParsing.toString(), 
					"Issue(s) encountered while parsing " + FILE_NAME, JOptionPane.WARNING_MESSAGE);
		}
	}
}
