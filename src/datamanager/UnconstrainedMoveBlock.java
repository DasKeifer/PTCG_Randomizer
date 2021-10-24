package datamanager;


import java.util.List;
import java.util.SortedSet;

import constants.RomConstants;
import rom_addressing.PrioritizedBankRange;

public class UnconstrainedMoveBlock extends MoveableBlock
{
	public UnconstrainedMoveBlock(String startingSegmentName)
	{
		super(startingSegmentName);
	}
	
	public UnconstrainedMoveBlock(String startingSegmentName, int priority, byte startBank, byte stopBank)
	{
		super(startingSegmentName, priority, startBank, stopBank);
	}
	
	public UnconstrainedMoveBlock(String startingSegmentName, PrioritizedBankRange pref)
	{
		super(startingSegmentName, pref);
	}
	
	public UnconstrainedMoveBlock(String startingSegmentName, List<PrioritizedBankRange> prefs)
	{
		super(startingSegmentName, prefs);
	}

	public UnconstrainedMoveBlock(List<String> sourceLines)
	{
		super(sourceLines);
	}
	
	public UnconstrainedMoveBlock(List<String> sourceLines, int priority, byte startBank, byte stopBank)
	{
		super(sourceLines, priority, startBank, stopBank);
	}
	
	public UnconstrainedMoveBlock(List<String> sourceLines, PrioritizedBankRange pref)
	{
		super(sourceLines, pref);
	}
	
	public UnconstrainedMoveBlock(List<String> sourceLines, List<PrioritizedBankRange> prefs)
	{
		super(sourceLines, prefs);
	}

	@Override
	public SortedSet<PrioritizedBankRange> getAllowableBankPreferences()
	{
		SortedSet<PrioritizedBankRange> toReturn = super.getAllowableBankPreferences();
		toReturn.add(new PrioritizedBankRange(Integer.MAX_VALUE, (byte) 0, (byte) (RomConstants.NUMBER_OF_BANKS - 1)));
		return toReturn;
	}
}
