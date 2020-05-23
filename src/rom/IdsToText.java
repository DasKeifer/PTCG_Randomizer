package rom;

import java.util.HashMap;
import java.util.Map;

public class IdsToText 
{
	public Map<Short, String> ptrToText = new HashMap<>();
	
	public short insertTextAtNextId(String text)
	{
		short nextId = (short) (count() + 1); // 0 is a null pointer
		ptrToText.put(nextId, text);
		return nextId;
	}
	
	public String getAtId(short id)
	{
		return ptrToText.get(id);
	}
	
	public short count()
	{
		return (short) ptrToText.size();
	}
}
