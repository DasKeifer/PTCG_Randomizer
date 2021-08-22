package datamanager;

import java.util.Map;
import java.util.SortedSet;

import compiler.DataBlock;
import constants.RomConstants;

public class UnconstrainedMoveBlock extends MoveableBlock
{
	public UnconstrainedMoveBlock(byte priority, DataBlock toPlaceInBank, BankPreference... prefs)
	{
		super(priority, toPlaceInBank, prefs);
	}

	@Override
	public SortedSet<BankPreference> getAllowableBankPreferences()
	{
		// TODO: be consistent with priority - low is high?
		SortedSet<BankPreference> toReturn = super.getAllowableBankPreferences();
		toReturn.add(new BankPreference((byte) 127, (byte) 0, (byte) (RomConstants.NUMBER_OF_BANKS - 1)));
		return toReturn;
	}

	@Override
	public boolean movesNotShrinks() 
	{
		// This moves
		return true;
	}

	@Override
	public int getShrunkWorstCaseSizeOnBank(byte unused1, int unused2, Map<String, Integer> unused3) 
	{
		return 0;
	}

	@Override
	public UnconstrainedMoveBlock getRemoteBlock() 
	{
		return this;
	}
}
