package datamanager;


import java.util.List;
import java.util.SortedSet;

import constants.RomConstants;
import romAddressing.PrioritizedBankRange;

public class UnconstrainedMoveBlock extends MoveableBlock
{
	public UnconstrainedMoveBlock(String startingSegmentName)
	{
		super(startingSegmentName);
	}
	
	public UnconstrainedMoveBlock(String startingSegmentName, byte priority, byte startBank, byte stopBank)
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
	
	public UnconstrainedMoveBlock(List<String> sourceLines, byte priority, byte startBank, byte stopBank)
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
		// TODO: be consistent with priority - low is high?
		// TODO: Also change priority to an int to make it easier to deal with?
		SortedSet<PrioritizedBankRange> toReturn = super.getAllowableBankPreferences();
		toReturn.add(new PrioritizedBankRange((byte) 127, (byte) 0, (byte) (RomConstants.NUMBER_OF_BANKS - 1)));
		return toReturn;
	}
}
