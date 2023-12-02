package ptcgr.randomizer.actions;

import java.util.ArrayList;
import java.util.List;

public class ActionCategories 
{
	public static String CATEGORY_ALL 		= "All";
	public static String CATEGORY_CARDS 	= "Cards";
	public static String CATEGORY_MOVES 	= "Moves";
	public static String CATEGORY_DECKS 	= "Decks";
	public static String CATEGORY_PACKS 	= "Packs";
	public static String CATEGORY_TWEAKS 	= "Tweaks";
	
	private static List<String> categories = new ArrayList<String>(
			List.of(CATEGORY_ALL, CATEGORY_CARDS, CATEGORY_MOVES, 
					CATEGORY_DECKS, CATEGORY_PACKS, CATEGORY_TWEAKS));
	
	public static void addCategory(String category)
	{
		categories.add(category);
	}
	
	// Categories except "ALL"
	public static List<String> getCategories()
	{
		return categories.subList(1, categories.size());
	}
	
	public static List<String> getCategoriesWithAll()
	{
		return categories;
	}
}
