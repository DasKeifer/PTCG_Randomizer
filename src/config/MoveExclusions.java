package config;

import java.awt.Component;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.support.MoveExclusionAdditionalLineArgs;
import config.support.MoveExclusionData;
import config.support.PtcgAdditionalLineArgs;
import config.support.PtcgLineByLineConfigReader;
import constants.CardConstants.CardId;
import data.CardGroup;
import data.Move;
import data.MonsterCard;
import data.romtexts.CardName;
import gbc_framework.utils.IOUtils;

public class MoveExclusions extends PtcgLineByLineConfigReader
{
	Map<CardId, List<MoveExclusionData>> exclByCardId;
	Map<String, List<MoveExclusionData>> exclByMoveName;
	

	
	public MoveExclusions(CardGroup<MonsterCard> allCards, Component toCenterPopupsOn)
	{
		super(toCenterPopupsOn);
		
		exclByCardId = new EnumMap<>(CardId.class);
		exclByMoveName = new HashMap<>();
		
		readAndParseConfig(new MoveExclusionAdditionalLineArgs(allCards));
	}
	
	@Override
	public String getName() 
	{
		return "MoveExclusions";
	}	
	
	@Override
	public String getExtension() 
	{
		return CSV_CONFIG_FILE_EXTENSION;
	}
	
	public boolean isMoveRemovedFromPool(CardId id, Move move)
	{
		return anyExclusionMatches(id, move, true, exclByCardId.get(id)) ||
				anyExclusionMatches(id, move, true, exclByMoveName.get(move.name.toString()));
	}

	public boolean isMoveExcludedFromRandomization(CardId id, Move move)
	{
		return anyExclusionMatches(id, move, false, exclByCardId.get(id)) ||
				anyExclusionMatches(id, move, false, exclByMoveName.get(move.name.toString()));
	}
	
	private boolean anyExclusionMatches(CardId id, Move move, boolean checkAgainstRemovedFromPoolListInsteadOfExludedFromRandList, List<MoveExclusionData> foundExcl)
	{
		if (foundExcl != null)
		{
			for (MoveExclusionData excl : foundExcl)
			{
				if (checkAgainstRemovedFromPoolListInsteadOfExludedFromRandList)
				{
					return excl.isRemoveFromPool() && excl.matchesMove(id, move);
				}
				else 
				{
					return excl.isExcludeFromRandomization() && excl.matchesMove(id, move);
				}
			}
		}
		
		return false;
	}
	
	public boolean addMoveExclusion(CardId cardId, String moveName, boolean removeFromPool, boolean excludeFromRandomization, String sourceLine)
	{
		return validateAndAddMoveExclusion(new MoveExclusionData(cardId, moveName, removeFromPool, excludeFromRandomization), sourceLine);
	}
	
	private boolean validateAndAddMoveExclusion(MoveExclusionData excl, String line)
	{
		boolean success = false;
		if (excl.isCardIdSet())
		{
			List<MoveExclusionData> list = exclByCardId.computeIfAbsent(excl.getCardId(), 
					ll -> new LinkedList<>());
			list.add(excl);
			success = true;
		}
		// Only add it to the move list if its not card specific. Otherwise it
		// will get handled above with the cards
		else if (excl.isMoveNameSet())
		{
			List<MoveExclusionData> list = exclByMoveName.computeIfAbsent(excl.getMoveName(), 
					ll -> new LinkedList<>());
			list.add(excl);
			success = true;
		}
		
		if (!success)
		{
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("Failed to validate and add MoveExclusion for card id \"");
			warningsFoundParsing.append(excl.getCardId().toString());
			warningsFoundParsing.append("\" with move name \"");
			warningsFoundParsing.append(excl.getMoveName());
			warningsFoundParsing.append(" while parsing line for unknown reasons - it will be skipped:");
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("\t");
			warningsFoundParsing.append(line);
		}

		return success;
	}

