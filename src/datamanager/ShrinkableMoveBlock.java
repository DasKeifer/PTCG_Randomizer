package datamanager;

import java.util.Map;

import compiler.DataBlock;
import compiler.Segment;
import rom.Texts;

public class ShrinkableMoveBlock extends MoveableBlock
{	
	private DataBlock shrunkLocalBlock;
	private UnconstrainedMoveBlock shrunkRemoteBlock;
	
	public ShrinkableMoveBlock(byte priority, DataBlock fullInBank, DataBlock shrunkLocalBlock, DataBlock shrunkRemoteBlock, BankPreference... prefs)
	{
		super(priority, fullInBank, prefs);
		this.shrunkLocalBlock = shrunkLocalBlock;
		this.shrunkRemoteBlock = new UnconstrainedMoveBlock(priority, shrunkRemoteBlock);
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte bankToGetSizeOn, AllocatedIndexes allocatedIndexes)
	{
		// TODO: Combine alloc addresses into one arg?
		return shrunkLocalBlock.getWorstCaseSizeOnBank(bankToGetSizeOn, allocatedIndexes);
	}

	@Override
	public boolean movesNotShrinks() 
	{
		return false; // shrinks
	}

	@Override
	public UnconstrainedMoveBlock getRemoteBlock() 
	{
		return shrunkRemoteBlock;
	}

	@Override
	public Map<String, Segment> getSegmentsById() 
	{
		if (isShrunkOrMoved())
		{
			return shrunkLocalBlock.getSegmentsById();
		}
		
		return super.getSegmentsById();
	}

	@Override
	public void extractTexts(Texts texts)
	{
		if (isShrunkOrMoved())
		{
			shrunkLocalBlock.extractTexts(texts);
		}
		else
		{		
			super.extractTexts(texts);
		}
	}

	@Override
	public void writeData(byte[] bytes, int assignedAddress, AllocatedIndexes allocatedIndexes)
	{
		if (isShrunkOrMoved())
		{
			shrunkLocalBlock.writeBytes(bytes, assignedAddress, allocatedIndexes);
		}
		else
		{
			super.writeData(bytes, assignedAddress, allocatedIndexes);
		}
	}
}