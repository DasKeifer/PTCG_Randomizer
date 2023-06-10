package config;

import java.awt.Component;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import config.support.MoveAssignmentData;
import config.support.PtcgAdditionalLineArgs;
import config.support.PtcgLineByLineConfigReader;
import constants.CardConstants.CardId;
import data.CardGroup;
import data.Move;
import data.MonsterCard;
import gbc_framework.utils.IOUtils;

public class MoveAssignments extends PtcgLineByLineConfigReader
{
	Map<CardId, List<MoveAssignmentData>> assignmentsByCardId;
	
	public MoveAssignments(CardGroup<MonsterCard> allCards, Component toCenterPopupsOn)
	{
		super(toCenterPopupsOn);
		
		assignmentsByCardId = new EnumMap<>(CardId.class);
		
		readAndParseConfig(new PtcgAdditionalLineArgs(allCards));
	}
	
	@Override
	public String getName() 
	{
		return "MoveAssignments";
	}
	
	@Override
	public String getExtension() 
	{
		return CSV_CONFIG_FILE_EXTENSION;
	}

	public void assignSpecifiedMoves(CardGroup<MonsterCard> cardsToApplyTo, MoveExclusions exclusionsToAddTo)
	{
		CardGroup<MonsterCard> foundCards = cardsToApplyTo.withIds(assignmentsByCardId.keySet());
		for (MonsterCard card : foundCards.iterable())
		{
			List<MoveAssignmentData> assigns = assignmentsByCardId.get(card.id);
			for (MoveAssignmentData assign : assigns)
			{
				// Set the move on the card
				card.setMove(assign.getMove(), assign.getMoveSlot());
			
				// Now add an exclusion so it won't get randomized
				// Note that at the end of assigning all the moves, we will pop up the move exclusion
				// dialog to show any errors encountered during adding the exclusions
				exclusionsToAddTo.addMoveExclusion(card.id, assign.getMove().name.toString(), 
						false, // false = do not remove move from rand pool - if they want it removed, they need to do so through moveExclusions
						true, //// true = remove from randomization so the move will be kept on the card
						"Internal error occured while adding MoveExclusion based on MoveAssignment with card ID " + assign.getCardId() +
						" and move " + assign.getMove().name.toString()); 
			}
		}
		
		// Now pop up the errors if any occurred while creating and adding exclusions
		exclusionsToAddTo.displayWarningsIfPresent();
	}

	@Override
	protected void parseAndAddLine(String line, PtcgAdditionalLineArgs additionalArgs)
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
		    	createAndAddMoveAssignmentData(args[0].trim(), cardSlot, args[2].trim(), args[3].trim(), additionalArgs.getAllPokes(), line);
			}
			else
			{
				// Add a message to the warning string
				warningsFoundParsing.append(IOUtils.NEWLINE);
				warningsFoundParsing.append("Failed to parse the move slot number to put the move at on the specified card from \"");
				warningsFoundParsing.append(args[1]);
				warningsFoundParsing.append("\" so the line will be skipped. It must be a valid number from 1 to ");
				warningsFoundParsing.append(MonsterCard.MAX_NUM_MOVES);
				warningsFoundParsing.append(": ");
				warningsFoundParsing.append(IOUtils.NEWLINE);
				warningsFoundParsing.append("\t");
				warningsFoundParsing.append(line);
			}
		}
	}
	
	private void addMoveAssignment(CardId toAddTo, int moveSlot, Move moveToAdd)
	{
		// Its tempting to save the card itself but we save the id because we modify a copy
		// of the cards and not the one that is found now
		MoveAssignmentData assign = new MoveAssignmentData(toAddTo, moveSlot, moveToAdd);
        List<MoveAssignmentData> list = assignmentsByCardId.computeIfAbsent(
        		assign.getCardId(), ll -> new LinkedList<>());
        list.add(assign);
	}
	
	private void createAndAddMoveAssignmentData(
			String cardNameOrIdToAdd, 
			int slot, 
			String moveHostCardNameOrId, 
			String moveNameOrIndexToAdd,
			CardGroup<MonsterCard> allCards,
			String line
	)
	{
		// Get the card to add the move to - already will add errors if not found
		MonsterCard toAddTo = getCardFromString(cardNameOrIdToAdd, allCards, line);
		if (toAddTo != null)
		{
			// Get the host card to get the move from - already will add errors if not found
			MonsterCard hostCard = getCardFromString(moveHostCardNameOrId, allCards, line);
			if (hostCard != null)
			{				
				// Try to get the move slot number from the string
				int moveToAddSlot = parseMoveSlot(moveNameOrIndexToAdd);
				if (moveToAddSlot >= 0)
				{
					// Its tempting to save the card itself but we save the id because we modify a copy
					// of the cards and not the one that is found now
					addMoveAssignment(toAddTo.id, slot,  hostCard.getMove(moveToAddSlot));
				}
				// If not then try it as a name
				else
				{
					Move moveWithName = hostCard.getMoveWithName(moveNameOrIndexToAdd);
					if (moveWithName != null)
					{
						addMoveAssignment(toAddTo.id, slot, moveWithName);
					}
					else
					{
						// Add a message to the warning string
						warningsFoundParsing.append(IOUtils.NEWLINE);
						warningsFoundParsing.append("Failed to find move on the host card with the given name or failed to parse the slot number of the move (must be a valid number from 1 to ");
						warningsFoundParsing.append(MonsterCard.MAX_NUM_MOVES);
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
	}
	
	private MonsterCard getCardFromString(String cardNameOrId, CardGroup<MonsterCard> allCards, String line)
	{
		MonsterCard foundCard = null;
				
    	// Assume its a card ID
    	try
    	{
    		return allCards.withId(CardId.readFromByte(Byte.parseByte(cardNameOrId)));
    	}
    	// Otherwise assume its a name which means it could apply to a number of
    	// cards
    	catch (IllegalArgumentException e) // Includes number format exception
    	{
    		// Continued outside catch since it will return otherwise
    	}
    	
		// Get the cards that match the name
		CardGroup<MonsterCard> foundCards = allCards.withNameIgnoringNumber(cardNameOrId);
		
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
			foundCard = CardGroup.fromNameSetBasedOnNumber(foundCards, cardNameOrId);
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
			if (cardSlot < 1 || cardSlot > MonsterCard.MAX_NUM_MOVES)
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