	@Override
	protected void parseAndAddLine(String line, PtcgAdditionalLineArgs moveExclLineArgs)
	{
		// Cast the arg to the expected type and stop if it fails
		// Should never fail but this is to play it safe
		MoveExclusionAdditionalLineArgs additionalArgs;
		try
		{
			additionalArgs = (MoveExclusionAdditionalLineArgs) moveExclLineArgs;
		}
		catch (ClassCastException cce)
		{
			warningsFoundParsing.append("Internal Logic Error! Invalid PtcgAdditionalLineArgs passed for ");
			warningsFoundParsing.append("MoveExclusions - they must be of type MoveExclusionAdditionalLineArgs!");
			return;
		}
		
    	// If we don't limit it, it will remove empty columns so we use a negative
    	// number to get all the columns without actually limiting it
		String[] args = line.split(",", -1);
		
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
	    	boolean removeFromPool = args[0].equalsIgnoreCase("true");
	    	if (!removeFromPool && !args[0].equalsIgnoreCase("false"))
	    	{
				// Add a message to the warning string
				warningsFoundParsing.append(IOUtils.NEWLINE);
				warningsFoundParsing.append("Line's \"Remove Move from randomization pool\" field specifies a value of \"");
				warningsFoundParsing.append(args[0]);
				warningsFoundParsing.append("\" which is not either \"true\" or \"false\". False will be assumed for this line: ");
    			warningsFoundParsing.append(IOUtils.NEWLINE);
    			warningsFoundParsing.append("\t");
				warningsFoundParsing.append(line);
	    	}
	    	
	    	boolean excludeFromRandomization = args[1].equalsIgnoreCase("true");
	    	if (!excludeFromRandomization && !args[1].equalsIgnoreCase("false"))
	    	{
				// Add a message to the warning string
				warningsFoundParsing.append(IOUtils.NEWLINE);
				warningsFoundParsing.append("Line's \"Exclude Move from Randomization (i.e. Move stays on card(s))\" field specifies a value of \"");
				warningsFoundParsing.append(args[1]);
				warningsFoundParsing.append("\" which is not either \"true\" or \"false\". False will be assumed for this line: ");
    			warningsFoundParsing.append(IOUtils.NEWLINE);
    			warningsFoundParsing.append("\t");
				warningsFoundParsing.append(line);
	    	}
	    	
	    	// See if card name is empty
	    	if (args[2].isEmpty())
	    	{
	    		createAndAddMoveExclusionDataWithNoCard(
	    				excludeFromRandomization, excludeFromRandomization, args[3], additionalArgs.getAllMovesNames(), line);
	    	}
	    	else
	    	{
	    		createAndAddMoveExclusionDataForCard(removeFromPool, excludeFromRandomization, args[2], args[3], additionalArgs.getAllPokes(), line);
	    	}
		}
	}
	
	private void createAndAddMoveExclusionDataWithNoCard(
			boolean removeFromPool, 
			boolean excludeFromRandomization, 
			String moveName, 
			Set<String> allMovesNames,
			String line
	)
	{
		// If it also doesn't have a move name, its an invalid line
		if (moveName.isEmpty())
		{
			// Add a message to the warning string
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("Line does not have either a card name/id or a move name so it - ");
			warningsFoundParsing.append("will be skipped. A line must have at least one of these: ");
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("\t");
			warningsFoundParsing.append(line);
		}
		else
		{
			// Ensure the move is a valid move on at least one card
			if (allMovesNames.contains(moveName.trim().toLowerCase()))
			{
				addMoveExclusion(CardId.NO_CARD, moveName, removeFromPool, excludeFromRandomization, line);
			}
			else
			{
    			// Add a message to the warning string
    			warningsFoundParsing.append(IOUtils.NEWLINE);
    			warningsFoundParsing.append("Failed to find any card with the specified move for \"");
    			warningsFoundParsing.append(moveName);
    			warningsFoundParsing.append("\" so the line will be skipped:");
    			warningsFoundParsing.append(IOUtils.NEWLINE);
    			warningsFoundParsing.append("\t");
    			warningsFoundParsing.append(line);
			}
		}
	}
	
	private void createAndAddMoveExclusionDataForCard(
			boolean removeFromPool, 
			boolean excludeFromRandomization, 
			String cardNameOrId, 
			String moveName, 
			CardGroup<MonsterCard> allPokes, 
			String line
	)
	{
    	// Assume its a card ID
    	try
    	{
    		addMoveExclusion(CardId.readFromByte(Byte.parseByte(cardNameOrId)), 
    				moveName, removeFromPool, excludeFromRandomization, line);
    	}
    	// Otherwise assume its a name which means it could apply to a number of
    	// cards
    	catch (IllegalArgumentException e) // Includes number format exception
    	{
    		CardGroup<MonsterCard> foundCards = allPokes.withNameIgnoringNumber(cardNameOrId);
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
    			// If we found some, see if its numbered
	    		if (CardName.doesHaveNumber(cardNameOrId))
	    		{
	    			createAndAddMoveExclusionDataForCardNameWithNumber(
	    					removeFromPool, excludeFromRandomization, cardNameOrId, moveName, foundCards, line);
	    		}
	    		// Otherwise it applies to all
	    		else
	    		{
	    			createAndAddMoveExclusionDataForAllCardInSet(
	    					removeFromPool, excludeFromRandomization, cardNameOrId, moveName, foundCards, line);
	    		}
    		}
    	}
	}
	
	private void createAndAddMoveExclusionDataForCardNameWithNumber(
			boolean removeFromPool, 
			boolean excludeFromRandomization, 
			String cardNameWithNumber, 
			String moveName, 
			CardGroup<MonsterCard> foundCards,
			String line
	)
	{
		MonsterCard specificCard = CardGroup.fromNameSetBasedOnNumber(foundCards, cardNameWithNumber);
		if (specificCard == null)
		{
			// Add a message to the warning string
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("Detected the card name has a number (e.g. \"<nameOnCard>_1\") ");
			warningsFoundParsing.append("but failed to get the card with that number (i.e. failed to ");
			warningsFoundParsing.append("parse the number or it was out of bounds) from the set found with ");
			warningsFoundParsing.append("the card name (found ");
			warningsFoundParsing.append(foundCards.count());
			warningsFoundParsing.append(") for \"");
			warningsFoundParsing.append(cardNameWithNumber);
			warningsFoundParsing.append("\". Line will be skipped:");
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("\t");
			warningsFoundParsing.append(line);
		}
		// Found the card!
		else 
		{
			// If there is a move name, make sure it is a valid move
			if (!moveName.isEmpty() && specificCard.getMoveWithName(moveName) == null)
			{
    			// Add a message to the warning string
    			warningsFoundParsing.append(IOUtils.NEWLINE);
    			warningsFoundParsing.append("Found the specified card (");
    			warningsFoundParsing.append(specificCard.name.toString());
    			warningsFoundParsing.append(" with id ");
    			warningsFoundParsing.append(specificCard.id.getValue());
    			warningsFoundParsing.append(") but failed to find move with name \"");
    			warningsFoundParsing.append(moveName);
    			warningsFoundParsing.append("\" on the card so the line will be skipped:");
    			warningsFoundParsing.append(IOUtils.NEWLINE);
    			warningsFoundParsing.append("\t");
    			warningsFoundParsing.append(line);
			}
			else 
			{
				addMoveExclusion(specificCard.id, moveName, removeFromPool, excludeFromRandomization, line);
			}
		}
	}
	
	private void createAndAddMoveExclusionDataForAllCardInSet(
			boolean removeFromPool, 
			boolean excludeFromRandomization, 
			String cardName, 
			String moveName, 
			CardGroup<MonsterCard> foundCards, 
			String line
	)
	{
		boolean foundAMove = false;
		for (MonsterCard card : foundCards.listOrderedByCardId())
		{
			if (moveName.isEmpty() || card.getMoveWithName(moveName) != null)
			{
				addMoveExclusion(card.id, moveName, removeFromPool, excludeFromRandomization, line);
				foundAMove = true;
			}
		}
		
		if (!foundAMove)
		{
			// Add a message to the warning string
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("Found ");
			warningsFoundParsing.append(foundCards.count());
			warningsFoundParsing.append(" card(s) with the specified name (");
			warningsFoundParsing.append(cardName);
			warningsFoundParsing.append(") but failed to find a move with name \"");
			warningsFoundParsing.append(moveName);
			warningsFoundParsing.append("\" on any of them so the line will be skipped:");
			warningsFoundParsing.append(IOUtils.NEWLINE);
			warningsFoundParsing.append("\t");
			warningsFoundParsing.append(line);
		}
	}
}
