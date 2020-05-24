package rom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Texts 
{
	private Map<Short, String> textMap = new HashMap<>();
	private Map<String, Short> usedText = new HashMap<>();

	public short insertTextAtNextId(String text)
	{
		short nextId = (short) (count() + 1); // 0 is a null pointer
		textMap.put(nextId, text);
		usedText.put(text, nextId);
		return nextId;
	}
	
	public short insertTextOrGetId(String text)
	{
		Short id = usedText.get(text);
		if (id != null)
		{
			return id;
		}
		
		return insertTextAtNextId(text);
	}
	
	public void removeTextAtIds(Set<Short> idsToRemove)
	{
		for (short id : idsToRemove)
		{
			usedText.remove(textMap.get(id));
			textMap.remove(id);
		}
	}
	
	public String getAtId(short id)
	{
		return textMap.get(id);
	}
	
	public void setAtId(short id, String text)
	{
		textMap.put(id, text);
		usedText.put(text, id);
	}
	
	public short count()
	{
		return (short) textMap.size();
	}
}
