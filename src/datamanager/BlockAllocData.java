package datamanager;

import java.util.Map;

import compiler.DataBlock;
import compiler.SegmentReference;

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
	
	public Map<String, SegmentReference> getSegmentReferencesById()
	{
		return dataBlock.getSegmentReferencesById();
	}
}
