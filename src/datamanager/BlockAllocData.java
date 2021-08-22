package datamanager;

import java.util.Map;

import compiler.DataBlock;
import compiler.Segment;
import rom.Texts;

public abstract class BlockAllocData
{
	protected DataBlock dataBlock;
	
	public BlockAllocData(DataBlock dataBlock)
	{
		this.dataBlock = dataBlock;
	}
	
	public String getId()
	{
		return dataBlock.getId();
	}
	
	public Map<String, Segment> getSegmentsById()
	{
		return dataBlock.getSegmentsById();
	}
	
	public void extractTexts(Texts texts)
	{
		dataBlock.extractTexts(texts);
	}

	public void writeData(byte[] bytes, int assignedAddress, Map<String, Integer> allocatedIndexes)
	{
		dataBlock.writeBytes(bytes, assignedAddress, allocatedIndexes);
	}
}
