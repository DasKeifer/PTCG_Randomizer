package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

class DataManagerUtils 
{
	static void addToPriorityMap(TreeMap<Byte, List<AllocData>> priorityMap, AllocData data)
	{
		List<AllocData> blocksWithPriority = priorityMap.get(data.getPriority());
		
		if (blocksWithPriority == null)
		{
			blocksWithPriority = new LinkedList<>();
			priorityMap.put(data.getPriority(), blocksWithPriority);
		}
		
		blocksWithPriority.add(data);
	}

}
