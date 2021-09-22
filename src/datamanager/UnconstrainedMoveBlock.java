package datamanager;


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
}
