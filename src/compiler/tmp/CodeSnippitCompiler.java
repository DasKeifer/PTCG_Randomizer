package compiler.tmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datamanager.DataManager;

public class CodeSnippitCompiler 
{
	Map<Byte, List<CodeSnippit>> snippitsByPriority;
	
	public CodeSnippitCompiler()
	{
		snippitsByPriority = new HashMap<>();
	}
	
	public void addBankSpecificNonRelocatableSnippit(CodeSnippit snippit, byte requiredBank, byte priority)
	{
		addSnippit(snippit.getHighestPriority(), snippit);
	}
	
	public void addBankSpecificRelocatableSnippit(CodeSnippit snippit, byte requiredBank, byte priority, Map<Byte, Byte> bankPriorities, Set<Byte> excludedBanks)
	{
		
	}
	
	public void addNonBankSpecificSnippit(CodeSnippit snippit, Map<Byte, Byte> bankPriorities, Set<Byte> excludedBanks)
	{
		
	}
	
	private void addSnippit(byte priority, CodeSnippit snippit)
	{
		List<CodeSnippit> snippits = snippitsByPriority.get(priority);
		if (snippits == null)
		{
			snippitsByPriority.put(priority, new ArrayList<>());
		}
		
		snippits.add(snippit);
	}
	
	public void writeAllSnippits(byte[] data, DataManager space)
	{
		// For each priority go through and attempt to add the snippit
		// if it fails, take action accordingly
			// specific non relocatable - throw away
			// specific relocatable - try fitting jump in and adding back into list with alternate bank/priority
			// anybank - just put back in the queue
		
		// How to handle things like the text pointers?
		// There's a number of things we can only write once we have pointers - functions inlcuded.
		// Perhaps allocate space then go through and write looking for dependencies on other things (snippits, text, etc.)?
		// Or perhaps write once then go back and overwrite the specific locations?
		
		// 3 stages?
		// 1. Finalize texts/cardIds etc. for snippits
				// Maybe do this step when assigning moves?
				// PokemoneCard.setMove(move)
					// Then it checks if its dependent on card data and if so makes a copy and finalizes the data? Seems reasonable but only if
					// This is the last stage of randomization
		// 2. allocate space
		// 3. write data
		
		List<CodeSnippit> snippits;
		List<Byte> priorities = new LinkedList<Byte>(snippitsByPriority.keySet());
		while (!priorities.isEmpty())
		{
			snippits = snippitsByPriority.remove(priorities.remove(0));
			
			for (CodeSnippit snippit : snippits)
			{
				
				// try to place
				
				// If there wasn't space, then try the next highest priority
				byte newPriority = snippit.getNextHighestPriority();
				addSnippit(newPriority, snippit);
			}
		}
	}
}
