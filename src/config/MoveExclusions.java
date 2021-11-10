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

import javax.swing.JOptionPane;

import constants.CardConstants.CardId;
import data.Card;
import data.Move;
import data.romtexts.CardName;
import rom.Cards;
import util.IOUtils;

public class MoveExclusions
{
	public static final String FILE_NAME = "MoveExclusions.csv";

	Map<CardId, List<MoveExclusionData>> exclByCardId;
	Map<String, List<MoveExclusionData>> exclByMoveName;
	
	private MoveExclusions()
	{
		exclByCardId = new EnumMap<>(CardId.class);
		exclByMoveName = new HashMap<>();
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
	
	public boolean addMoveExclusion(CardId cardId, String moveName, boolean removeFromPool, boolean excludeFromRandomization)
	{
		return validateAndAddMoveExclusion(new MoveExclusionData(cardId, moveName, removeFromPool, excludeFromRandomization));
	}
	
	private boolean validateAndAddMoveExclusion(MoveExclusionData excl)
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
		else
		{
			List<MoveExclusionData> list = exclByMoveName.computeIfAbsent(excl.getMoveName(), 
					ll -> new LinkedList<>());
			list.add(excl);
			success = true;
		}

		return success;
	}
	
	public static MoveExclusions parseMoveExclusionsCsv(Cards<Card> allCards, Component toCenterPopupsOn)
	{
		MoveExclusions exclusions = new MoveExclusions();
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
		        	if (!line.startsWith("#"))
		        	{
			        	List<MoveExclusionData> excls = parseLine(line, allCards, warningsFound);
			        		
			        	for (MoveExclusionData excl : excls)
			        	{
			        		if (!exclusions.validateAndAddMoveExclusion(excl))
			        		{
			        			warningsFound.append(IOUtils.NEWLINE);
			        			warningsFound.append("Failed to validate and add move exclusion for card id \"");
			        			warningsFound.append(excl.getCardId().toString());
			        			warningsFound.append("\" with move name \"");
			        			warningsFound.append(excl.getMoveName());
			        			warningsFound.append(" while parsing line for unknown reasons - it will be skipped:");
			        			warningsFound.append(IOUtils.NEWLINE);
			        			warningsFound.append("\t");
			        			warningsFound.append(line);
			        		}
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
    	// If we don't limit it, it will remove empty columns so we use a negative
    	// number to get all the columns without actually limiting it
		String[] args = line.split(",", -1);
		
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
	    	boolean removeFromPool = args[0].equalsIgnoreCase("true");
	    	if (!removeFromPool && !args[0].equalsIgnoreCase("false"))
	    	{
				// Add a message to the warning string
				warningsFound.append(IOUtils.NEWLINE);
				warningsFound.append("Line's \"Remove Move from randomization pool\" field specifies a value other ");
				warningsFound.append("than \"true\" or \"false\". False will be assumed for this line: ");
    			warningsFound.append(IOUtils.NEWLINE);
    			warningsFound.append("\t");
				warningsFound.append(line);
	    	}
	    	
	    	boolean excludeFromRandomization = args[1].equalsIgnoreCase("true");
	    	if (!excludeFromRandomization && !args[1].equalsIgnoreCase("false"))
	    	{
				// Add a message to the warning string
				warningsFound.append(IOUtils.NEWLINE);
				warningsFound.append("Line's \"Exclude Move from Randomization (i.e. Move stays on card(s))\" field specifies ");
				warningsFound.append("a value other  than \"true\" or \"false\". False will be assumed for this line: ");
    			warningsFound.append(IOUtils.NEWLINE);
    			warningsFound.append("\t");
				warningsFound.append(line);
	    	}
	    	
	    	return createMoveExclusionData(removeFromPool, excludeFromRandomization, args[2], args[3], allCards, warningsFound, line);
		}
    	
    	return new LinkedList<>();
	}
	
	private static List<MoveExclusionData> createMoveExclusionData(boolean removeFromPool, boolean excludeFromRandomization, String cardNameOrId, String moveName, Cards<Card> allCards, StringBuilder warningsFound, String line)
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
    			parsedExcl.add(new MoveExclusionData(CardId.NO_CARD, moveName, removeFromPool, excludeFromRandomization));
    		}
    	}
    	else
    	{
	    	// Assume its a card ID
	    	try
	    	{
	    		parsedExcl.add(new MoveExclusionData(CardId.readFromByte(Byte.parseByte(cardNameOrId)), 
	    				moveName, removeFromPool, excludeFromRandomization));
	    	}
	    	// Otherwise assume its a name which means it could apply to a number of
	    	// cards
	    	catch (IllegalArgumentException e) // Includes number format exception
	    	{
	    		Cards<Card> foundCards = allCards.getCardsWithNameIgnoringNumber(cardNameOrId);
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
	    			// If we found some, see if its numbered
		    		if (CardName.doesHaveNumber(cardNameOrId))
		    		{
		    			Card specificCard = Cards.getCardFromNameSetBasedOnNumber(foundCards, cardNameOrId);
		    			if (specificCard == null)
		    			{
			    			// Add a message to the warning string
			    			warningsFound.append(IOUtils.NEWLINE);
			    			warningsFound.append("Detected the card name has a number (e.g. \"<nameOnCard>_1\") ");
			    			warningsFound.append("but failed to get the card with that number (i.e. failed to ");
			    			warningsFound.append("parse the number or it was out of bounds) from the set found with ");
			    			warningsFound.append("the card name (found ");
			    			warningsFound.append(foundCards.count());
			    			warningsFound.append("). Line will be skipped:");
			    			warningsFound.append(IOUtils.NEWLINE);
			    			warningsFound.append("\t");
			    			warningsFound.append(line);
		    			}
		    			// Found the card!
		    			else 
		    			{
		    	    		parsedExcl.add(new MoveExclusionData(specificCard.id, moveName, removeFromPool, excludeFromRandomization));
		    			}
		    		}
		    		// Otherwise it applies to all
		    		else
		    		{
		    			for (Card card : foundCards.toList())
		    			{
		    				parsedExcl.add(new MoveExclusionData(card.id, moveName, removeFromPool, excludeFromRandomization));
		    			}
		    		}
	    		}
	    	}
    	}
    	
		return parsedExcl;
	}
}
