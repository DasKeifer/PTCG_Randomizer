package datamanager;

import java.util.Map;

import compiler.DataBlock;
import compiler.SegmentReference;
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
	
	public Map<String, SegmentReference> getSegmentReferencesById()
	{
		return dataBlock.getSegmentReferencesById();
	}
	
	public void extractTexts(Texts texts)
	{
		dataBlock.extractTexts(texts);
	}
	
	public void linkData(Texts romTexts, Map<String, SegmentReference> segRefsById)
	{
		dataBlock.linkData(romTexts, segRefsById);
	}

	public void writeData(byte[] bytes)
	{
		dataBlock.writeBytes(bytes);
	}
}
