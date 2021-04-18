package rom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Texts 
{
	private Map<Short, String> textMap;
	private Map<String, Short> usedText;

	public Texts()
	{
		textMap = new HashMap<>();
		usedText = new HashMap<>();
	}
	
	public Texts(Texts toCopy)
	{
		textMap = new HashMap<>(toCopy.textMap);
		usedText = new HashMap<>(toCopy.usedText);
	}
	
	public short insertTextAtNextId(String text)
	{
		short nextId = (short) (count() + 1); // 0 is a null pointer
		textMap.put(nextId, text);
		return nextId;
	}
	
	public short getId(String text)
	{
		// TODO implement
		return insertTextOrGetId(text);
	}
	
	public short insertTextOrGetId(String text)
	{
		Short id = usedText.get(text);
		if (id == null)
		{
			id = insertTextAtNextId(text);
			usedText.put(text, id);
		}

		return id;
	}
	
	public void removeTextAtId(short idToRemove)
	{
		usedText.remove(textMap.get(idToRemove));
		textMap.remove(idToRemove);
	}
	
	public void removeTextAtIds(Set<Short> idsToRemove)
	{
		for (short id : idsToRemove)
		{
			removeTextAtId(id);
		}
	}
	
	public String getAtId(short id)
	{
		return textMap.get(id);
	}
	
	public void putAtId(short id, String text)
	{
		textMap.put(id, text);
		usedText.put(text, id);
	}
	
	public short count()
	{
		return (short) textMap.size();
	}
}
