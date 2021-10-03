package datamanager;


import java.util.List;
import java.util.SortedSet;

import constants.RomConstants;

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
	
	public UnconstrainedMoveBlock(String startingSegmentName, BankPreference pref)
	{
		super(startingSegmentName, pref);
	}
	
	public UnconstrainedMoveBlock(String startingSegmentName, List<BankPreference> prefs)
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
	
	public UnconstrainedMoveBlock(List<String> sourceLines, BankPreference pref)
	{
		super(sourceLines, pref);
	}
	
	public UnconstrainedMoveBlock(List<String> sourceLines, List<BankPreference> prefs)
	{
		super(sourceLines, prefs);
	}

	@Override
	public SortedSet<BankPreference> getAllowableBankPreferences()
	{
		// TODO: be consistent with priority - low is high?
		SortedSet<BankPreference> toReturn = super.getAllowableBankPreferences();
		toReturn.add(new BankPreference((byte) 127, (byte) 0, (byte) (RomConstants.NUMBER_OF_BANKS - 1)));
		return toReturn;
	}
}
