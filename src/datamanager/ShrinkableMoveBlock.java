package datamanager;

import java.util.Map;

import compiler.DataBlock;
import compiler.SegmentReference;
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
	public int getShrunkWorstCaseSizeOnBank(byte bank) 
	{
		return shrunkLocalBlock.getWorstCaseSizeOnBank(bank);
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
	public Map<String, SegmentReference> getSegmentReferencesById() 
	{
		if (isShrunkOrMoved())
		{
			return shrunkLocalBlock.getSegmentReferencesById();
		}
		
		return super.getSegmentReferencesById();
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
	public void linkData(Texts romTexts, Map<String, SegmentReference> segRefsById)
	{
		if (isShrunkOrMoved())
		{
			shrunkLocalBlock.linkData(romTexts, segRefsById);
		}
		else
		{		
			super.linkData(romTexts, segRefsById);
		}
	}

	@Override
	public void writeData(byte[] bytes)
	{
		if (isShrunkOrMoved())
		{
			shrunkLocalBlock.writeBytes(bytes);
		}
		else
		{
			super.writeData(bytes);
		}
	}
}