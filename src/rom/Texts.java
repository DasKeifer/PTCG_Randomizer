package rom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Texts 
{
	private Map<Short, String> textMap = new HashMap<>();
	
	public short insertTextAtNextId(String text)
	{
		short nextId = (short) (count() + 1); // 0 is a null pointer
		textMap.put(nextId, text);
		return nextId;
	}
	
	public void removeTextAtIds(Set<Short> idsToRemove)
	{
		textMap.keySet().removeAll(idsToRemove);
	}
	
	public String getAtId(short id)
	{
		return textMap.get(id);
	}
	
	public void setAtId(short id, String text)
	{
		textMap.put(id, text);
	}
	
	public short count()
	{
		return (short) textMap.size();
	}
}
