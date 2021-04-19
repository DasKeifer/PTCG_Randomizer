package rom;

import java.util.HashMap;
import java.util.Map;

public class Texts 
{
	private Map<Short, String> textMap;
	private Map<String, Short> reverseMap;

	public Texts()
	{
		textMap = new HashMap<>();
		reverseMap = new HashMap<>();
		
		// Put in the "null pointer" reservation at ID 0
		textMap.put((short) 0, "");
		reverseMap.put("", (short) 0);
	}
	
	public Texts(Texts toCopy)
	{
		textMap = new HashMap<>(toCopy.textMap);
		reverseMap = new HashMap<>(toCopy.reverseMap);
	}
	
	public short insertTextAtNextId(String text)
	{
		short nextId = count();
		textMap.put(nextId, text);
		reverseMap.put(text, nextId);
		return nextId;
	}
	
	public short getId(String text)
	{
		Short id = reverseMap.get(text);
		if (id == null)
		{
			return 0;
		}
		return id;
	}
	
	public short insertTextOrGetId(String text)
	{
		Short id = reverseMap.get(text);
		if (id == null)
		{
			// This takes care of placing in both maps
			id = insertTextAtNextId(text);
		}
		return id;
	}
	
	public String getAtId(short id)
	{
		return textMap.get(id);
	}
	
	public void putAtId(short id, String text)
	{
		textMap.put(id, text);
		reverseMap.put(text, id);
	}
	
	public short count()
	{
		return (short) textMap.size();
	}
}
