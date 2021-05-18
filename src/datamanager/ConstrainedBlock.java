package datamanager;

import java.util.Map;

import compiler.DataBlock;
import compiler.SegmentReference;

public class ConstrainedBlock extends MoveableBlock
{	
	private DataBlock shrunkLocalBlock;
	private FloatingBlock shrunkRemoteBlock;
	
	public ConstrainedBlock(byte priority, DataBlock fullInBank, DataBlock shrunkLocalBlock, DataBlock shrunkRemoteBlock) 
	{
		super(priority, fullInBank);
		this.shrunkLocalBlock = shrunkLocalBlock;
		this.shrunkRemoteBlock = new FloatingBlock(priority, shrunkRemoteBlock);
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return shrunkLocalBlock.getWorstCaseSizeOnBank(bank);
	}

	@Override
	public boolean shrinksNotMoves() 
	{
		return true;
	}

	@Override
	public FloatingBlock applyShrink() 
	{
		return shrunkRemoteBlock;
	}

	@Override
	public FloatingBlock revertShrink() 
	{
		return shrunkRemoteBlock;
	}

	@Override
	public Map<String, SegmentReference> getSegmentReferencesById() 
	{
		if (isShrunkOrMoved())
		{
			return shrunkLocalBlock.getSegmentReferencesById();
		}
		
		return super.getSegmentReferencesById();
	}
	
	@Override
	public int writeBytes(byte[] bytes)
	{
		if (isShrunkOrMoved())
		{
			return shrunkLocalBlock.writeBytes(bytes);
		}
		
		return super.writeBytes(bytes);
	}
}