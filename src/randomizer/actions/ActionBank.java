package randomizer.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class ActionBank
{
	private HashMap<Integer, Action> allActions;
	private HashMap<String, HashMap<Integer, Action>> actionsByCategory;
	
	public ActionBank()
	{
		allActions = new HashMap<>();
		actionsByCategory = new HashMap<>();
	}
	
	public void add(Action a)
	{
		allActions.put(a.getId(), a);
		
		HashMap<Integer, Action> actionCatogory = actionsByCategory.get(a.getCategory());
		if (actionCatogory == null)
		{
			actionCatogory = new HashMap<>();
			actionsByCategory.put(a.getCategory(), actionCatogory);
		}
		actionCatogory.put(a.getId(), a);
	}
	
	public Action get(int id)
	{
		return allActions.get(id);
	}
	
	public Collection<Action> get()
	{
		return get(null);
	}
	
	public Collection<Action> get(String category)
	{
		if (category == null || ActionCategories.CATEGORY_ALL.equals(category))
		{
			return allActions.values();
		}
		HashMap<Integer, Action> found = actionsByCategory.get(category);
		return found != null ? found.values() : Collections.emptyList();
	}
}
