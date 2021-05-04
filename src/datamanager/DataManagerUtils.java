package datamanager;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

class DataManagerUtils 
{
	static void addToPriorityMap(SortedMap<Byte, List<MoveableBlock>> priorityMap, MoveableBlock data)
	{
		List<MoveableBlock> blocksWithPriority = priorityMap.get(data.getPriority());
		
		if (blocksWithPriority == null)
		{
			blocksWithPriority = new LinkedList<>();
			priorityMap.put(data.getPriority(), blocksWithPriority);
		}
		
		blocksWithPriority.add(data);
	}

}
